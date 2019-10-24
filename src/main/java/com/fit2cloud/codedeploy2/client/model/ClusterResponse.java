package com.fit2cloud.codedeploy2.client.model;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author yankaijun
 * @date 2019-10-17 12:00
 */
public class ClusterResponse {

    private String[] resource_actions;
    @JSONField(
            name = "display_name"
    )
    private String displayName;
    private String name;
    @JSONField(
            name = "container_manager"
    )
    private String containerManager;
    @JSONField(
            name = "created_at"
    )
    private String createdAt;
    private String namespace;
    @JSONField(
            name = "updated_at"
    )
    private String updatedAt;
    @JSONField(
            name = "platform_version"
    )
    private String platformVersion;
    private String state;
    @JSONField(
            name = "env_uuid"
    )
    private String envUuid;
    private String type;
    private String id;
    private ClusterResponse.Features features;

    public ClusterResponse() {
    }

    public String[] getResource_actions() {
        return this.resource_actions;
    }

    public void setResource_actions(String[] resource_actions) {
        this.resource_actions = resource_actions;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContainerManager() {
        return this.containerManager;
    }

    public void setContainerManager(String containerManager) {
        this.containerManager = containerManager;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPlatformVersion() {
        return this.platformVersion;
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getEnvUuid() {
        return this.envUuid;
    }

    public void setEnvUuid(String envUuid) {
        this.envUuid = envUuid;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ClusterResponse.Features getFeatures() {
        return this.features;
    }

    public void setFeatures(ClusterResponse.Features features) {
        this.features = features;
    }

    class Log {
        private String es;

        Log() {
        }
    }

    class Customized {
        private ClusterResponse.Log log;

        Customized() {
        }
    }

    class Features {
        private ClusterResponse.Customized customized;

        Features() {
        }
    }
}
