package com.fit2cloud.codedeploy2.impl;

import com.fit2cloud.codedeploy2.client.Fit2cloudClient;
import com.fit2cloud.codedeploy2.client.model.*;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yankaijun
 * @date 2019-10-16 17:36
 */
public class ExtensionUtils {

    public static FormValidation doCheckAccount(
            @QueryParameter String f2cAccessKey,
            @QueryParameter String f2cSecretKey,
            @QueryParameter String f2cEndpoint) {
        if (StringUtils.isEmpty(f2cAccessKey)) {
            return FormValidation.error("FIT2CLOUD ConsumerKey不能为空！");
        }
        if (StringUtils.isEmpty(f2cSecretKey)) {
            return FormValidation.error("FIT2CLOUD SecretKey不能为空！");
        }
        if (StringUtils.isEmpty(f2cEndpoint)) {
            return FormValidation.error("FIT2CLOUD EndPoint不能为空！");
        }
        try {
            Fit2cloudClient fit2cloudClient = new Fit2cloudClient(f2cAccessKey, f2cSecretKey, f2cEndpoint);
            fit2cloudClient.checkUser();
        } catch (Exception e) {
            return FormValidation.error(e.getMessage());
        }
        return FormValidation.ok("验证FIT2CLOUD帐号成功！");
    }

