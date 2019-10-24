package com.fit2cloud.codedeploy2;

import com.fit2cloud.codedeploy2.client.Fit2cloudClient;
import com.fit2cloud.codedeploy2.client.model.ApplicationRepository;
import com.fit2cloud.codedeploy2.client.model.ApplicationRepositorySetting;
import com.fit2cloud.codedeploy2.impl.*;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.support.steps.build.RunWrapper;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintStream;

/**
 * 不再直接继承 Publisher
 * 代码官方建议继承 Recorder
 *
 * @author yankaijun
 */
public class F2CCodeDeploySouthPublisher extends Recorder implements SimpleBuildStep {
    private static final String LOG_PREFIX = "[FIT2CLOUD 代码部署 V2.0]";
    private final String f2cEndpoint;
    private final String f2cAccessKey;
    private final String f2cSecretKey;
    private final String workspaceId;
    private final String applicationId;
    private final String applicationRepositoryId = null;
    private final String clusterId;
    private final String clusterRoleId;
    private final String cloudServerId;
    private final String deployPolicy;
    private final String deploymentLevel;
    private final Integer backupQuantity;
    private final String applicationVersionName;
    private final String applicationSettingId = null;
    private final boolean autoDeploy;
    private final String includes;
    private final String excludes;
    private final String appspecFilePath;
    private final String description;
    private final boolean waitForCompletion;
    private final Long pollingTimeoutSec;
    private final Long pollingFreqSec;
    private final String nexusGroupId;
    private final String nexusArtifactId;
    private final String nexusArtifactVersion;
    private final boolean nexusChecked;
    private final boolean ossChecked;
    private final boolean s3Checked;
    private final boolean artifactoryChecked;
    private final boolean harborChecked;
    /**
     * 容器应用参数 begin
     */
    private final String imagePath;
    private final boolean applicationCommon;
    private final boolean applicationContainer;
    private final String imageNameWithTag;
    private final String projectName;
    private final String cluster;
    private final String namespace;
    private final String appName;
    private final String containerName;
    private final String replicas;
    private final String limitCpu;
    private final String limitMemory;
    private final String strategy;
    private final String resourceKind;
    private final String applicationType;
    private final String paasApplicationVersionName;
    private final boolean autoPaaSDeploy;
    /**
     * 容器应用参数 end
     */
    private final String path;
    //上传到阿里云参数
    private final String objectPrefixAliyun;
    //上传到亚马逊参数
    private final String objectPrefixAWS;
    private final String repositorySettingId;
    private final String artifactType;
    private PrintStream logger;

    private static final String COMMON_APP = "common";
    private static final String CONTAINER_APP = "container";

