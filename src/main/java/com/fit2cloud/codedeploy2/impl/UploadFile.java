package com.fit2cloud.codedeploy2.impl;

import com.fit2cloud.codedeploy2.F2CCodeDeploySouthPublisher;
import com.fit2cloud.codedeploy2.Utils;
import com.fit2cloud.codedeploy2.client.model.ApplicationRepository;
import com.fit2cloud.codedeploy2.oss.*;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.DirScanner;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author yankaijun
 * @date 2019-10-16 17:18
 */
public class UploadFile {

    private static final String COMMON_APP = "common";
    private static final String CONTAINER_APP = "container";
    private boolean myResult;
    private Run<?, ?> build;
    private TaskListener listener;
    private String projectName;
    private FilePath workspace;
    private int builtNumber;
    private ApplicationRepository applicationRepository;
    private String newAddress;
    private String artifactType;

    public UploadFile(Run<?, ?> build, TaskListener listener, String projectName, FilePath workspace, int builtNumber, ApplicationRepository applicationRepository, String artifactType) {
        this.build = build;
        this.listener = listener;
        this.projectName = projectName;
        this.workspace = workspace;
        this.builtNumber = builtNumber;
        this.applicationRepository = applicationRepository;
        this.artifactType = artifactType;
    }

    public boolean failed() {
        return myResult;
    }

    public String getNewAddress() {
        return newAddress;
    }

    public UploadFile invoke(F2CCodeDeploySouthPublisher publisher) throws IOException, InterruptedException {
        String applicationType = publisher.getApplicationType();
        if (COMMON_APP.equals(applicationType)) {
            commonUpload(publisher);
        } else if (CONTAINER_APP.equals(applicationType)) {
            containerUpload(publisher);
        } else {
            publisher.log("暂时不支持应用类型，目前仅支持普通应用，容器应用");
            myResult = true;
        }
        return this;
    }

    private void containerUpload(F2CCodeDeploySouthPublisher publisher) throws IOException, InterruptedException {
        String imageNameWithTag = publisher.getImageNameWithTag();
        StringBuffer command = new StringBuffer();
        //打包好了，直接构建镜像，上传镜像
        String repository = applicationRepository.getRepository();
        repository = repository.replace("https://", "").replace("http://", "");
        command.append("docker login ").append(repository)
                .append(" -u ").append(applicationRepository.getAccessId())
                .append(" -p ").append(applicationRepository.getAccessPassword())
                .append(" && ");
        command.append("docker build -t ").append(imageNameWithTag).append(" . && ");
        command.append("docker push ").append(imageNameWithTag);
        String workPath = "${WORKSPACE}";
        workPath = Utils.replaceTokens(build, listener, workPath);
        switch (this.artifactType) {
            case ArtifactType.ARTIFACTORY:
            case ArtifactType.HARBOR:
                try {
                    String result = DockerUtils.commonExecCmdWithResult(command.toString(), workPath);
                    publisher.log(result);
                } catch (Exception e) {
                    myResult = true;
                    e.printStackTrace();
                }
                break;
            default:
                publisher.log("暂时不支持 " + this.artifactType + " 类型制品库");
                myResult = true;
                break;
        }
    }

