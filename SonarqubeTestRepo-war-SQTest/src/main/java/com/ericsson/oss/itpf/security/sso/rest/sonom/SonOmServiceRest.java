/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.itpf.security.sso.rest.sonom;

import com.ericsson.oss.itpf.security.sso.ejb.services.external.ExternalAppCredentials;
import com.ericsson.oss.itpf.security.sso.ejb.services.external.ExternalWebAppProperties;
import com.ericsson.oss.itpf.security.sso.ejb.services.external.ExternalWebAppProperties.AppType;
import com.ericsson.oss.itpf.security.sso.ejb.sysinit.SonOmPropertiesReader;
import com.ericsson.oss.itpf.security.sso.ejb.sysinit.external.ExternalApplicationConfigReader;
import com.ericsson.oss.itpf.security.sso.resource.interceptor.Authorize;
import com.ericsson.oss.itpf.security.sso.rest.external.ExternalAppRestService;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class is used to expose REST endpoints for services, that allow to launch SON-OM providing SSO
 *
 * @author ekarpia
 *
 */
@Path("/")
public class SonOmServiceRest extends ExternalAppRestService {

    private static final Logger logger = LoggerFactory.getLogger(SonOmServiceRest.class);

    private boolean isEnabled;
    private boolean isSsoEnabled;

    private ExternalWebAppProperties sonomProperties;

    /**
     *
     */
    public SonOmServiceRest() {

        ExternalApplicationConfigReader deploymentProperties = null;
        try {
            deploymentProperties = new SonOmPropertiesReader();
        } catch (final IOException e) {
            isEnabled = false;
            logger.error(e.getMessage(), e);
        }

        if (deploymentProperties != null) {

            //as this feature is optional,
            isEnabled = true;

            final String sonomIntegration = deploymentProperties.getValue("sonom_sso").toString();

            if (sonomIntegration == null) {
                isEnabled = false;
                logger.info("Service disabled");

            } else {
                isSsoEnabled = Boolean.valueOf(sonomIntegration);

                sonomProperties = new ExternalWebAppProperties(AppType.SONOM, deploymentProperties, "");

                logger.info("Service enabled, sso {}, sonom Properties {}, sonomProtocol {}, sonomPort {}", isSsoEnabled, sonomProperties.toString());
            }
        }

    }

    /**
     * Allows user to access sonom
     *
     * @param webAppId
     * @param userName
     * @param ssoToken
     * @return
     */
    @GET
    @Path("/web/sonom")
    @Authorize(action = "read", resource = { "manage_regions", "sas_user", "sas_manage_instances", "sas_toggle_use_case", "sas_configure_use_case",
            "sas_start_use_case", "sas_manage_exceptions", "sas_set_mysql", "acom_user", "acom_manage_instances", "acom_toggle_use_case",
            "acom_configure_use_case", "acom_start_use_case", "sdg_manage_instances", "sdg_configure_flavor", "sdg_toggle_flavor", "sdg_start_task",
            "sdg_stop_task", "sdg_set_mysql", "sdg_reset_database", "sdg_repair_database", "sis_manage_instances", "sis_manage_profiles",
            "sis_schedule_task", "sis_remove_task", "sis_set_mysql", "sis_set_shared_data_path" })
    public Response generateWebAccess(

            @HeaderParam(USER_HEADER) final String userName, @CookieParam(SSO_TOKEN_COOKIE) final String ssoToken) throws IOException {

        if (!isEnabled) {
            return getMessageResponse(Status.SERVICE_UNAVAILABLE, SERVICE_NOT_CONFIGURED);
        }

        if (userName == null || ssoToken == null) {
            return getMessageResponse(Status.NOT_ACCEPTABLE, CREDENTIALS_NOT_AVAILABLE);
        }

        logger.info("User {} is requesting access to SON-OM.", userName);

        try {

            final URI loginURL = new URI(String.format("%s/%s", this.sonomProperties.getURI(), "son"));

            if (!isSsoEnabled) {
                return Response.seeOther(loginURL).build();
            }

            final List<NameValuePair> sonomHeaders = new ArrayList<NameValuePair>();
            sonomHeaders.add(new BasicNameValuePair(ExternalAppCredentials.USERNAME_HEADER_NAME, userName));
            sonomHeaders.add(new BasicNameValuePair(ExternalAppCredentials.TOKEN_HEADER_NAME, ssoToken));

            final HttpGet sonomLoginRequest = this.generateGet(this.sonomProperties.getAppHost(), "/son/sonmanagerweb/sso", sonomHeaders);

            final ExternalAppCredentials credentials = new ExternalAppCredentials(userName, ssoToken);

            final URI appPageUri = UriBuilder.fromUri(loginURL).queryParam(ExternalAppCredentials.USERNAME_HEADER_NAME, credentials.getUserName())
                    .queryParam(ExternalAppCredentials.TOKEN_HEADER_NAME, credentials.getSsoToken()).build();

            return this.buildResponseForWebApp(credentials, appPageUri, sonomLoginRequest, "SON-OM", this.sonomProperties.isSecured(), true);

        } catch (final URISyntaxException e) {
            logger.error(e.getMessage(), e);
            return getMessageResponse(Status.SERVICE_UNAVAILABLE, SERVICE_NOT_AVAILABLE);
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
            return getMessageResponse(Status.SERVICE_UNAVAILABLE, SERVICE_NOT_AVAILABLE);
        }

    }

}
