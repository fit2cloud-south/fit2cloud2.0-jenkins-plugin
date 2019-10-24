package com.fit2cloud.codedeploy2.client.model;

import java.io.Serializable;
import java.util.Map;

/**
 * @author yankaijun
 * @date 2019-10-11 15:22
 */
public class PaaSDeployRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer replicas;
    private String appName;
    private String appDisplayName;
    private String containerName;
    private String image;
    private String limitCpu;
    private String limitMemory;
    private String strategy;
    private Map<String, String> labels;
    private String namespace;
    private String projectName;
    private String clusterName;
    private String description;
    private String resourceKind;
    private String deploymentLogId;
    private String applicationVersionId;

    public String getApplicationVersionId() {
        return applicationVersionId;
    }

    public PaaSDeployRequest setApplicationVersionId(String applicationVersionId) {
        this.applicationVersionId = applicationVersionId;
        return this;
    }

    public String getDeploymentLogId() {
        return deploymentLogId;
    }

    public PaaSDeployRequest setDeploymentLogId(String deploymentLogId) {
        this.deploymentLogId = deploymentLogId;
        return this;
    }

    public String getResourceKind() {
        return resourceKind;
    }

    public PaaSDeployRequest setResourceKind(String resourceKind) {
        this.resourceKind = resourceKind;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PaaSDeployRequest setDescription(String description) {
        this.description = description;
        return this;
    }

    public Integer getReplicas() {
        return replicas;
    }

    public PaaSDeployRequest setReplicas(Integer replicas) {
        this.replicas = replicas;
        return this;
    }

    public String getAppName() {
        return appName;
    }

    public PaaSDeployRequest setAppName(String appName) {
        this.appName = appName;
        return this;
    }

    public String getAppDisplayName() {
        return appDisplayName;
    }

    public PaaSDeployRequest setAppDisplayName(String appDisplayName) {
        this.appDisplayName = appDisplayName;
        return this;
    }

    public String getContainerName() {
        return containerName;
    }

    public PaaSDeployRequest setContainerName(String containerName) {
        this.containerName = containerName;
        return this;
    }

    public String getImage() {
        return image;
    }

    public PaaSDeployRequest setImage(String image) {
        this.image = image;
        return this;
    }

    public String getLimitCpu() {
        return limitCpu;
    }

    public PaaSDeployRequest setLimitCpu(String limitCpu) {
        this.limitCpu = limitCpu;
        return this;
    }

    public String getLimitMemory() {
        return limitMemory;
    }

    public PaaSDeployRequest setLimitMemory(String limitMemory) {
        this.limitMemory = limitMemory;
        return this;
    }

    public String getStrategy() {
        return strategy;
    }

    public PaaSDeployRequest setStrategy(String strategy) {
        this.strategy = strategy;
        return this;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public PaaSDeployRequest setLabels(Map<String, String> labels) {
        this.labels = labels;
        return this;
    }

    public String getNamespace() {
        return namespace;
    }

    public PaaSDeployRequest setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public String getProjectName() {
        return projectName;
    }

    public PaaSDeployRequest setProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public String getClusterName() {
        return clusterName;
    }

    public PaaSDeployRequest setClusterName(String clusterName) {
        this.clusterName = clusterName;
        return this;
    }

}
