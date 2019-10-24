package com.fit2cloud.codedeploy2.client.model;

/**
 * @author yankaijun
 * @date 2019-10-17 12:00
 */
public class PaaSNamespace {

    private String id;
    private String name;

    public String getId() {
        return id;
    }

    public PaaSNamespace setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public PaaSNamespace setName(String name) {
        this.name = name;
        return this;
    }
}
