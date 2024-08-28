package com.ericsson.oss.itpf.security.sso.test.operators;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LoginLogoutOperator {


    private HttpUriRequest loginRequest;
    private HttpUriRequest logoutRequest;
    private Header[] cookieheaders;
    private HttpResponse httpResponse;
    private static final Logger logger = LoggerFactory.getLogger(LoginLogoutOperator.class);

    public LoginLogoutOperator() {

        loginRequest = new HttpPost( "http://sso-instance-1.enmapache.athtem.eei.ericsson.se:8080/heimdallr/UI/Login?IDToken1=administrator&IDToken2=TestPassw0rd" );
        logoutRequest = new HttpPost( "http://sso-instance-1.enmapache.athtem.eei.ericsson.se:8080/heimdallr/UI/Logout" );

    }


    public Header[] doLogin(){

        logger.info("Login request");

        try {
            httpResponse = HttpClientBuilder.create().build().execute(loginRequest);
            cookieheaders = httpResponse.getHeaders("Set-Cookie");
           // sessions = getUserSessions();
        } catch (IOException e) {

            logger.error("Login Failed {}",e.getMessage());
        }

        return cookieheaders;
    }


    public void doLogout(){

        logger.info("Logout request");
        StringBuffer buf = new StringBuffer();

        try {
            for(int i=0; i<cookieheaders.length;i++)
                buf.append(cookieheaders[i].getElements()[0].getName()+"="+cookieheaders[i].getElements()[0].getValue()+"; ");

            logoutRequest.addHeader("Cookie",buf.toString());
            httpResponse = HttpClientBuilder.create().build().execute(logoutRequest);
         //   sessions = getUserSessions();
        } catch (IOException e) {
            logger.error("Logout Failed");
        }
    }











}
