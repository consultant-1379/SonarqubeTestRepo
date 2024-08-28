/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.itpf.security.sso.pib;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;

@Stateless
public class ConfigurationUpdater {

    private static final String PATHUPDATECONFIG = "/pib/configurationService/updateConfigParameterValue";
    private static String host;
    private static final int PORT = 8080;
    private static final String SCHEME = "http";
    private static final String PARAMNAME = "paramName";
    private static final String PARAMVALUE = "paramValue";
    private static final String MAXSESSIONTIMEOUTPARAMNAME = "maxSessionTimeout";
    private static final String IDLESESSIONTIMEOUTPARAMNAME = "idleSessionTimeout";
    private static final String SESSIONCONFIGURATIONTIMESTAMP = "sessionConfigurationTimestamp";
    private static final String PIB_PASSWORD = "3ric550N*";
    private static final String PIB_USERNAME = "pibUser";

    @Inject
    private Logger logger;

    @PostConstruct
    public void init() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    Runtime.getRuntime().exec("hostname").getInputStream()));
            host = reader.readLine();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public void updateMaxSessionTimeout(int maxSessionTimeout)
            throws IOException {
        updatePibParameter(MAXSESSIONTIMEOUTPARAMNAME, String.valueOf(maxSessionTimeout));
    }

    public void updateIdleSessionTimeout(int idleSessionTimeout)
            throws IOException {
        updatePibParameter(IDLESESSIONTIMEOUTPARAMNAME, String.valueOf(idleSessionTimeout));
    }

    public void updateSessionConfigurationTimestamp(long timestamp) throws IOException{
       updatePibParameter(SESSIONCONFIGURATIONTIMESTAMP, String.valueOf(timestamp));
    }

    public long updateSessionTimeouts(int maxSessionTimeout, int idleSessionTimeout) throws IOException {
        long timeStamp = new Date().getTime();
        updateIdleSessionTimeout(idleSessionTimeout);
        updateMaxSessionTimeout(maxSessionTimeout);
        updateSessionConfigurationTimestamp(timeStamp);
        return timeStamp;
    }

    private void updatePibParameter(String paramName, String paramValue) throws IOException{
        try {
            HttpClient httpClient = new DefaultHttpClient();
            URI uri = new URIBuilder()
                    .setScheme(SCHEME)
                    .setHost(host)
                    .setPort(PORT)
                    .setPath(PATHUPDATECONFIG)
                    .setParameter(PARAMNAME, paramName)
                    .setParameter(PARAMVALUE,
                            paramValue).build();
            HttpGet httpGet = new HttpGet(uri);
            logger.debug("Updating " + paramName + "  with the value " + paramValue + " by url: "
                    + httpGet.getURI().toString());
            final String encoding = DatatypeConverter
                    .printBase64Binary((PIB_USERNAME + ":" + PIB_PASSWORD)
                            .getBytes("UTF-8"));

            httpGet.setHeader("Authorization", "Basic " + encoding);
            httpClient.execute(httpGet);
        } catch (URISyntaxException e) {
            logger.error(e.getMessage());
        }
    }

}