    public static ListBoxModel doFillWorkspaceIdItems(@QueryParameter String f2cAccessKey,
                                                      @QueryParameter String f2cSecretKey,
                                                      @QueryParameter String f2cEndpoint) {
        ListBoxModel items = new ListBoxModel();
        items.add("请选择工作空间", "");
        try {
            Fit2cloudClient fit2CloudClient = new Fit2cloudClient(f2cAccessKey, f2cSecretKey, f2cEndpoint);
            List<Workspace> list = fit2CloudClient.getWorkspace();
            if (list != null && list.size() > 0) {
                for (Workspace c : list) {
                    items.add(c.getName(), String.valueOf(c.getId()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    public static ListBoxModel doFillApplicationIdItems(@QueryParameter String f2cAccessKey,
                                                        @QueryParameter String f2cSecretKey,
                                                        @QueryParameter String f2cEndpoint,
                                                        @QueryParameter String workspaceId) {
        ListBoxModel items = new ListBoxModel();
        try {
            List<ApplicationDTO> list = new ArrayList<>();
            items.add("请选择应用", "");
            Fit2cloudClient fit2CloudClient = new Fit2cloudClient(f2cAccessKey, f2cSecretKey, f2cEndpoint);
            if (workspaceId != null && !workspaceId.equals("")) {
                list = fit2CloudClient.getApplications(workspaceId);
            }
            if (list != null && list.size() > 0) {
                for (Application c : list) {
                    items.add(c.getName(), String.valueOf(c.getId()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    public static ListBoxModel doFillRepositorySettingIdItems(@QueryParameter String f2cAccessKey,
                                                              @QueryParameter String f2cSecretKey,
                                                              @QueryParameter String f2cEndpoint,
                                                              @QueryParameter String workspaceId,
                                                              @QueryParameter String applicationId) {
        ListBoxModel items = new ListBoxModel();
        try {
            Fit2cloudClient fit2CloudClient = new Fit2cloudClient(f2cAccessKey, f2cSecretKey, f2cEndpoint);
            items.add("请选择环境", "");
            List<ApplicationDTO> applicationDTOS = fit2CloudClient.getApplications(workspaceId);

            ApplicationDTO application = null;

            for (ApplicationDTO applicationDTO : applicationDTOS) {
                if (applicationDTO.getId().equals(applicationId)) {
                    application = applicationDTO;
                }
            }

            //assert application != null;
            if (application == null) {
                return items;
            }
            List<ApplicationRepositorySetting> list = application.getApplicationRepositorySettings();
            List<ApplicationRepository> applicationRepositories = fit2CloudClient.getApplicationRepositorys(workspaceId);
            List<TagValue> envs = fit2CloudClient.getEnvList();

            if (list != null && list.size() > 0) {
                for (ApplicationRepositorySetting c : list) {
                    ApplicationRepository repository = null;
                    for (ApplicationRepository applicationRepository : applicationRepositories) {
                        if (applicationRepository.getId().equals(c.getRepositoryId())) {
                            repository = applicationRepository;
                        }
                    }
                    String envName = null;
                    for (TagValue env : envs) {
                        if (env.getId().equals(c.getEnvId())) {
                            envName = env.getTagValueAlias();
                        }
                        if (c.getEnvId().equalsIgnoreCase("ALL")) {
                            envName = "全部环境";
                        }
                    }

                    //assert repository != null;
                    if (repository == null) {
                        return items;
                    }
                    items.add(envName + "---" + repository.getType(), String.valueOf(c.getId()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    public static ListBoxModel doFillClusterIdItems(@QueryParameter String f2cAccessKey,
                                                    @QueryParameter String f2cSecretKey,
                                                    @QueryParameter String f2cEndpoint,
                                                    @QueryParameter String workspaceId,
                                                    @QueryParameter String applicationId,
                                                    @QueryParameter String repositorySettingId) {
        ListBoxModel items = new ListBoxModel();
        items.add("请选择集群", "");

        try {
            Fit2cloudClient fit2CloudClient = new Fit2cloudClient(f2cAccessKey, f2cSecretKey, f2cEndpoint);
            List<ClusterDTO> list = fit2CloudClient.getClusters(workspaceId);

            final Fit2cloudClient fit2cloudClient = new Fit2cloudClient(f2cAccessKey, f2cSecretKey, f2cEndpoint);
            ApplicationDTO applicationDTO = null;
            List<ApplicationDTO> applications = fit2cloudClient.getApplications(workspaceId);
            for (ApplicationDTO app : applications) {
                if (app.getId().equalsIgnoreCase(applicationId)) {
                    applicationDTO = app;
                }
            }
            if (applicationDTO == null) {
                return items;
            }
            ApplicationRepositorySetting applicationRepositorySetting = null;
            List<ApplicationRepositorySetting> repositorySettings = applicationDTO.getApplicationRepositorySettings();
            for (ApplicationRepositorySetting appst : repositorySettings) {
                if (appst.getId().equalsIgnoreCase(repositorySettingId)) {
                    applicationRepositorySetting = appst;
                }
            }
            if (applicationRepositorySetting == null) {
                return items;
            }
            String envValueId = applicationRepositorySetting.getEnvId();
            String businessValueId = applicationDTO.getBusinessValueId();

            if (list != null && list.size() > 0) {
                for (ClusterDTO c : list) {
                    if ((businessValueId == null || businessValueId.equalsIgnoreCase(c.getSystemValueId()))
                            && (envValueId.equalsIgnoreCase("ALL") || envValueId.equalsIgnoreCase(c.getEnvValueId()))) {
                        items.add(c.getName(), String.valueOf(c.getId()));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    public static ListBoxModel doFillClusterRoleIdItems(@QueryParameter String f2cAccessKey,
                                                        @QueryParameter String f2cSecretKey,
                                                        @QueryParameter String f2cEndpoint,
                                                        @QueryParameter String workspaceId,
                                                        @QueryParameter String clusterId) {
        ListBoxModel items = new ListBoxModel();
        items.add("请选择主机组", "");

        try {
            Fit2cloudClient fit2CloudClient = new Fit2cloudClient(f2cAccessKey, f2cSecretKey, f2cEndpoint);
            List<ClusterRole> list = fit2CloudClient.getClusterRoles(workspaceId, clusterId);
            if (list != null && list.size() > 0) {
                items.add("全部主机组", "ALL");
                for (ClusterRole c : list) {
                    items.add(c.getName(), String.valueOf(c.getId()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    public static ListBoxModel doFillCloudServerIdItems(@QueryParameter String f2cAccessKey,
                                                        @QueryParameter String f2cSecretKey,
                                                        @QueryParameter String f2cEndpoint,
                                                        @QueryParameter String workspaceId,
                                                        @QueryParameter String clusterId,
                                                        @QueryParameter String clusterRoleId) {
        ListBoxModel items = new ListBoxModel();
        items.add("请选择主机", "");
        try {
            Fit2cloudClient fit2CloudClient = new Fit2cloudClient(f2cAccessKey, f2cSecretKey, f2cEndpoint);
            List<CloudServer> list = fit2CloudClient.getCloudServers(workspaceId, clusterRoleId, clusterId);
            if (list != null && list.size() > 0) {
                items.add("全部主机", "ALL");
                for (CloudServer c : list) {
                    items.add(c.getInstanceName(), String.valueOf(c.getId()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    public static ListBoxModel doFillDeployPolicyItems() {
        ListBoxModel items = new ListBoxModel();
        items.add("全部同时部署", "all");
        items.add("半数分批部署", "harf");
        items.add("单台依次部署", "sigle");
        return items;
    }

    public static ListBoxModel doFillDeploymentLevelItems() {
        ListBoxModel items = new ListBoxModel();
        items.add("全量部署", "all");
        items.add("增量部署", "increment");
        return items;
    }

    public static ListBoxModel doFillBackupQuantityItems() {
        ListBoxModel items = new ListBoxModel();
        items.add("全部同时部署", "all");
        items.add("半数分批部署", "harf");
        items.add("单台依次部署", "sigle");
        return items;
    }

    public static ListBoxModel doFillResourceKindItems() {
        ListBoxModel items = new ListBoxModel();
        items.add("Deployment", "Deployment");
        items.add("StatefulSet", "StatefulSet");
        return items;
    }

    public static ListBoxModel doFillStrategyItems() {
        ListBoxModel items = new ListBoxModel();
        items.add("RollingUpdate", "RollingUpdate");
        return items;
    }

    public static ListBoxModel doFillClusterItems(@QueryParameter String f2cAccessKey,
                                                  @QueryParameter String f2cSecretKey,
                                                  @QueryParameter String f2cEndpoint) {
        ListBoxModel items = new ListBoxModel();
        items.add("请选择目标集群", "");
        try {
            Fit2cloudClient fit2CloudClient = new Fit2cloudClient(f2cAccessKey, f2cSecretKey, f2cEndpoint);
            List<ClusterResponse> list = fit2CloudClient.getPaaSCluster();
            for (ClusterResponse response : list) {
                items.add(response.getName(), response.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    public static ListBoxModel doFillNamespaceItems(@QueryParameter String f2cAccessKey,
                                                    @QueryParameter String f2cSecretKey,
                                                    @QueryParameter String f2cEndpoint,
                                                    @QueryParameter String projectName,
                                                    @QueryParameter String cluster) {
        ListBoxModel items = new ListBoxModel();
        items.add("请选择命名空间", "");
        if (StringUtils.isBlank(projectName) || StringUtils.isBlank(cluster)) {
            return items;
        }
        try {
            Fit2cloudClient fit2CloudClient = new Fit2cloudClient(f2cAccessKey, f2cSecretKey, f2cEndpoint);
            List<PaaSNamespace> list = fit2CloudClient.getPaaSNamespace(projectName, cluster);
            for (PaaSNamespace response : list) {
                items.add(response.getName(), response.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }
}
