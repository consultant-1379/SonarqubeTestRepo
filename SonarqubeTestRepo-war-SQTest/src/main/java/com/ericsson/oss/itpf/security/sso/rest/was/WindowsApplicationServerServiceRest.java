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
package com.ericsson.oss.itpf.security.sso.rest.was;

import com.ericsson.oss.itpf.security.sso.ejb.beans.ActiveDirectoryServiceImpl;
import com.ericsson.oss.itpf.security.sso.ejb.services.external.ExternalAppCredentials;
import com.ericsson.oss.itpf.security.sso.ejb.services.external.ExternalAppIdentifier;
import com.ericsson.oss.itpf.security.sso.ejb.services.external.ExternalWebAppProperties;
import com.ericsson.oss.itpf.security.sso.ejb.services.external.ExternalWebAppProperties.AppType;
import com.ericsson.oss.itpf.security.sso.ejb.services.external.citrix.StoreFrontClient;
import com.ericsson.oss.itpf.security.sso.ejb.services.external.citrix.StoreFrontClient.CitrixApplication;
import com.ericsson.oss.itpf.security.sso.ejb.sysinit.EniqWasPropertiesReader;
import com.ericsson.oss.itpf.security.sso.resource.interceptor.Authorize;
import com.ericsson.oss.itpf.security.sso.rest.external.ExternalAppRestService;
import com.ericsson.oss.itpf.security.sso.rest.external.net.SimpleHttpClient;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.xpath.XPathExpressionException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class is used to expose REST endpoints for services, that allow to launch Citrix Applications and Web Application on Windows Application Server
 * (WAS), before launching application SSO details are used, to update information about the user in Active Directory
 *
 * @author ekarpia
 *
 */
@Path("/")
public class WindowsApplicationServerServiceRest extends ExternalAppRestService {

    private static final String CITRIX_ICA_FILE_CONTENT_TYPE = "application/x-ica";

    private static final Logger logger = LoggerFactory.getLogger(WindowsApplicationServerServiceRest.class);

    //constants of error messages

    private static final String APPLICATION_ID_NOT_AVAILABLE = "Please provide valid Application ID.";

    private static final String APPLICATION_ID_NOT_VALID = "There is no application of that type.";
    private static final String CITRIX_COMMUNICATION_FAILURE = "Unable to generate ICA file, please contact ENM System Administrator.";

    private static final String AD_COMMUNICATION_ERROR = "Unable to update users password in Active Directory, please contact ENM System Administrator.";

    /**
     * Administrator account is not allowed to access, if sso is enabled
     */
    private List<String> blackListedUserNames = new ArrayList<String>(Arrays.asList("Administrator", "administrator"));

    /**
     * Available web applications on WAS
     *
     * @author ekarpia
     *
     */
    public enum WasWebApplication implements ExternalAppIdentifier {
        BO_CMC, BO_BILAUNCHPAD, NETAN_WEBPLAYER,
    }

    /**
     *
     */
    private Map<ExternalAppIdentifier, AppType> applicationsList = new HashMap<ExternalAppIdentifier, AppType>();

    @Context
    private UriInfo uri;

    private ActiveDirectoryServiceImpl activeDirectoryService = null;

    private StoreFrontClient citrixService = null;

    private String domainName = null;

    private boolean isEnabled;
    private boolean isBoSSOEnabled;
    private boolean isNetanSSOEnabled;

    private ExternalWebAppProperties boCMCProperties;
    private ExternalWebAppProperties boBILaunchPadProperties;
    private ExternalWebAppProperties netanWebProperties;

    EniqWasPropertiesReader deploymentProperties;