    /**
     * Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
     */
    @DataBoundConstructor
    public F2CCodeDeploySouthPublisher(String f2cEndpoint,
                                       String f2cAccessKey,
                                       String f2cSecretKey,
                                       String applicationId,
                                       String clusterId,
                                       String clusterRoleId,
                                       String workspaceId,
                                       String cloudServerId,
                                       String deployPolicy,
                                       String deploymentLevel,
                                       Integer backupQuantity,
                                       String applicationVersionName,
                                       String paasApplicationVersionName,
                                       boolean waitForCompletion,
                                       boolean autoDeploy,
                                       Long pollingTimeoutSec,
                                       Long pollingFreqSec,
                                       String includes,
                                       String excludes,
                                       String appspecFilePath,
                                       String description,
                                       String artifactType,
                                       String applicationType,
                                       String repositorySettingId,
                                       String objectPrefixAliyun,
                                       String objectPrefixAWS,
                                       String path,
                                       String imagePath,
                                       String imageNameWithTag,
                                       String projectName,
                                       boolean autoPaaSDeploy,
                                       String namespace,
                                       String cluster,
                                       String appName,
                                       String containerName,
                                       String replicas,
                                       String limitCpu,
                                       String limitMemory,
                                       String strategy,
                                       String resourceKind,
                                       String nexusGroupId,
                                       String nexusArtifactId,
                                       String nexusArtifactVersion) {
        this.f2cEndpoint = f2cEndpoint;
        this.f2cAccessKey = f2cAccessKey;
        this.artifactType = StringUtils.isBlank(artifactType) ? ArtifactType.NEXUS : artifactType;
        this.repositorySettingId = repositorySettingId;
        this.f2cSecretKey = f2cSecretKey;
        this.applicationId = applicationId;
        this.clusterId = clusterId;
        this.clusterRoleId = clusterRoleId;
        this.workspaceId = workspaceId;
        this.cloudServerId = cloudServerId;
        this.applicationVersionName = applicationVersionName;
        this.paasApplicationVersionName = paasApplicationVersionName;
        this.deployPolicy = deployPolicy;
        this.deploymentLevel = deploymentLevel;
        this.backupQuantity = backupQuantity == null ? 0 : backupQuantity;
        this.autoDeploy = autoDeploy;
        this.includes = includes;
        this.excludes = excludes;
        this.appspecFilePath = StringUtils.isBlank(appspecFilePath) ? "appspec.yml" : appspecFilePath;
        this.description = description;
        this.pollingFreqSec = pollingFreqSec;
        this.pollingTimeoutSec = pollingTimeoutSec;
        this.waitForCompletion = waitForCompletion;
        this.objectPrefixAliyun = objectPrefixAliyun;
        this.objectPrefixAWS = objectPrefixAWS;
        this.path = path;
        this.nexusGroupId = nexusGroupId;
        this.nexusArtifactId = nexusArtifactId;
        this.nexusArtifactVersion = nexusArtifactVersion;
        this.nexusChecked = ArtifactType.NEXUS.equals(artifactType) ? true : false;
        this.artifactoryChecked = ArtifactType.ARTIFACTORY.equals(artifactType) ? true : false;
        this.ossChecked = ArtifactType.OSS.equals(artifactType) ? true : false;
        this.s3Checked = ArtifactType.S3.equals(artifactType) ? true : false;
        this.harborChecked = ArtifactType.HARBOR.equals(artifactType) ? true : false;
        this.applicationCommon = COMMON_APP.equals(applicationType);
        this.applicationContainer = CONTAINER_APP.equals(applicationType);
        this.applicationType = applicationType;
        this.imageNameWithTag = imageNameWithTag;
        this.imagePath = imagePath;
        this.projectName = projectName;
        this.cluster = cluster;
        this.namespace = namespace;
        this.appName = appName;
        this.containerName = containerName;
        this.autoPaaSDeploy = autoPaaSDeploy;
        this.replicas = replicas;
        this.limitCpu = limitCpu;
        this.limitMemory = limitMemory;
        this.strategy = strategy;
        this.resourceKind = resourceKind;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.STEP;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath filePath, @Nonnull Launcher launcher, @Nonnull TaskListener taskListener) throws InterruptedException, IOException {
        RunWrapper wrapper = new RunWrapper(run, true);
        boolean executeRes = execute(run, taskListener, wrapper.getProjectName(), filePath);
        if (!executeRes) {
            throw new InterruptedException("Interrupt to build deploy failure！");
        }
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        return execute(build, listener, build.getProject().getName(), build.getWorkspace());
    }

    private boolean execute(Run<?, ?> build, TaskListener listener, String projectName, FilePath workspace) {
        this.logger = listener.getLogger();
        int builtNumber = build.getNumber();
        final boolean buildFailed = build.getResult() == Result.FAILURE;
        if (buildFailed) {
            log("Build Failed, Skipping CodeDeploy publisher as build failed");
            return false;
        }
        final Fit2cloudClient fit2cloudClient = new Fit2cloudClient(this.f2cAccessKey, this.f2cSecretKey, this.f2cEndpoint);

        // 1.校验参数
        log("开始校验参数...");
        ParamCheck paramCheck = new ParamCheck(fit2cloudClient).invoke(this);
        if (paramCheck.isFailed()) {
            return false;
        }
        log("校验参数成功...");

        // 2.查询仓库
        RepositorySelect repositorySelect = new RepositorySelect(workspaceId, applicationId, fit2cloudClient).invoke(this);
        if (repositorySelect.failed()) {
            return false;
        }
        ApplicationRepository applicationRepository = repositorySelect.getApplicationRepository();
        ApplicationRepositorySetting repSetting = repositorySelect.getRepSetting();

        // 3.上传文件
        UploadFile uploadFile;
        try {
            uploadFile = new UploadFile(build, listener, projectName, workspace, builtNumber, applicationRepository, artifactType).invoke(this);
            if (uploadFile.failed()) {
                return false;
            }
        } catch (Exception e) {
            log("上传文件失败");
            return false;
        }

        String newAddress;
        if (this.isApplicationCommon()) {
            newAddress = uploadFile.getNewAddress();
        } else {
            newAddress = this.imageNameWithTag;
        }
        // 4. 注册应用版本和部署代码
        //这些参数被重新更新过
        String clusterId = paramCheck.getClusterId();
        String cloudServerId = paramCheck.getCloudServerId();
        String clusterRoleId = paramCheck.getClusterRoleId();
        String applicationId = paramCheck.getApplicationId();
        String workspaceId = paramCheck.getWorkspaceId();
        CodeDeploy codeDeploy = new CodeDeploy(build, listener, workspaceId, applicationId, clusterId, cloudServerId, clusterRoleId, fit2cloudClient, repSetting, newAddress).invoke(this);
        if (codeDeploy.failed()) {
            return false;
        }
        return codeDeploy.isDeployFlag();
    }

