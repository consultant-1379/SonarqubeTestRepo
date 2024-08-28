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
package com.ericsson.oss.itpf.security.sso.rest.external;

import com.ericsson.oss.itpf.security.sso.ejb.services.external.ExternalAppCredentials;
import com.ericsson.oss.itpf.security.sso.rest.external.net.SimpleHttpClient;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.List;

public class ExternalAppRestService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalAppRestService.class);

    // common properties for ENM access
    public static final String SSO_TOKEN_COOKIE = "iPlanetDirectoryPro";

    public static final String USER_HEADER = "X-Tor-UserID";

    // common constants of error messages
    protected static final String SERVICE_NOT_CONFIGURED = "Service is not configured, please contact ENM System Administrator to execute Post Deployment procedure.";

    protected static final String CREDENTIALS_NOT_AVAILABLE = "Either username or SSO Token are not supplied, please contact ENM System Administrator.";

    protected static final String SERVICE_NOT_AVAILABLE = "Service is not available.";

    /**
     * Returns the message for user in HTML format
     *
     * @param status
     * @param message
     * @return
     */
    protected Response getMessageResponse(final Status status, final String message) {
        return Response.status(status).entity(message).type(MediaType.TEXT_HTML).build();
    }

    /**
     * Builds response for web applications BO/NETAN/SON-OM
     *
     * @return
     * @throws IOException
     * @throws IllegalStateException
     * @throws URISyntaxException
     */
    protected Response buildResponseForWebApp(final ExternalAppCredentials credentials, final URI appPageUri, final HttpRequestBase request,
            final String applicationName, final boolean isSecured, final boolean addAuthHeaders)
            throws IllegalStateException, IOException, URISyntaxException {

        final SimpleHttpClient appClient = this.getHttpClient(isSecured);

        final CloseableHttpClient httpClient = appClient.getHttpClient();

        final CloseableHttpResponse response = httpClient.execute(request);

        final int statusCode = response.getStatusLine().getStatusCode();

        // continue
        if (HttpStatus.SC_OK == statusCode) {
            logger.info("Success.\n");
        } else if (HttpStatus.SC_BAD_REQUEST == statusCode) {
            return getMessageResponse(Status.BAD_REQUEST,
                    String.format("Unable to authenticate to %s, please provide valid credentials.", applicationName));
        } else {
            return getMessageResponse(Status.UNAUTHORIZED,
                    String.format("Unable to authenticate to %s, please contact ENM System Administrator.", applicationName));
        }

        logger.info("\nresponse status line {},\nhttp status code {}", response.getStatusLine(), response.getStatusLine().getStatusCode());

        // extract all cookies

        final NewCookie[] cookies = SimpleHttpClient.convertCookies(appClient.getHttpCookieStore());

        logger.info("APP PAGE with SSO after login URL {}.", appPageUri);
        logger.info("Cookies for redirect <{}>", (Object) cookies);

        if (addAuthHeaders) {
            return Response.seeOther(appPageUri).cookie(cookies).header(ExternalAppCredentials.USERNAME_HEADER_NAME, credentials.getUserName())
                    .header(ExternalAppCredentials.TOKEN_HEADER_NAME, credentials.getSsoToken()).build();
        }

        return Response.seeOther(appPageUri).cookie(cookies).build();

    }

    /**
     * Gets instance of Http Client
     *
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     */
    protected SimpleHttpClient getHttpClient(final boolean isSecuredProtocol) throws IOException {
        return SimpleHttpClient.getHttpClient(isSecuredProtocol);
    }

    /**
     *
     * @param relativePath
     * @return
     * @throws UnsupportedEncodingException
     */
    protected HttpPost generatePost(final HttpHost httpHost, final String relativePath, final List<NameValuePair> parameters)
            throws UnsupportedEncodingException {

        final String uri = String.format("%s://%s%s", httpHost.getSchemeName(), httpHost.toHostString(), relativePath);

        logger.info("POST-URL {}", uri);

        final HttpPost httpPost = new HttpPost(uri);

        httpPost.setEntity(new UrlEncodedFormEntity(parameters));

        httpPost.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
        httpPost.addHeader(HttpHeaders.HOST, httpHost.toHostString());

        return httpPost;
    }

    /**
     *
     * @param relativePath
     * @return
     * @throws UnsupportedEncodingException
     */
    protected HttpGet generateGet(final HttpHost httpHost, final String relativePath, final List<NameValuePair> headers)
            throws UnsupportedEncodingException {

        final String uri = String.format("%s://%s%s", httpHost.getSchemeName(), httpHost.toHostString(), relativePath);

        logger.info("GET-URL {}", uri);

        final HttpGet httpGet = new HttpGet(uri);

        if (headers != null) {
            for (final NameValuePair header : headers) {
                httpGet.addHeader(header.getName(), header.getValue());
            }
        }

        return httpGet;
    }

    /**
     *
     * @param response
     * @throws IOException
     * @throws IllegalStateException
     */
    protected boolean checkResponse(final CloseableHttpResponse response) throws IllegalStateException, IOException {

        final int statusCode = response.getStatusLine().getStatusCode();

        logger.debug("Status code {}", statusCode);
        if (statusCode == HttpStatus.SC_OK

                || statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
            logger.debug("Success.\n");

            return true;
        }
        return false;

    }
}
