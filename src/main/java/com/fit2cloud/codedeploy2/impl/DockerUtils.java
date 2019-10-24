package com.fit2cloud.codedeploy2.impl;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * @author yankaijun
 * @date 2019-10-17 14:40
 */
public class DockerUtils {

    public static String commonExecCmdWithResult(String command, String workDir) throws Exception {
        StringBuffer stringBuffer = new StringBuffer();
        Process exec;
        if (StringUtils.isNotBlank(workDir)) {
            exec = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command}, null, new File(workDir));
        } else {
            exec = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
        }
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(exec.getErrorStream()))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuffer.append(line + "\n");
            }
            exec.waitFor();
            if (exec.exitValue() != 0) {
                while ((line = errorReader.readLine()) != null) {
                    stringBuffer.append(line);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return stringBuffer.toString();
    }

}
