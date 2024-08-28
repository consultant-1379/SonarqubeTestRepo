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
package com.ericsson.oss.itpf.security.sso.ejb.services.external.citrix;

import com.ericsson.oss.itpf.security.sso.ejb.services.external.ExternalAppIdentifier;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class StoreFrontClient {

    private static final Logger logger = LoggerFactory.getLogger(StoreFrontClient.class);

    public static final String STOREFRONT_URL_SUFFIX = "Citrix/StoreNameWeb/";

    private URL storeFrontUrl = null;
    private String storeFrontUrlStr = null;

    private static final String SECURE_PROTOCOL = "https";

    //TODO - replace with SimpleHttpClient to improve resource utilization and handling cookies
    private CloseableHttpClient httpClient;
    private boolean isSecureProtocol;

    /**
     * Available applications
     *
     * @author ekarpia
     *
     */
    public enum CitrixApplication implements ExternalAppIdentifier {

        IDT("Controller.InformationDesignTo"), NETWORK_ANALYTICS_SERVER("Controller.TIBCO Spotfire"), NETWORK_ANALYTICS_SERVER_SHOW(
                "Controller.TIBCO Spotfire show"), UDT(
                        "Controller.Designer"), INTERNET_EXPLORER("Controller.internet explorer"), WIRC("Controller.WebIRichClient");

        private final String applicationId;

        /**
         * @param applicationId
         */
        CitrixApplication(final String applicationId) {
            this.applicationId = applicationId;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return applicationId;
        }
    }

    /**
     * Gets instance of Http Client
     */
    void getHttpClient() {
        if (isSecureProtocol) {
            // Create a trust manager that does not validate certificate chains
            final SSLContext sslContext;

            try {

                logger.debug("Creating SSL context.");

                sslContext = SSLContext.getInstance("TLS");

                final TrustManager tm = new X509TrustManager() {

                    @Override
                    public void checkClientTrusted(final X509Certificate[] arg0, final String arg1) throws CertificateException {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void checkServerTrusted(final X509Certificate[] arg0, final String arg1) throws CertificateException {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        // TODO Auto-generated method stub
                        return null;
                    }
                };

                sslContext.init(null, new TrustManager[] { tm }, null);

                this.httpClient = HttpClients.custom().setSslcontext(sslContext).setHostnameVerifier(new AllowAllHostnameVerifier()).build();
            } catch (final GeneralSecurityException e) {
                logger.error(e.getMessage(), e);
            }

        } else {
            this.httpClient = HttpClients.createDefault();
        }
    }

    /**
     *
     */
    public StoreFrontClient(final String hostName, final String protocol, final String port, final String url) {

        try {
            this.storeFrontUrlStr = String.format("%s://%s:%s/%s", protocol, hostName, port, STOREFRONT_URL_SUFFIX);
            this.storeFrontUrl = new URL(storeFrontUrlStr);

            if (SECURE_PROTOCOL.equals(protocol)) {
                isSecureProtocol = true;
            } else {
                isSecureProtocol = false;
            }

        } catch (final MalformedURLException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("StoreFront URL {}", storeFrontUrl);
    }

    /**
     *
     * @return
     */
    public URI getURI() {
        try {
            return storeFrontUrl.toURI();
        } catch (final URISyntaxException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Creates connection to Citrix StoreFront and output on success content of ICA file
     *
     * @param username
     * @param password
     * @param domainNetbiosName
     * @param citrixApplication
     * @return returns content of ICA file
     * @throws IOException
     * @throws JSONException
     */
    public String invoke(final String username, final String password, final String domainNetbiosName, final CitrixApplication citrixApplication)
            throws IOException, JSONException {

        final String isSSL = isSecureProtocol ? "Yes" : "No";

        logger.info("isSSL {}", isSSL);

        final HttpPost httpPost = new HttpPost(this.storeFrontUrl + "Home/Configuration");

        //first request is to obtain csfrToken and sessionId
        this.addCommonHeaders(httpPost, isSSL, null);

        this.getHttpClient();
        final CloseableHttpResponse response = httpClient.execute(httpPost);

        logger.info("Home/Configuration  {} ", IOUtils.toString(response.getEntity().getContent(), "UTF-8"));

        final String sessionId = extractCookieValue("ASP.NET_SessionId", response.getHeaders("Set-Cookie"));
        final String csfrToken = extractCookieValue("CsrfToken", response.getHeaders("Set-Cookie"));

        // --------------------

        final HttpPost httpPost2 = new HttpPost(this.storeFrontUrlStr + "Authentication/GetAuthMethods");
        httpPost2.addHeader("Cookie", "CsrfToken=" + csfrToken + ";" + "ASP.NET_SessionId=" + sessionId);

        this.addCommonHeaders(httpPost2, isSSL, csfrToken);

        this.getHttpClient();
        final CloseableHttpResponse response2 = this.httpClient.execute(httpPost2);

        logger.info("/Authentication/GetAuthMethods {}", IOUtils.toString(response2.getEntity().getContent(), "UTF-8"));

        // --------------------

        final HttpPost httpPost3 = new HttpPost(this.storeFrontUrlStr + "ExplicitAuth/Login");

        httpPost3.addHeader("Cookie", "CsrfToken=" + csfrToken + ";" + "ASP.NET_SessionId=" + sessionId);

        final List<NameValuePair> nvps2 = new ArrayList<NameValuePair>();

        final String domainWithUserName = String.format("%s\\%s", domainNetbiosName, username);

        logger.info("domainWithUserName {} ", domainWithUserName);

        nvps2.add(new BasicNameValuePair("username", domainWithUserName));
        nvps2.add(new BasicNameValuePair("password", password));
        httpPost3.setEntity(new UrlEncodedFormEntity(nvps2));

        this.addCommonHeaders(httpPost3, isSSL, csfrToken);

        this.getHttpClient();
        final CloseableHttpResponse response3 = this.httpClient.execute(httpPost3);
        final String str = IOUtils.toString(response3.getEntity().getContent(), "UTF-8");

        logger.info("/ExplicitAuth/Login \n {}", str);

        // -----------------------

        final HttpPost httpPost4 = new HttpPost(this.storeFrontUrlStr + "ExplicitAuth/LoginAttempt");

        httpPost4.addHeader("Cookie", "CsrfToken=" + csfrToken + ";" + "ASP.NET_SessionId=" + sessionId);

        final List<NameValuePair> nvps3 = new ArrayList<NameValuePair>();
        nvps3.add(new BasicNameValuePair("username", domainWithUserName));
        nvps3.add(new BasicNameValuePair("password", password));
        nvps3.add(new BasicNameValuePair("saveCredentials", "false"));
        nvps3.add(new BasicNameValuePair("loginBtn", "Log On"));
        nvps3.add(new BasicNameValuePair("StateContext", ""));
        httpPost4.setEntity(new UrlEncodedFormEntity(nvps3));

        this.addCommonHeaders(httpPost4, isSSL, csfrToken);

        this.getHttpClient();
        final CloseableHttpResponse response4 = this.httpClient.execute(httpPost4);
        final String str2 = IOUtils.toString(response4.getEntity().getContent(), "UTF-8");

        logger.info("/ExplicitAuth/LoginAttempt {}", str2);

        final String authId = extractCookieValue("CtxsAuthId", response4.getHeaders("Set-Cookie"));

        final HttpPost httpPost5 = new HttpPost(this.storeFrontUrlStr + "Resources/List");

        httpPost5.addHeader("Cookie", "CsrfToken=" + csfrToken + ";" + "ASP.NET_SessionId=" + sessionId + ";" + "CtxsAuthId=" + authId);

        final List<NameValuePair> nvps4 = new ArrayList<NameValuePair>();
        nvps4.add(new BasicNameValuePair("format", "json"));
        nvps4.add(new BasicNameValuePair("resourceDetails", "Default"));
        httpPost4.setEntity(new UrlEncodedFormEntity(nvps4));

        this.addCommonHeaders(httpPost5, isSSL, csfrToken);

        this.getHttpClient();
        final CloseableHttpResponse response5 = this.httpClient.execute(httpPost5);
        final String str3 = IOUtils.toString(response5.getEntity().getContent(), "UTF-8");

        final JSONObject resourcesList = new JSONObject(str3);

        if (!resourcesList.has("resources")) {
            throw new IOException("No applications available 1.");
        }

        final JSONArray resources = resourcesList.getJSONArray("resources");

        String launchUrl = null;

        if (resources.length() == 0) {
            throw new IOException("No applications available 1.");
        }

        logger.info("Number of available applications {}", resources.length());

        for (int i = 0; i < resources.length(); i++) {
            final JSONObject application = resources.getJSONObject(i);
            logger.info("Application id {}", application.getString("id"));
        }

        if (citrixApplication != null) {
            for (int i = 0; i < resources.length(); i++) {
                final JSONObject application = resources.getJSONObject(i);

                if (citrixApplication.toString().equals(application.getString("id"))) {

                    logger.info("Found application {} to launch", citrixApplication.toString());

                    launchUrl = application.getString("launchurl");
                    break;
                }
            }

            //here we know, that launchUrl was not set

            if (launchUrl == null) {
                throw new IOException("No applications available 2.");
            }

        } else {
            launchUrl = resources.getJSONObject(0).get("launchurl").toString();
        }

        logger.info("/Resources/List {} ", resourcesList);

        final String icaFileLaunchUrl = this.storeFrontUrlStr + launchUrl;

        logger.info("ICA File Request URL {} ", icaFileLaunchUrl);
        final HttpPost httpPost6 = new HttpPost(icaFileLaunchUrl);

        httpPost6.addHeader("Cookie", "CsrfToken=" + csfrToken + ";" + "ASP.NET_SessionId=" + sessionId + ";" + "CtxsAuthId=" + authId);

        this.addCommonHeaders(httpPost6, isSSL, csfrToken);

        this.getHttpClient();
        final CloseableHttpResponse response6 = this.httpClient.execute(httpPost6);
        final String str4 = IOUtils.toString(response6.getEntity().getContent(), "UTF-8");

        logger.info("ICA file content {}", str4);

        return str4;

    }

    /**
     * Adds common headers required by Citrix
     */
    private void addCommonHeaders(final HttpPost httpPost, final String isSSL, final String csfrToken) {
        httpPost.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=UTF-8");
        httpPost.addHeader(HttpHeaders.ACCEPT, "application/xml, text/xml, */*; q=0.01");
        httpPost.addHeader(HttpHeaders.CONNECTION, "keep-alive");
        httpPost.addHeader(HttpHeaders.HOST, storeFrontUrl.getHost());
        httpPost.addHeader(HttpHeaders.REFERER, this.storeFrontUrlStr);
        httpPost.addHeader("X-Citrix-IsUsingHTTPS", isSSL);
        httpPost.addHeader("X-Requested-With", "XMLHttpRequest");

        if (csfrToken != null) {
            httpPost.addHeader("Csrf-Token", csfrToken);
        }
    }

    /**
     *
     * @param key
     * @param cookie
     * @return
     */
    private String extractCookieValue(final String key, final Header[] cookie) {

        for (final Header header : cookie) {
            for (final HeaderElement element : header.getElements()) {
                if (element.getName().equals(key)) {
                    return element.getValue();
                }
            }
        }

        return "";
    }

}