    /**
     *
     */
    public WindowsApplicationServerServiceRest() {

        EniqWasPropertiesReader deploymentProperties = null;
        try {
            deploymentProperties = new EniqWasPropertiesReader();
        } catch (final IOException e) {
            isEnabled = false;
            logger.error(e.getMessage(), e);
        }

        if (deploymentProperties != null) {
            //as this feature is optional,
            isEnabled = true;

            isBoSSOEnabled = Boolean.valueOf(deploymentProperties.getValue("bo_sso").toString());
            isNetanSSOEnabled = Boolean.valueOf(deploymentProperties.getValue("netan_sso").toString());

            final String wasIntegration = deploymentProperties.getValue("was_ad_domain_name").toString();

            if (wasIntegration == null) {
                domainName = null;
                citrixService = null;
                activeDirectoryService = null;
                isEnabled = false;

            } else {
                domainName = deploymentProperties.getValue("was_ad_domain_name").toString();

                activeDirectoryService = new ActiveDirectoryServiceImpl(deploymentProperties);

                citrixService = new StoreFrontClient(

                        deploymentProperties.getValue("was_citrix_hostname"), deploymentProperties.getValue("was_citrix_protocol"),
                        deploymentProperties.getValue("was_citrix_port"), deploymentProperties.getValue("was_citrix_url"));

                //web applications
                applicationsList.put(WasWebApplication.BO_CMC, AppType.BO);
                applicationsList.put(WasWebApplication.BO_BILAUNCHPAD, AppType.BO);
                applicationsList.put(WasWebApplication.NETAN_WEBPLAYER, AppType.NETAN);

                //desktop applications

                applicationsList.put(CitrixApplication.IDT, AppType.BO);
                applicationsList.put(CitrixApplication.UDT, AppType.BO);
                applicationsList.put(CitrixApplication.WIRC, AppType.BO);
                applicationsList.put(CitrixApplication.NETWORK_ANALYTICS_SERVER, AppType.NETAN);

                boCMCProperties = new ExternalWebAppProperties(AppType.BO, deploymentProperties, "/BOE/CMC");
                boBILaunchPadProperties = new ExternalWebAppProperties(AppType.BO, deploymentProperties, "/BOE/BI");
                netanWebProperties = new ExternalWebAppProperties(AppType.NETAN, deploymentProperties, "/");
            }
        }
    }

    @GET
    @Path("/was/citrix/ica/{citrixAppId}")
    @Authorize(action = "read", resource = { "netan-server-admin-access", "netan-business-analyst-access", "netan-business-author-access",
            "netan-consumer-access", "bo-admin-access", "bo-report-operator-access", "bo-universe-access" })
    public Response generateCitrixReceiverFile(

            @PathParam("citrixAppId") final String citrixAppId,

            @HeaderParam(USER_HEADER) final String userName, @CookieParam(SSO_TOKEN_COOKIE) final String ssoToken) {

        if (!isEnabled) {
            return getMessageResponse(Status.SERVICE_UNAVAILABLE, SERVICE_NOT_CONFIGURED);
        }

        if (userName == null || ssoToken == null) {

            return getMessageResponse(Status.NOT_ACCEPTABLE, CREDENTIALS_NOT_AVAILABLE);
        }

        logger.info("Username {}, citrixAppId {}", userName, citrixAppId);

        if (citrixAppId == null) {
            return getMessageResponse(Status.BAD_REQUEST, APPLICATION_ID_NOT_AVAILABLE);
        }

        CitrixApplication appId = null;

        try {

            appId = CitrixApplication.valueOf(citrixAppId);

            if (appId == null) {
                return getMessageResponse(Status.BAD_REQUEST, APPLICATION_ID_NOT_VALID);
            }
        } catch (final IllegalArgumentException ex) {
            return getMessageResponse(Status.BAD_REQUEST, APPLICATION_ID_NOT_VALID);
        }

        //check type of application we are having

        final boolean isSSO = isAppSSOEnabled(appId);

        if (blackListedUserNames.contains(userName) && isSSO) {
            return getMessageResponse(Status.NOT_ACCEPTABLE, "You are not allowed to access this feature, using this user");
        }

        if (!isSSO) {
            if (citrixService != null) {
                return Response.seeOther(citrixService.getURI()).build();
            } else {
                return getMessageResponse(Status.SERVICE_UNAVAILABLE, CITRIX_COMMUNICATION_FAILURE);
            }
        }

        final boolean isSucccess = this.updateUserPassword(userName, ssoToken);

        if (!isSucccess) {
            return getMessageResponse(Status.BAD_REQUEST, AD_COMMUNICATION_ERROR);
        }

        if (citrixService != null) {

            try {
                final String icaFile = citrixService.invoke(userName, ssoToken, this.domainName, appId);
                return Response.ok().entity(icaFile).type(CITRIX_ICA_FILE_CONTENT_TYPE).build();
            } catch (final IOException ex) {
                logger.error(ex.getMessage(), ex);
                return getMessageResponse(Status.BAD_REQUEST, ex.getMessage());
            } catch (final JSONException ex) {
                logger.error(ex.getMessage(), ex);
                return getMessageResponse(Status.BAD_REQUEST, CITRIX_COMMUNICATION_FAILURE);
            }

        } else {
            logger.error("citrixService is null");
        }

        return getMessageResponse(Status.SERVICE_UNAVAILABLE, SERVICE_NOT_AVAILABLE);
    }

