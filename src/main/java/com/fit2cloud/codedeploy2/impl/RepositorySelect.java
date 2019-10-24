package com.fit2cloud.codedeploy2.impl;

import com.fit2cloud.codedeploy2.F2CCodeDeploySouthPublisher;
import com.fit2cloud.codedeploy2.client.Fit2cloudClient;
import com.fit2cloud.codedeploy2.client.model.ApplicationDTO;
import com.fit2cloud.codedeploy2.client.model.ApplicationRepository;
import com.fit2cloud.codedeploy2.client.model.ApplicationRepositorySetting;
import com.fit2cloud.codedeploy2.client.model.TagValue;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * @author yankaijun
 * @date 2019-10-16 17:18
 */
public class RepositorySelect {

    private boolean myResult;
    private String workspaceId;
    private String applicationId;
    private Fit2cloudClient fit2cloudClient;
    private ApplicationRepository applicationRepository;
    private ApplicationRepositorySetting repSetting;

    public RepositorySelect(String workspaceId, String applicationId, Fit2cloudClient fit2cloudClient) {
        this.workspaceId = workspaceId;
        this.applicationId = applicationId;
        this.fit2cloudClient = fit2cloudClient;
    }

    public boolean failed() {
        return myResult;
    }

    public ApplicationRepository getApplicationRepository() {
        return applicationRepository;
    }

    public ApplicationRepositorySetting getRepSetting() {
        return repSetting;
    }

    public RepositorySelect invoke(F2CCodeDeploySouthPublisher publisher) {
        applicationRepository = null;
        repSetting = null;
        ApplicationRepository rep = null;
        try {
            ApplicationDTO app = null;
            List<ApplicationDTO> applicationDTOS = fit2cloudClient.getApplications(workspaceId);
            for (ApplicationDTO applicationDTO : applicationDTOS) {
                if (applicationDTO.getId().equals(applicationId)) {
                    app = applicationDTO;
                }
            }
            if (app != null) {
                List<TagValue> envs = fit2cloudClient.getEnvList();
                List<ApplicationRepository> applicationRepositorys = fit2cloudClient.getApplicationRepositorys(workspaceId);

                for (ApplicationRepositorySetting setting : app.getApplicationRepositorySettings()) {
                    if (setting.getId().equals(publisher.getRepositorySettingId())) {
                        repSetting = setting;
                    }
                    ApplicationRepository repository = null;
                    for (ApplicationRepository appRepository : applicationRepositorys) {
                        if (appRepository.getId().equals(setting.getRepositoryId())) {
                            repository = appRepository;
                        }
                    }
                    String envName = null;
                    for (TagValue env : envs) {
                        if (env.getId().equals(setting.getEnvId())) {
                            envName = env.getTagValueAlias();
                        }
                        if ("ALL".equalsIgnoreCase(setting.getEnvId())) {
                            envName = "全部环境";
                        }
                    }
                    if (null != repository) {
                        if ((envName + "---" + repository.getType()).equalsIgnoreCase(publisher.getRepositorySettingId())) {
                            repSetting = setting;
                            break;
                        }
                    }
                }
            }
            if (repSetting != null) {
                List<ApplicationRepository> repositories = fit2cloudClient.getApplicationRepositorys(workspaceId);
                for (ApplicationRepository re : repositories) {
                    if (re.getId().equals(repSetting.getRepositoryId())) {
                        rep = re;
                    }
                    if (StringUtils.isNotBlank(re.getName())) {
                        if (re.getName().equals(repSetting.getRepositoryId())) {
                            rep = re;
                        }
                    }
                }
            }

            if (rep != null) {
                applicationRepository = rep;
            }

            String repoType = applicationRepository.getType();
            //界面上选择的仓库和cmp中保存的仓库进行对比
            if (!publisher.getArtifactType().equalsIgnoreCase(repoType)) {
                publisher.log("所选仓库与 \"Zip文件上传设置\"中的类型设置不匹配!");
                myResult = true;
                return this;
            }

        } catch (Exception e) {
            publisher.log("加载仓库失败！" + e.getMessage());
            myResult = true;
            return this;
        }
        myResult = false;
        return this;
    }
}
