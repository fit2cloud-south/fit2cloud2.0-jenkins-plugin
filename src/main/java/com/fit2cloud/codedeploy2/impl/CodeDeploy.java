package com.fit2cloud.codedeploy2.impl;

import com.fit2cloud.codedeploy2.F2CCodeDeploySouthPublisher;
import com.fit2cloud.codedeploy2.Utils;
import com.fit2cloud.codedeploy2.client.Fit2cloudClient;
import com.fit2cloud.codedeploy2.client.model.*;
import hudson.model.Run;
import hudson.model.TaskListener;

/**
 * @author yankaijun
 * @date 2019-10-16 17:31
 */
public class CodeDeploy {

    private static final String COMMON_APP = "common";
    private static final String CONTAINER_APP = "container";
    private boolean myResult;
    private Run<?, ?> build;
    private TaskListener listener;
    private String workspaceId;
    private String applicationId;
    private String clusterId;
    private String cloudServerId;
    private String clusterRoleId;
    private Fit2cloudClient fit2cloudClient;
    private ApplicationRepositorySetting repSetting;
    private String newAddress;
    /**
     * 正确部署完成
     */
    private boolean deployFlag = true;

    public CodeDeploy(Run<?, ?> build, TaskListener listener, String workspaceId, String applicationId, String clusterId, String cloudServerId, String clusterRoleId, Fit2cloudClient fit2cloudClient, ApplicationRepositorySetting repSetting, String newAddress) {
        this.build = build;
        this.listener = listener;
        this.workspaceId = workspaceId;
        this.applicationId = applicationId;
        this.clusterId = clusterId;
        this.cloudServerId = cloudServerId;
        this.clusterRoleId = clusterRoleId;
        this.fit2cloudClient = fit2cloudClient;
        this.repSetting = repSetting;
        this.newAddress = newAddress;
    }

    public boolean failed() {
        return myResult;
    }

    public boolean isDeployFlag() {
        return deployFlag;
    }

    public CodeDeploy invoke(F2CCodeDeploySouthPublisher publisher) {
        //版本注册
        ApplicationVersion appVersion = registryVersion(publisher);
        String applicationType = publisher.getApplicationType();
        //应用部署
        if (COMMON_APP.equals(applicationType)) {
            deployCommonApp(publisher, appVersion);
        } else if (CONTAINER_APP.equals(applicationType)) {
            deployContainerApp(publisher, appVersion);
        }
        return this;
    }

    private ApplicationVersion registryVersion(F2CCodeDeploySouthPublisher publisher) {
        ApplicationVersion appVersion;
        try {
            publisher.log("注册应用版本中...");
            if (repSetting == null) {
                throw new CodeDeployException("仓库设置不正确，未获得正确仓库信息");
            }
            String versionName = publisher.isApplicationCommon() ? publisher.getApplicationVersionName() : publisher.getPaasApplicationVersionName();
            String newAppVersion = Utils.replaceTokens(build, listener, versionName);
            publisher.log("版本信息：" + newAppVersion);
            ApplicationVersionDTO applicationVersion = new ApplicationVersionDTO();
            applicationVersion.setApplicationId(applicationId);
            applicationVersion.setName(newAppVersion);
            applicationVersion.setEnvironmentValueId(repSetting.getEnvId());
            applicationVersion.setApplicationRepositoryId(repSetting.getRepositoryId());
            applicationVersion.setLocation(newAddress);
            appVersion = fit2cloudClient.createApplicationVersion(applicationVersion, workspaceId);
            publisher.log("注册版本成功！");
            return appVersion;
        } catch (Exception e) {
            publisher.log("版本注册失败！ 原因：" + e.getMessage());
            myResult = true;
        }
        return null;
    }

