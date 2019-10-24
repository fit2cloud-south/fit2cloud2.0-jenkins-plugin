package com.fit2cloud.codedeploy2.oss;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;
import java.util.Map;

public class HarborService {

    public static RestTemplate getRestTemplate() {
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(3000);
        httpRequestFactory.setConnectTimeout(3000);
        httpRequestFactory.setReadTimeout(3000);
        return new RestTemplate(httpRequestFactory);
    }

    public static String getHarborCookie(String url, String accessKey, String secretKey) {
        url = url + "/c/login?principal=" + accessKey + "&password=" + secretKey;
        RestTemplate restTemplate = getRestTemplate();
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Accept", "application/json");
        HttpEntity<String> requestEntity = new HttpEntity<>(null, requestHeaders);
        try {
            ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            HttpHeaders headers = result.getHeaders();
            List<String> stringList = headers.get("Set-Cookie");
            if (CollectionUtils.isNotEmpty(stringList)) {
                for (int i = 0; i < stringList.size(); i++) {
                    String str = stringList.get(0);
                    String[] setCookies = str.split(";");
                    if (setCookies[0].startsWith("sid=")) {
                        return setCookies[0];
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

//    public static JSONArray getHarborProjects(Map<String, Object> map) {
//        String url = (String) map.get("url");
//        String accessId = (String) map.get("accessId");
//        String accessPassword = (String) map.get("accessPassword");
//        String harborCookie = getHarborCookie(url, accessId, accessPassword);
//        url = url + "/api/projects?page=1&page_size=150000";
//        RestTemplate restTemplate = getRestTemplate();
//        HttpHeaders requestHeaders = new HttpHeaders();
//        requestHeaders.add("Accept", "application/json");
//        requestHeaders.add("Cookie", harborCookie);
//        JSONObject jsonObject = new JSONObject();
//        HttpEntity<Object> requestEntity = new HttpEntity<>(jsonObject, requestHeaders);
//        ResponseEntity<String> isFailed = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
//        // System.out.println(isFailed.getBody());
//        return JSONArray.parseArray(isFailed.getBody());
//    }
//
//    // https://registry.fit2cloud.com/api/projects/43
//    // https://registry.fit2cloud.com/api/repositories?page=1&page_size=15&project_id=43
//    public static JSONArray getHarborImages(Map<String, Object> map) {
//        String url = (String) map.get("url");
//        String accessId = (String) map.get("accessId");
//        String accessPassword = (String) map.get("accessPassword");
//        String projectId = (String) map.get("projectId");
//        String harborCookie = getHarborCookie(url, accessId, accessPassword);
//        url = url + "/api/repositories?page=1&page_size=1500000&project_id=" + projectId;
//        RestTemplate restTemplate = getRestTemplate();
//        HttpHeaders requestHeaders = new HttpHeaders();
//        requestHeaders.add("Accept", "application/json");
//        requestHeaders.add("Cookie", harborCookie);
//        JSONObject jsonObject = new JSONObject();
//        HttpEntity<Object> requestEntity = new HttpEntity<>(jsonObject, requestHeaders);
//        ResponseEntity<String> isFailed = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
//        return JSONArray.parseArray(isFailed.getBody());
//    }
//
//    // https://registry.fit2cloud.com/api/repositories/north/devops/devops/tags?page=1&page_size=15000
//
//    public static JSONArray getImageTags(Map<String, Object> map) {
//        String url = (String) map.get("url");
//        String accessId = (String) map.get("accessId");
//        String accessPassword = (String) map.get("accessPassword");
//        // String projectId = (String) map.get("projectId");
//        String imageName = (String) map.get("imageName");
//        String harborCookie = getHarborCookie(url, accessId, accessPassword);
//        url = url + "/api/repositories/" + imageName + "/tags?page=1&page_size=1500000";
//        RestTemplate restTemplate = getRestTemplate();
//        HttpHeaders requestHeaders = new HttpHeaders();
//        requestHeaders.add("Accept", "application/json");
//        requestHeaders.add("Cookie", harborCookie);
//        JSONObject jsonObject = new JSONObject();
//        HttpEntity<Object> requestEntity = new HttpEntity<>(jsonObject, requestHeaders);
//        ResponseEntity<String> isFailed = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
//        return JSONArray.parseArray(isFailed.getBody());
//    }

    public static void upload(Run<?, ?> build, FilePath workspace, TaskListener listener, String accessId, String accessPassword, Object o, String repository, String expFPAws, String expVPAws, File zipFile) {
        //TODO 调用docker push ？？
    }
}