    /**
     * Allows user to access specific web application
     *
     * @param webAppId
     * @param userName
     * @param ssoToken
     * @return
     */
    @GET
    @Path("/was/web/{appId}")
    @Authorize(action = "read", resource = { "netan-server-admin-access", "netan-business-analyst-access", "netan-business-author-access",
            "netan-consumer-access", "bo-admin-access", "bo-report-operator-access", "bo-universe-access" })
    public Response generateWebAccess(

            @PathParam("appId") final String webAppId,

            @HeaderParam(USER_HEADER) final String userName, @CookieParam(SSO_TOKEN_COOKIE) final String ssoToken,
            @Context final HttpServletRequest request, @Context final HttpServletResponse response

    ) throws IOException, XPathExpressionException {

        if (!isEnabled) {
            return getMessageResponse(Status.SERVICE_UNAVAILABLE, SERVICE_NOT_CONFIGURED);
        }

        if (userName == null || ssoToken == null) {
            return getMessageResponse(Status.NOT_ACCEPTABLE, CREDENTIALS_NOT_AVAILABLE);
        }

        logger.info("Username {}, WasWebApplication {}", userName, webAppId);

        if (webAppId == null) {
            return getMessageResponse(Status.BAD_REQUEST, APPLICATION_ID_NOT_AVAILABLE);
        }

        WasWebApplication appId = null;

        try {

            appId = WasWebApplication.valueOf(webAppId);

            if (appId == null) {
                getMessageResponse(Status.BAD_REQUEST, APPLICATION_ID_NOT_VALID);
            }
        } catch (final IllegalArgumentException ex) {
            return getMessageResponse(Status.BAD_REQUEST, APPLICATION_ID_NOT_VALID);
        }

        //check type of application we are having

        final boolean isSSO = isAppSSOEnabled(appId);

        if (blackListedUserNames.contains(userName) && isSSO) {
            return getMessageResponse(Status.NOT_ACCEPTABLE, "You are not allowed to access this feature, using this user.");
        }

        if (!isSSO) {
            try {
                final URI appUri = this.constructURI(appId);
                logger.info("App URI {}", appUri);
                return Response.seeOther(appUri).build();
            } catch (final URISyntaxException e) {
                return getMessageResponse(Status.BAD_REQUEST, "ENM configuration is not valid for " + appId);
            }
        }

        //do not update passwords with SSO Token for CMC
        if (WasWebApplication.BO_CMC != appId) {
            final boolean isSucccess = this.updateUserPassword(userName, ssoToken);

            if (!isSucccess) {
                return getMessageResponse(Status.BAD_REQUEST, AD_COMMUNICATION_ERROR);
            }
        }

        //construct web application url
        //proceed to application

        if (WasWebApplication.NETAN_WEBPLAYER == appId) {
            try {

                logger.info("Generate authentication for {}", appId.name());
                return this.generateAuthForNetanWebPlayer(userName, ssoToken, this.netanWebProperties, appId);
            } catch (IllegalStateException | URISyntaxException e) {
                return getMessageResponse(Status.SERVICE_UNAVAILABLE, SERVICE_NOT_AVAILABLE);
            }
        }

        else if (WasWebApplication.BO_BILAUNCHPAD == appId) {
            try {

                logger.info("Generate authentication for {}", appId.name());
                return this.generateAuthForBi(userName, ssoToken, this.boBILaunchPadProperties, appId);
            } catch (IllegalStateException | URISyntaxException e) {
                return getMessageResponse(Status.SERVICE_UNAVAILABLE, SERVICE_NOT_AVAILABLE);
            }
        }

        else if (WasWebApplication.BO_CMC == appId) {
            try {

                logger.info("Generate authentication for {}", appId.name());
                final URI appUri = this.constructURI(appId);
                logger.info("App URI {}", appUri);
                return Response.seeOther(appUri).build();
            } catch (IllegalStateException | URISyntaxException e) {
                return getMessageResponse(Status.SERVICE_UNAVAILABLE, SERVICE_NOT_AVAILABLE);
            }
        } else {
            return getMessageResponse(Status.BAD_REQUEST, "Application is not supported.");
        }

    }