    /**
     * This indicates to Jenkins that this isFailed an implementation of an extension point.
     */
    @Symbol("fit2cloud")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            req.bindParameters(this);
            save();
            return super.configure(req, formData);
        }

        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            super(F2CCodeDeploySouthPublisher.class);
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        /**
         * This human readable name isFailed used in the configuration screen.
         */
        @Override
        public String getDisplayName() {
            return "FIT2CLOUD 代码部署 V2.0";
        }

        public FormValidation doCheckAccount(
                @QueryParameter String f2cAccessKey,
                @QueryParameter String f2cSecretKey,
                @QueryParameter String f2cEndpoint) {

            return ExtensionUtils.doCheckAccount(f2cAccessKey,
                    f2cSecretKey,
                    f2cEndpoint);
        }

        public ListBoxModel doFillWorkspaceIdItems(@QueryParameter String f2cAccessKey,
                                                   @QueryParameter String f2cSecretKey,
                                                   @QueryParameter String f2cEndpoint) {
            return ExtensionUtils.doFillWorkspaceIdItems(f2cAccessKey,
                    f2cSecretKey,
                    f2cEndpoint);
        }

        public ListBoxModel doFillApplicationIdItems(@QueryParameter String f2cAccessKey,
                                                     @QueryParameter String f2cSecretKey,
                                                     @QueryParameter String f2cEndpoint,
                                                     @QueryParameter String workspaceId) {
            return ExtensionUtils.doFillApplicationIdItems(f2cAccessKey,
                    f2cSecretKey,
                    f2cEndpoint,
                    workspaceId);
        }

        public ListBoxModel doFillRepositorySettingIdItems(@QueryParameter String f2cAccessKey,
                                                           @QueryParameter String f2cSecretKey,
                                                           @QueryParameter String f2cEndpoint,
                                                           @QueryParameter String workspaceId,
                                                           @QueryParameter String applicationId) {
            return ExtensionUtils.doFillRepositorySettingIdItems(f2cAccessKey,
                    f2cSecretKey,
                    f2cEndpoint,
                    workspaceId,
                    applicationId);
        }

        public ListBoxModel doFillClusterIdItems(@QueryParameter String f2cAccessKey,
                                                 @QueryParameter String f2cSecretKey,
                                                 @QueryParameter String f2cEndpoint,
                                                 @QueryParameter String workspaceId,
                                                 @QueryParameter String applicationId,
                                                 @QueryParameter String repositorySettingId) {
            return ExtensionUtils.doFillClusterIdItems(f2cAccessKey,
                    f2cSecretKey,
                    f2cEndpoint,
                    workspaceId,
                    applicationId,
                    repositorySettingId);
        }

        public ListBoxModel doFillClusterRoleIdItems(@QueryParameter String f2cAccessKey,
                                                     @QueryParameter String f2cSecretKey,
                                                     @QueryParameter String f2cEndpoint,
                                                     @QueryParameter String workspaceId,
                                                     @QueryParameter String clusterId) {
            return ExtensionUtils.doFillClusterRoleIdItems(f2cAccessKey,
                    f2cSecretKey,
                    f2cEndpoint,
                    workspaceId,
                    clusterId);
        }

        public ListBoxModel doFillCloudServerIdItems(@QueryParameter String f2cAccessKey,
                                                     @QueryParameter String f2cSecretKey,
                                                     @QueryParameter String f2cEndpoint,
                                                     @QueryParameter String workspaceId,
                                                     @QueryParameter String clusterId,
                                                     @QueryParameter String clusterRoleId) {
            return ExtensionUtils.doFillCloudServerIdItems(f2cAccessKey,
                    f2cSecretKey,
                    f2cEndpoint,
                    workspaceId,
                    clusterId,
                    clusterRoleId);
        }

        public ListBoxModel doFillDeployPolicyItems() {
            return ExtensionUtils.doFillDeployPolicyItems();
        }

        public ListBoxModel doFillDeploymentLevelItems() {
            return ExtensionUtils.doFillDeploymentLevelItems();
        }

        public ListBoxModel doFillBackupQuantityItems() {
            return ExtensionUtils.doFillBackupQuantityItems();
        }

        /**
         * PaaS 部署策略
         *
         * @return
         */
        public ListBoxModel doFillStrategyItems() {
            return ExtensionUtils.doFillStrategyItems();
        }

        /**
         * PaaS资源类型
         *
         * @return
         */
        public ListBoxModel doFillResourceKindItems() {
            return ExtensionUtils.doFillResourceKindItems();
        }

        /**
         * PaaS部署目标集群
         *
         * @return
         */
        public ListBoxModel doFillClusterItems(@QueryParameter String f2cAccessKey,
                                               @QueryParameter String f2cSecretKey,
                                               @QueryParameter String f2cEndpoint) {
            return ExtensionUtils.doFillClusterItems(f2cAccessKey, f2cSecretKey, f2cEndpoint);
        }

        /**
         * PaaS部署命名空间
         *
         * @return
         */
        public ListBoxModel doFillNamespaceItems(@QueryParameter String f2cAccessKey,
                                                 @QueryParameter String f2cSecretKey,
                                                 @QueryParameter String f2cEndpoint,
                                                 @QueryParameter String projectName,
                                                 @QueryParameter String cluster) {
            return ExtensionUtils.doFillNamespaceItems(f2cAccessKey, f2cSecretKey, f2cEndpoint, projectName, cluster);
        }

    }

    public String getF2cEndpoint() {
        return f2cEndpoint;
    }

    public String getF2cAccessKey() {
        return f2cAccessKey;
    }

    public String getF2cSecretKey() {
        return f2cSecretKey;
    }

    public String getApplicationRepositoryId() {
        return applicationRepositoryId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public boolean isNexusChecked() {
        return nexusChecked;
    }

    public boolean isOssChecked() {
        return ossChecked;
    }

    public boolean isS3Checked() {
        return s3Checked;
    }

    public boolean isAutoDeploy() {
        return autoDeploy;
    }

    public boolean isAutoPaaSDeploy() {
        return autoPaaSDeploy;
    }

    public boolean isArtifactoryChecked() {
        return artifactoryChecked;
    }

    public String getClusterId() {
        return clusterId;
    }

    public String getClusterRoleId() {
        return clusterRoleId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public String getCloudServerId() {
        return cloudServerId;
    }

    public String getDeployPolicy() {
        return deployPolicy;
    }

    public String getApplicationVersionName() {
        return applicationVersionName;
    }

    public String getIncludes() {
        return includes;
    }

    public String getExcludes() {
        return excludes;
    }

    public String getAppspecFilePath() {
        return appspecFilePath;
    }

    public String getDescription() {
        return description;
    }

    public boolean isWaitForCompletion() {
        return waitForCompletion;
    }

    public Long getPollingTimeoutSec() {
        return pollingTimeoutSec;
    }

    public Long getPollingFreqSec() {
        return pollingFreqSec;
    }

    public void log(String msg) {
        logger.println(LOG_PREFIX + msg);
    }

    public String getRepositorySettingId() {
        return repositorySettingId;
    }

    public String getApplicationSettingId() {
        return applicationSettingId;
    }

    public String getArtifactType() {
        return artifactType;
    }

    public String getObjectPrefixAliyun() {
        return objectPrefixAliyun;
    }

    public String getObjectPrefixAWS() {
        return objectPrefixAWS;
    }

    public String getPath() {
        return path;
    }

    public String getNexusGroupId() {
        return nexusGroupId;
    }

    public String getNexusArtifactId() {
        return nexusArtifactId;
    }

    public String getNexusArtifactVersion() {
        return nexusArtifactVersion;
    }

    public String getDeploymentLevel() {
        return deploymentLevel;
    }

    public Integer getBackupQuantity() {
        return backupQuantity;
    }

    public static String getLogPrefix() {
        return LOG_PREFIX;
    }

    public String getImageNameWithTag() {
        return imageNameWithTag;
    }

    public String getCluster() {
        return cluster;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getAppName() {
        return appName;
    }

    public String getContainerName() {
        return containerName;
    }

    public String getReplicas() {
        return replicas;
    }

    public String getLimitCpu() {
        return limitCpu;
    }

    public String getLimitMemory() {
        return limitMemory;
    }

    public String getStrategy() {
        return strategy;
    }

    public String getResourceKind() {
        return resourceKind;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public boolean isApplicationCommon() {
        return applicationCommon;
    }

    public boolean isApplicationContainer() {
        return applicationContainer;
    }

    public boolean isHarborChecked() {
        return harborChecked;
    }

    public String getPaasApplicationVersionName() {
        return paasApplicationVersionName;
    }
}