    private void deployCommonApp(F2CCodeDeploySouthPublisher publisher, ApplicationVersion appVersion) {
        ApplicationDeployment applicationDeploy = null;
        try {
            if (publisher.isAutoDeploy()) {
                publisher.log("创建代码部署任务...");
                ApplicationDeployment applicationDeployment = new ApplicationDeployment();
                applicationDeployment.setClusterId(clusterId);
                applicationDeployment.setClusterRoleId(clusterRoleId);
                applicationDeployment.setCloudServerId(cloudServerId);
                applicationDeployment.setApplicationVersionId(appVersion.getId());
                applicationDeployment.setPolicy(publisher.getDeployPolicy());
                applicationDeployment.setDeploymentLevel(publisher.getDeploymentLevel());
                applicationDeployment.setBackupQuantity(publisher.getBackupQuantity());
                applicationDeployment.setDescription("Jenkins 触发");
                applicationDeploy = fit2cloudClient.createApplicationDeployment(applicationDeployment, workspaceId);
            }
        } catch (Exception e) {
            publisher.log("创建代码部署任务失败: " + e.getMessage());
            myResult = true;
            return;
        }
        try {
            int i = 0;
            if (publisher.isApplicationCommon() && publisher.isAutoDeploy() && publisher.isWaitForCompletion()) {
                publisher.log("执行代码部署...");
                Long pollingFreqSec = publisher.getPollingFreqSec();
                while (true) {
                    Thread.sleep(1000 * pollingFreqSec);
                    ApplicationDeployment applicationDeployment = fit2cloudClient.getApplicationDeployment(applicationDeploy.getId());
                    if (applicationDeployment.getStatus().equalsIgnoreCase("success")
                            || applicationDeployment.getStatus().equalsIgnoreCase("fail")) {
                        publisher.log("部署完成！");
                        if (applicationDeployment.getStatus().equalsIgnoreCase("success")) {
                            publisher.log("部署结果: 成功");
                        } else {
                            deployFlag = false;
                            throw new Exception("部署任务执行失败，具体结果请登录FIT2CLOUD控制台查看！");
                        }
                        break;
                    } else {
                        publisher.log("部署任务运行中...");
                    }
                }
                if (pollingFreqSec * ++i > pollingFreqSec) {
                    deployFlag = false;
                    throw new Exception("部署超时,请查看FIT2CLOUD控制台！");
                }
            }
        } catch (Exception e) {
            publisher.log("执行代码部署失败: " + e.getMessage());
            deployFlag = false;
        }
    }

    private void deployContainerApp(F2CCodeDeploySouthPublisher publisher, ApplicationVersion appVersion) {
        publisher.log("创建PaaS代码部署任务...");
        ApplicationDeployment paaSApplicationDeployment = null;
        try {
            if (publisher.isAutoPaaSDeploy()) {
                publisher.log("创建PaaS部署任务...");
                PaaSDeployRequest paaSDeployRequest = new PaaSDeployRequest();
                paaSDeployRequest.setProjectName(publisher.getProjectName());
                paaSDeployRequest.setClusterName(publisher.getCluster());
                paaSDeployRequest.setNamespace(publisher.getNamespace());
                paaSDeployRequest.setAppName(publisher.getAppName());
                paaSDeployRequest.setAppDisplayName(publisher.getAppName());
                paaSDeployRequest.setContainerName(publisher.getContainerName());
                paaSDeployRequest.setReplicas(Integer.valueOf(publisher.getReplicas()));
                paaSDeployRequest.setLimitCpu(publisher.getLimitCpu());
                paaSDeployRequest.setLimitMemory(publisher.getLimitMemory());
                paaSDeployRequest.setStrategy(publisher.getStrategy());
                paaSDeployRequest.setResourceKind(publisher.getResourceKind());
                paaSDeployRequest.setDescription(publisher.getDescription());
                paaSDeployRequest.setApplicationVersionId(appVersion.getId());
                paaSApplicationDeployment = fit2cloudClient.createPaaSApplicationDeployment(paaSDeployRequest, workspaceId);
            }
        } catch (Exception e) {
            publisher.log("创建PaaS部署任务失败: " + e.getMessage());
            myResult = true;
            return;
        }
        //获取PaaS任务的部署情况
        try {
            int i = 0;
            if (publisher.isApplicationContainer() && publisher.isAutoPaaSDeploy() && publisher.isWaitForCompletion()) {
                publisher.log("执行PaaS部署...");
                Long pollingFreqSec = publisher.getPollingFreqSec();
                while (true) {
                    Thread.sleep(1000 * pollingFreqSec);
                    ApplicationDeployment applicationDeployment = fit2cloudClient.getPaaSApplicationDeployment(paaSApplicationDeployment.getId());
                    if (applicationDeployment.getStatus().equalsIgnoreCase("success") || applicationDeployment.getStatus().equalsIgnoreCase("fail")) {
                        publisher.log("PaaS部署完成！");
                        if (applicationDeployment.getStatus().equalsIgnoreCase("success")) {
                            publisher.log("PaaS部署结果: 成功");
                        } else {
                            deployFlag = false;
                            throw new Exception("部署PaaS任务执行失败，具体结果请登录FIT2CLOUD控制台查看！");
                        }
                        break;
                    } else {
                        publisher.log("部署PaaS任务运行中...");
                    }
                }
                if (pollingFreqSec * ++i > pollingFreqSec) {
                    deployFlag = false;
                    throw new Exception("部署超时,请查看FIT2CLOUD控制台！");
                }
            }
        } catch (Exception e) {
            publisher.log("执行PaaS部署失败: " + e.getMessage());
            deployFlag = false;
        }
    }

}
