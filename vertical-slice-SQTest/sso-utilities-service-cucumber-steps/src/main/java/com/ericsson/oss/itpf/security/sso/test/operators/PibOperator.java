package com.ericsson.oss.itpf.security.sso.test.operators;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class PibOperator {

    private static final String READ_ENDPOINT = "http://sso-instance-1:8080/pib/configurationService/getConfigParameterValue";
    public static final String UPDATE_ENDPOINT = "http://sso-instance-1:8080/pib/configurationService/updateConfigParameterValue";
    private static final Logger logger = LoggerFactory.getLogger(PibOperator.class);


    private HttpUriRequest pibReadRequest;
    private HttpUriRequest pibUpdateRequest;
    private Header userHeader;

    public PibOperator() {
        userHeader = new BasicHeader("X-Tor-UserID","administrator");
    }

    public String read(String key){

       String paramValue=null;
       StringBuffer buf = new StringBuffer(READ_ENDPOINT);
       buf.append("?paramName="+key);
       pibReadRequest =  new HttpGet(buf.toString());
       pibReadRequest.setHeader(userHeader);
        try {
            HttpResponse response = HttpClientBuilder.create().build().execute(pibReadRequest);
            paramValue = response.getEntity().toString();
        } catch (IOException e) {
           logger.error("Unable to read pibParameter [{}]:{}",key,e.getMessage());
        }

        return paramValue;

    }


//    public String update(String key,String value){
//
//
//
//    }




}
