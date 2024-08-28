/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.itpf.security.sso.ejb.sysinit;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.iplanet.am.util.SystemProperties;

@Singleton
@Startup
public class ServiceBootstrap {

    @Inject
    private Logger logger;

    private String ssoInstanceName = "heimdallr";
    private String ssoNamingService = "namingservice";
    private String ssoAdmin = "amadmin";
    private String ssoAdminPwd = "h31md477R";

    private static final String GLOBAL_CONFIG = "/ericsson/tor/data/global.properties";
    private boolean onCloud;

    private String ssoAccessPoint;
    private String namingServiceUrl;
    private String ssoInstanceUrl;
    private String uiPresServer;

    private List<String> ssoInstancesUrls = new ArrayList<>();

    @PostConstruct
    void onServiceStart() {
        initAttributes();
        setSystemProperties();
        logger.info("Starting sso-utilities service");
        logger.info("SSO access point: {}", ssoAccessPoint);
        logger.info("Naming service url: {}", namingServiceUrl);
        logger.info("SSO url: {}", ssoInstanceUrl);
        logger.info("SSO instances: {}", ssoInstancesUrls);
    }

    private void initAttributes() {
        onCloud = "true".equalsIgnoreCase(getGlobalProperty("DDC_ON_CLOUD"));
        uiPresServer = getGlobalProperty("UI_PRES_SERVER");
        ssoAccessPoint = "sso." + uiPresServer;
        namingServiceUrl = String.format("http://%s:8080/%s/%s", ssoAccessPoint, ssoInstanceName, ssoNamingService);
        ssoInstanceUrl = String.format("http://%s:8080/%s", ssoAccessPoint, ssoInstanceName);
        ssoInstancesUrls = getSsoUrlsOnPhysicall();
    }

    private void setSystemProperties() {
        final Properties props = new Properties();
        props.setProperty("com.iplanet.am.naming.url", namingServiceUrl);
        props.setProperty("com.sun.identity.agents.app.username", ssoAdmin);
        props.setProperty("com.iplanet.am.service.password", ssoAdminPwd);
        props.setProperty("com.iplanet.am.services.deploymentDescriptor", "/" + ssoInstanceName);
        SystemProperties.initializeProperties(props);
    }

    public String getSsoAdminUsername() {
        return ssoAdmin;
    }

    public String getSsoAdminPwd() {
        return ssoAdminPwd;
    }

    public String getSsoInstanceUrl() {
        return ssoInstanceUrl;
    }

    public List<String> getSsoInstancesUrls() {
        if (onCloud) {
            return getSsoUrlsOnCloud();
        } else {
            return ssoInstancesUrls;
        }
    }

    @PreDestroy
    void onServiceStop() {
        logger.info("Stopping sso-utilities service");
    }

    private List<String> getSsoUrlsOnCloud() {
        final List<String> urls = new ArrayList<>();
        try {
            for (final InetAddress address : InetAddress.getAllByName("sso")) {
                urls.add("http://" + address.getHostAddress() + ":8080/" + ssoInstanceName);
            }
        } catch (final UnknownHostException e) {
            logger.warn("Failed to get IPs of sso", e);
        }
        return urls;
    }

    private List<String> getSsoUrlsOnPhysicall() {
        final List<String> urls = new ArrayList<>();
        final String instances = getGlobalProperty("sso_instances");
        if (instances != null) {
            for (final String instance : instances.split(",")) {
                urls.add("http://" + instance + "." + uiPresServer + ":8080/" + ssoInstanceName);
            }
        }
        return urls;
    }

    private String getGlobalProperty(final String str) {
        String property = null;
        try (BufferedReader reader = getBufferedReader()) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.contains(str + "=")) {
                    property = line.substring(line.indexOf("=") + 1);
                    return property;
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return property;
    }

    BufferedReader getBufferedReader() throws FileNotFoundException {
        return new BufferedReader(new FileReader(GLOBAL_CONFIG));
    }

    void setLogger(final Logger logger) {
        this.logger = logger;
    }

}