    private void commonUpload(F2CCodeDeploySouthPublisher publisher) {
        File zipFile = null;
        newAddress = null;
        try {
            String zipFileName = projectName + "-" + builtNumber + ".zip";
            String includesNew = Utils.replaceTokens(build, listener, publisher.getIncludes());
            String excludesNew = Utils.replaceTokens(build, listener, publisher.getIncludes());
            String appspecFilePathNew = Utils.replaceTokens(build, listener, publisher.getAppspecFilePath());
            zipFile = zipFile(zipFileName, workspace, includesNew, excludesNew, appspecFilePathNew, publisher);
            switch (this.artifactType) {
                case ArtifactType.OSS:
                    publisher.log("开始上传zip文件到OSS服务器");
                    //getBucketLocation
                    String expFP = Utils.replaceTokens(build, listener, zipFile.toString());

                    if (expFP != null) {
                        expFP = expFP.trim();
                    }
                    // Resolve virtual path
                    String expVP = Utils.replaceTokens(build, listener, publisher.getObjectPrefixAliyun());
                    if (Utils.isNullOrEmpty(expVP)) {
                        expVP = null;
                    }
                    if (!Utils.isNullOrEmpty(expVP) && !expVP.endsWith(Utils.FWD_SLASH)) {
                        expVP = expVP.trim() + Utils.FWD_SLASH;
                    }
                    try {
                        int filesUploaded = AliyunOSSClient.upload(build, workspace, listener,
                                applicationRepository.getAccessId(),
                                applicationRepository.getAccessPassword(),
                                ".aliyuncs.com",
                                applicationRepository.getRepository().replace("bucket:", ""), expFP, expVP, zipFile);
                        if (filesUploaded > 0) {
                            publisher.log("上传Artifacts到阿里云OSS成功!");
                        }
                    } catch (Exception e) {
                        publisher.log("上传Artifact到阿里云OSS失败，错误消息如下:");
                        publisher.log(e.getMessage());
                        myResult = true;
                    }
                    publisher.log("上传zip文件到oss服务器成功!");
                    if (expVP == null) {
                        newAddress = zipFile.getName();
                    } else {
                        newAddress = publisher.getObjectPrefixAliyun() + "/" + zipFile.getName();
                    }
                    publisher.log("文件路径" + newAddress);
                    break;
                case ArtifactType.ARTIFACTORY:
                    publisher.log("开始上传zip文件到Artifactory服务器");
                    if (StringUtils.isBlank(publisher.getPath())) {
                        publisher.log("请输入上传至 Artifactory 的 Path");
                        myResult = true;
                    }
                    String pathNew = Utils.replaceTokens(build, listener, publisher.getPath());
                    try {

                        String r = applicationRepository.getRepository();
                        String server = r.substring(0, r.indexOf("/artifactory"));
                        newAddress = ArtifactoryUploader.uploadArtifactory(zipFile, server.trim(),
                                applicationRepository.getAccessId(), applicationRepository.getAccessPassword(), r, pathNew);
                    } catch (Exception e) {
                        publisher.log("上传文件到 Artifactory 服务器失败！错误消息如下:");
                        publisher.log(e.getMessage());
                        myResult = true;
                    }
                    publisher.log("上传zip文件到Artifactory服务器成功!");
                    break;
                case ArtifactType.NEXUS:
                    if (StringUtils.isBlank(publisher.getNexusArtifactId()) || StringUtils.isBlank(publisher.getNexusGroupId()) || StringUtils.isBlank(publisher.getNexusArtifactVersion())) {
                        publisher.log("请输入上传至 Nexus 的 GroupId、 ArtifactId 和 NexusArtifactVersion");
                        myResult = true;
                    }
                    String nexusGroupIdNew = Utils.replaceTokens(build, listener, publisher.getNexusArtifactId());
                    String nexusArtifactIdNew = Utils.replaceTokens(build, listener, publisher.getNexusGroupId());
                    String nexusArtifactVersionNew = Utils.replaceTokens(build, listener, publisher.getNexusArtifactVersion());

                    publisher.log("开始上传zip文件到nexus服务器");
                    try {
                        newAddress = NexusUploader.upload(zipFile, applicationRepository.getAccessId(), applicationRepository.getAccessPassword(), applicationRepository.getRepository(),
                                nexusGroupIdNew, nexusArtifactIdNew, String.valueOf(builtNumber), "zip", nexusArtifactVersionNew);
                    } catch (Exception e) {
                        publisher.log("上传文件到 Nexus 服务器失败！错误消息如下:");
                        publisher.log(e.getMessage());
                        //e.printStackTrace(F2CCodeDeploySouthPublisher.logger);
                        myResult = true;
                    }
                    publisher.log("上传zip文件到nexus服务器成功!");
                    break;
                case ArtifactType.S3:
                    publisher.log("开始上传zip文件到AWS服务器");
                    //getBucketLocation
                    String expFPAws = Utils.replaceTokens(build, listener, zipFile.toString());

                    if (expFPAws != null) {
                        expFPAws = expFPAws.trim();
                    }

                    // Resolve virtual path
                    String expVPAws = Utils.replaceTokens(build, listener, publisher.getObjectPrefixAWS());
                    if (Utils.isNullOrEmpty(expVPAws)) {
                        expVPAws = null;
                    }
                    if (!Utils.isNullOrEmpty(expVPAws) && !expVPAws.endsWith(Utils.FWD_SLASH)) {
                        expVPAws = expVPAws.trim() + Utils.FWD_SLASH;
                    }
                    try {
                        AWSS3Client.upload(build, workspace, listener,
                                applicationRepository.getAccessId(),
                                applicationRepository.getAccessPassword(),
                                null,
                                applicationRepository.getRepository(), expFPAws, expVPAws, zipFile);
                        publisher.log("上传Artifacts到亚马逊AWS成功!");
                    } catch (Exception e) {
                        publisher.log("上传Artifact到亚马逊AWS失败，错误消息如下:");
                        publisher.log(e.getMessage());
                        //e.printStackTrace(F2CCodeDeploySouthPublisher.logger);
                        myResult = true;
                    }
                    publisher.log("上传zip文件到亚马逊AWS服务器成功!");
                    if (expVPAws == null) {
                        newAddress = zipFile.getName();
                    } else {
                        newAddress = publisher.getObjectPrefixAWS() + "/" + zipFile.getName();
                    }
                    publisher.log("文件路径:" + newAddress);
                    break;
                default:
                    publisher.log("暂时不支持 " + this.artifactType + " 类型制品库");
                    myResult = true;
            }
            if (!myResult) {
                publisher.log("上传成功");
            }
        } catch (Exception e) {
            publisher.log("生成ZIP包失败: " + e.getMessage());
            myResult = true;
        } finally {
            if (zipFile != null && zipFile.exists()) {
                try {
                    publisher.log("删除 Zip 文件 " + zipFile.getAbsolutePath());
                    zipFile.delete();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * 根据指定的文件信息打包成zip文件
     *
     * @param zipFileName
     * @param sourceDirectory
     * @param includesNew
     * @param excludesNew
     * @param appspecFilePathNew
     * @param publisher
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws IllegalArgumentException
     */
    private File zipFile(String zipFileName, FilePath sourceDirectory, String includesNew, String excludesNew, String appspecFilePathNew, F2CCodeDeploySouthPublisher publisher) throws IOException, InterruptedException, IllegalArgumentException {
        FilePath appspecFp = new FilePath(sourceDirectory, appspecFilePathNew);

        publisher.log("指定 appspecPath ::::: " + appspecFp.toURI().getPath());
        if (appspecFp.exists()) {
            if (!"appspec.yml".equals(appspecFilePathNew)) {
                FilePath appspecDestFP = new FilePath(sourceDirectory, "appspec.yml");
                publisher.log("目标 appspecPath  ::::: " + appspecDestFP.toURI().getPath());
                appspecFp.copyTo(appspecDestFP);
            }
            publisher.log("成功添加appspec文件");
        } else {
            throw new IllegalArgumentException("没有找到对应的appspec.yml文件！");
        }
        //update by and 版本为空就默认（任务民+jenkins构建号） 否则是指定的版本
        zipFileName = StringUtils.isNotBlank(publisher.getApplicationVersionName()) ? publisher.getApplicationVersionName() + ".zip" : zipFileName;
        File zipFile = new File("/tmp/" + zipFileName);
        final boolean fileCreated = zipFile.createNewFile();
        if (!fileCreated) {
            publisher.log("Zip文件已存在，开始覆盖 : " + zipFile.getPath());
        }

        publisher.log("生成Zip文件 : " + zipFile.getAbsolutePath());
        FileOutputStream outputStream = new FileOutputStream(zipFile);
        try {
            String allIncludes = includesNew + ",appspec.yml";
            sourceDirectory.zip(
                    outputStream,
                    new DirScanner.Glob(allIncludes, excludesNew)
            );
        } finally {
            outputStream.close();
        }
        return zipFile;
    }
}
