package com.fit2cloud.codedeploy2.impl;

import com.fit2cloud.codedeploy2.F2CCodeDeploySouthPublisher;
import com.fit2cloud.codedeploy2.client.Fit2cloudClient;
import com.fit2cloud.codedeploy2.client.model.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * @author yankaijun
 * @date 2019-10-16 17:03
 */
public class ParamCheck {

    private boolean myResult;
    private Fit2cloudClient fit2cloudClient;

    private String clusterId;
    private String cloudServerId;
    private String clusterRoleId;
    private String workspaceId;
    private String applicationId;

    public ParamCheck(Fit2cloudClient fit2cloudClient) {
        this.fit2cloudClient = fit2cloudClient;
    }

    public boolean isFailed() {
        return myResult;
    }

    public String getClusterId() {
        return clusterId;
    }

    public String getCloudServerId() {
        return cloudServerId;
    }

    public String getClusterRoleId() {
        return clusterRoleId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public ParamCheck invoke(F2CCodeDeploySouthPublisher publisher) {
        try {
            boolean findWorkspace = false;
            List<Workspace> workspaces = fit2cloudClient.getWorkspace();
            String tmpWorkspaceId = publisher.getWorkspaceId();
            for (Workspace wk : workspaces) {
                if (wk.getId().equals(publisher.getWorkspaceId())) {
                    findWorkspace = true;
                    tmpWorkspaceId = wk.getId();
                }
                if (StringUtils.isNotBlank(wk.getName())) {
                    if (wk.getName().equals(publisher.getWorkspaceId())) {
                        findWorkspace = true;
                        tmpWorkspaceId = wk.getId();
                    }
                }
            }
            workspaceId = tmpWorkspaceId;
            if (!findWorkspace) {
                throw new CodeDeployException("工作空间不存在！");
            }

            boolean findApplication = false;
            String tmpApplicationId = publisher.getApplicationId();
            List<ApplicationDTO> applications = fit2cloudClient.getApplications(workspaceId);
            for (ApplicationDTO applicationDTO : applications) {
                if (applicationDTO.getId().equals(publisher.getApplicationId())) {
                    findApplication = true;
                    tmpApplicationId = applicationDTO.getId();
                }
                if (StringUtils.isNotBlank(applicationDTO.getName())) {
                    if (applicationDTO.getName().equals(publisher.getApplicationId())) {
                        findApplication = true;
                        tmpApplicationId = applicationDTO.getId();
                    }
                }
            }

            applicationId = tmpApplicationId;
            if (!findApplication) {
                throw new CodeDeployException("应用不存在！");
            }

            //普通应用参数校验
            if (publisher.isAutoDeploy() && publisher.isApplicationCommon()) {
                boolean findCluster = false;
                List<ClusterDTO> clusters = fit2cloudClient.getClusters(workspaceId);
                String tmpClusterId = publisher.getCluster();
                if (CollectionUtils.isNotEmpty(clusters)) {
                    for (ClusterDTO clusterDTO : clusters) {
                        if (clusterDTO.getId().equals(publisher.getClusterId())) {
                            findCluster = true;
                            tmpClusterId = clusterDTO.getId();
                        }
                        if (StringUtils.isNotBlank(clusterDTO.getName())) {
                            if (clusterDTO.getName().equals(publisher.getClusterId())) {
                                findCluster = true;
                                tmpClusterId = clusterDTO.getId();
                            }
                        }
                    }
                }
                if (!findCluster) {
                    throw new CodeDeployException("集群不存在! ");
                }
                clusterId = tmpClusterId;

                List<ClusterRole> clusterRoles = fit2cloudClient.getClusterRoles(workspaceId, clusterId);
                if (clusterRoles.size() == 0) {
                    throw new CodeDeployException("此集群下主机组为空！");
                }

                String tmpClusterRoleId = publisher.getClusterRoleId();
                if (!"ALL".equalsIgnoreCase(publisher.getClusterRoleId())) {
                    boolean findClusterRole = false;
                    for (ClusterRole clusterRole : clusterRoles) {
                        if (clusterRole.getId().equals(publisher.getClusterRoleId())) {
                            findClusterRole = true;
                            tmpClusterRoleId = clusterRole.getId();
                        }
                        if (StringUtils.isNotBlank(clusterRole.getName())) {
                            if (clusterRole.getName().equals(publisher.getClusterRoleId())) {
                                findClusterRole = true;
                                tmpClusterRoleId = clusterRole.getId();
                            }
                        }
                    }
                    if (!findClusterRole) {
                        throw new CodeDeployException("主机组不存在! ");
                    }
                }
                clusterRoleId = tmpClusterRoleId;

                List<CloudServer> cloudServers = fit2cloudClient.getCloudServers(workspaceId, clusterRoleId, clusterId);
                if (cloudServers.size() == 0) {
                    throw new CodeDeployException("此主机组下主机为空！");
                }
                String tmpCloudServerId = publisher.getCloudServerId();
                if (!"ALL".equalsIgnoreCase(publisher.getCloudServerId())) {
                    boolean findCLoudServer = false;
                    for (CloudServer cloudServer : cloudServers) {
                        if (cloudServer.getId().equals(publisher.getCloudServerId())) {
                            findCLoudServer = true;
                            tmpCloudServerId = cloudServer.getId();
                        }
                        if (StringUtils.isNotBlank(cloudServer.getInstanceName())) {
                            if (cloudServer.getInstanceName().equals(publisher.getCloudServerId())) {
                                findCLoudServer = true;
                                tmpCloudServerId = cloudServer.getId();
                            }
                        }
                    }
                    if (!findCLoudServer) {
                        throw new CodeDeployException("主机组不存在! ");
                    }
                }

                if (StringUtils.isBlank(publisher.getDeploymentLevel())) {
                    throw new CodeDeployException("部署级别不可为空");
                }
                cloudServerId = tmpCloudServerId;
            }

            //容器应用参数校验
            if (publisher.isAutoPaaSDeploy() && publisher.isApplicationContainer()) {
                if (StringUtils.isBlank(publisher.getCluster())) {
                    throw new CodeDeployException("PaaS 目标集群不可为空");
                }
                if (StringUtils.isBlank(publisher.getNamespace())) {
                    throw new CodeDeployException("PaaS 命名空间不可为空");
                }
                if (StringUtils.isBlank(publisher.getAppName())) {
                    throw new CodeDeployException("PaaS APP名称不可为空");
                }
                if (StringUtils.isBlank(publisher.getContainerName())) {
                    throw new CodeDeployException("容器名称不可为空");
                }

                try {
                    Integer.valueOf(publisher.getReplicas());
                    Integer.valueOf(publisher.getLimitCpu());
                    Integer.valueOf(publisher.getLimitMemory());
                } catch (NumberFormatException e) {
                    throw new CodeDeployException("PaaS 副本数量/cpu/内存 需要为数值");
                }

                if (StringUtils.isBlank(publisher.getStrategy())) {
                    throw new CodeDeployException("PaaS 部署策略不可为空");
                }

                if (StringUtils.isBlank(publisher.getResourceKind())) {
                    throw new CodeDeployException("PaaS 资源类型不可为空");
                }
            }
        } catch (Exception e) {
            publisher.log("存在参数为空或获取工作空间|集群|应用异常：" + e.getMessage());
            myResult = true;
            return this;
        }
        myResult = false;
        return this;
    }
}