    /**
     *
     * @param userName
     * @param ssoToken
     * @param appProperties
     * @param appId
     * @return
     * @throws URISyntaxException
     * @throws IllegalStateException
     * @throws IOException
     */
    private Response generateAuthForNetanWebPlayer(final String userName, final String ssoToken, final ExternalWebAppProperties appProperties,

            final WasWebApplication appId) throws URISyntaxException, IllegalStateException, IOException {
        final List<NameValuePair> appHeaders = new ArrayList<NameValuePair>();
        appHeaders.add(new BasicNameValuePair(ExternalAppCredentials.USERNAME_HEADER_NAME, userName));
        appHeaders.add(new BasicNameValuePair(ExternalAppCredentials.TOKEN_HEADER_NAME, ssoToken));

        logger.info("Added headers for username {} with sso token {}", userName, ssoToken);
        final HttpGet loginRequest = this.generateGet(appProperties.getAppHost(), appProperties.getUrl(), appHeaders);

        final ExternalAppCredentials credentials = new ExternalAppCredentials(userName, ssoToken);

        final URI appPageUri = appProperties.getURI();

        return this.buildResponseForWebApp(credentials, appPageUri, loginRequest, appId.name(), appProperties.isSecured(), true);

    }

    /**
     *
     * @param userName
     * @param ssoToken
     * @param appProperties
     * @param appId
     * @return
     * @throws URISyntaxException
     * @throws IllegalStateException
     * @throws IOException
     */
    private Response generateAuthForBi(final String userName, final String ssoToken, final ExternalWebAppProperties appProperties,

            final WasWebApplication appId) throws URISyntaxException, IllegalStateException, IOException {

        logger.info("Added headers for username {} with sso token {}", userName, ssoToken);
        final SimpleHttpClient boAppClient = this.getHttpClient(appProperties.isSecured());
        final HttpHost boHost = appProperties.getAppHost();

        final CloseableHttpClient boClient = boAppClient.getHttpClient();

        final StringBuilder strBuilder = new StringBuilder();

        strBuilder.append("/AdminTools/GetBOToken.jsp?");
        strBuilder.append("user=").append(userName).append("&password=").append(URLEncoder.encode(ssoToken, "UTF-8"));

        final HttpGet authenticationRequest = this.generateGet(boHost, strBuilder.toString(), null);

        final CloseableHttpResponse authenticationResponse = boClient.execute(authenticationRequest);
        if (!this.checkResponse(authenticationResponse)) {
            return getMessageResponse(Status.BAD_REQUEST, "ENM Username or ENM SSO Token not valid.");
        }

        final String response = IOUtils.toString(authenticationResponse.getEntity().getContent(), "UTF-8");

        final String cleanResponse = response.replace("\n", "").trim();

        final String biToken = cleanResponse.replace("Token=", "");

        final String appPageUrl = appProperties.getUrl();

        //generate URL to redirect

        final String urlToRedirect = String.format("%s://%s%s/%s=%s", boHost.getSchemeName(), boHost.toHostString(), appPageUrl,

                "logon/start.do?ivsLogonToken", biToken);

        logger.info("urlToRedirect {}", urlToRedirect);
        return Response.temporaryRedirect(new URI(urlToRedirect)).build();

    }

    /**
     *
     * @param appId
     * @return
     * @throws URISyntaxException
     */
    private URI constructURI(final ExternalAppIdentifier appId) throws URISyntaxException {

        //check type of application we are having
        final AppType appName = this.applicationsList.get(appId);

        logger.info("appName:{} appId: {} ", appName, appId);
        if (appName == AppType.BO) {

            if (appId == WasWebApplication.BO_BILAUNCHPAD) {
                return this.boBILaunchPadProperties.getURI();
            } else if (appId == WasWebApplication.BO_CMC) {
                return this.boCMCProperties.getURI();

            }
        }

        else if (appName == AppType.NETAN) {
            return this.netanWebProperties.getURI();
        }
        return null;
    }

    /**
     *
     * @param appId
     * @return
     */
    private boolean isAppSSOEnabled(final ExternalAppIdentifier appId) {
        //check type of application we are having
        final AppType appName = this.applicationsList.get(appId);

        boolean isSSO = true;

        if (appName == AppType.BO) {

            if (!isBoSSOEnabled) {
                isSSO = false;
            }
        }

        else if (appName == AppType.NETAN) {
            if (!isNetanSSOEnabled) {
                isSSO = false;
            }
        }

        return isSSO;
    }

    /**
     * Updates user's password in Active Directory
     *
     * @param userName
     * @param ssoToken
     * @return
     */
    private boolean updateUserPassword(final String userName, final String ssoToken) {
        logger.info("Username {}, ssoToken {}", userName, ssoToken);

        try {

            if (activeDirectoryService != null) {
                activeDirectoryService.sync(userName, ssoToken);
                return true;
            } else {
                logger.info("activeDirectoryService is null");
            }
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

}
