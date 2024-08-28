package com.ericsson.oss.itpf.security.sso.test.step;


import com.ericsson.oss.cucumber.arquillian.api.CucumberGlues;
import com.ericsson.oss.itpf.security.sso.test.utils.TestUtils;
import com.google.gson.JsonObject;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.json.JSONObject;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;

@CucumberGlues
public class SSOCucumberSteps {

    @Inject
    TestUtils utils;

    private HttpUriRequest loginRequest;
    private HttpUriRequest logoutRequest;
    private HttpUriRequest terminateRequest;
    private HttpUriRequest sessionsRequest;
    private HttpResponse sessionsResponse;
    private HttpResponse httpResponse;
    private boolean tokenFound = false;
    private String sessionResult=null;
    private Header userHeader;
    private Header contentheader;
    private Header[] cookieheaders;
    private int sessions;
    private static final Logger logger = LoggerFactory.getLogger(SSOCucumberSteps.class);


    @Before
    public void setup(){
        loginRequest = new HttpPost( "http://sso-instance-1.enmapache.athtem.eei.ericsson.se:8080/heimdallr/UI/Login?IDToken1=administrator&IDToken2=TestPassw0rd" );
        logoutRequest = new HttpPost( "http://sso-instance-1.enmapache.athtem.eei.ericsson.se:8080/heimdallr/UI/Logout" );
        sessionsRequest = new HttpGet( "http://sso-instance-1.enmapache.athtem.eei.ericsson.se:8080/oss/sso/utilities/users" );
        terminateRequest = new HttpDelete("http://sso.enmapache.athtem.eei.ericsson.se:8080/oss/sso/utilities/users/administrator");
        userHeader = new BasicHeader("X-Tor-UserID","administrator");
        contentheader = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        sessionsRequest.addHeader(contentheader);
        sessionsRequest.addHeader(userHeader);
        terminateRequest.addHeader(userHeader);
        doTerminate();

    }


    @Given("^A working SSO login REST$")
    public void givenClause() {}

    @When("^The user inserts valid credentials$")
    public void whenClause() {
        doLogin();

    }

    @Then("^A valid cookie is returned$")
    public void ThenClause() {

       for(int i=0; i<cookieheaders.length;i++){

         if(cookieheaders[i].getValue().contains("iPlanetDirectoryPro")) {
             tokenFound = true;
             break;
         }
       }
       Assert.assertTrue((httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY) ||
                                  (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK));
       Assert.assertTrue(tokenFound);
       doLogout();

    }


    @Given("^Check the session number per user$")
    public void givenClause1(){}

    @When("^The users REST is called$")
    public void whenClause1() {
            doLogin();

    }

    @Then("^The number of sessions is correctly reported$")
    public void ThenClause1() {
        Assert.assertEquals(sessions,1);
        doLogout();
    }


    @Given("^Test sessions termination$")
    public void givenClause2(){
        doLogin();
    }

    @When("^The terminate REST is called$")
    public void whenClause2() {
            doTerminate();

    }

    @Then("^The number of reported sessions is zero$")
    public void ThenClause2() {
        Assert.assertEquals(sessions,0);
    }


  //Utility functions (operators)
    public void doLogin(){

        logger.info("Login request");

        try {
            httpResponse = HttpClientBuilder.create().build().execute(loginRequest);
            cookieheaders = httpResponse.getHeaders("Set-Cookie");
            sessions = getUserSessions();
        } catch (IOException e) {

            logger.error("Login Failed {}",e.getMessage());
        }
    }


    public void doTerminate(){

        logger.info("Terminate request");

        try {
            httpResponse = HttpClientBuilder.create().build().execute(terminateRequest);
            sessions = getUserSessions();
        } catch (IOException e) {

            logger.error("Terminate sessions Failed {}",e.getMessage());
        }
    }


    public void doLogout(){

        logger.info("Logout request");
        StringBuffer buf = new StringBuffer();

        try {
            for(int i=0; i<cookieheaders.length;i++)
                buf.append(cookieheaders[i].getElements()[0].getName()+"="+cookieheaders[i].getElements()[0].getValue()+"; ");

            logoutRequest.addHeader("Cookie",buf.toString());
            httpResponse = HttpClientBuilder.create().build().execute(logoutRequest);
            sessions = getUserSessions();
        } catch (IOException e) {
            logger.error("Logout Failed");
        }
    }


    public int getUserSessions()  {
        int userSessions=0;

        try {

            sessionsResponse = HttpClientBuilder.create().build().execute(sessionsRequest);
            InputStream instream = sessionsResponse.getEntity().getContent();
            sessionResult = utils.convertStreamToString(instream);
            JSONObject sessions = new JSONObject(sessionResult);
            JSONObject users = sessions.getJSONObject("users");
            userSessions = Integer.parseInt(users.get("administrator").toString());
        } catch(Exception e){
            logger.error("Unable to get sessions: {}",e.getMessage());
        }

        return userSessions;
    }
}
