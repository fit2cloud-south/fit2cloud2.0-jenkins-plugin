package com.fit2cloud.codedeploy2.client.model;


import java.io.Serializable;

public class User implements Serializable {
    private String id;

    private String name;

    private String email;

    private String source;

    private String password;

    private Boolean active;

    private String phone;

    private Long createTime;

    private String lastSourceId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table user
     *
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column user.id
     *
     * @return the value of user.id
     *
     */
    public String getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column user.id
     *
     * @param id the value for user.id
     *
     */
    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column user.name
     *
     * @return the value of user.name
     *
     */
    public String getName() {
        return name;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column user.name
     *
     * @param name the value for user.name
     *
     */
    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column user.email
     *
     * @return the value of user.email
     *
     */
    public String getEmail() {
        return email;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column user.email
     *
     * @param email the value for user.email
     *
     */
    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column user.source
     *
     * @return the value of user.source
     *
     */
    public String getSource() {
        return source;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column user.source
     *
     * @param source the value for user.source
     *
     */
    public void setSource(String source) {
        this.source = source == null ? null : source.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column user.password
     *
     * @return the value of user.password
     *
     */
    public String getPassword() {
        return password;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column user.password
     *
     * @param password the value for user.password
     *
     */
    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column user.active
     *
     * @return the value of user.active
     *
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column user.active
     *
     * @param active the value for user.active
     *
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column user.phone
     *
     * @return the value of user.phone
     *
     */
    public String getPhone() {
        return phone;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column user.phone
     *
     * @param phone the value for user.phone
     *
     */
    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column user.create_time
     *
     * @return the value of user.create_time
     *
     */
    public Long getCreateTime() {
        return createTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column user.create_time
     *
     * @param createTime the value for user.create_time
     *
     */
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column user.last_source_id
     *
     * @return the value of user.last_source_id
     *
     */
    public String getLastSourceId() {
        return lastSourceId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column user.last_source_id
     *
     * @param lastSourceId the value for user.last_source_id
     *
     */
    public void setLastSourceId(String lastSourceId) {
        this.lastSourceId = lastSourceId == null ? null : lastSourceId.trim();
    }
}
