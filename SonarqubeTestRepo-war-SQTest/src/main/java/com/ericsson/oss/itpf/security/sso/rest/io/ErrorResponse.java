package com.ericsson.oss.itpf.security.sso.rest.io;

/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by emapawl on 2016-01-19.
 */

public class ErrorResponse {

    private static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    private String userMessage;
    private Integer httpStatusCode;
    private String internalErrorCode;
    private String time;

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(Integer httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public String getInternalErrorCode() {
        return internalErrorCode;
    }

    public void setInternalErrorCode(String internalErrorCode) {
        this.internalErrorCode = internalErrorCode;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public static String getCurrentTime() {
        SimpleDateFormat df = new SimpleDateFormat(ISO_DATE_FORMAT);
        return df.format(new Date());
    }

    @Override
    public String toString() {

        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append(this.getClass().getSimpleName()).append("{");
        stringBuilder.append("userMessage='").append(userMessage);
        stringBuilder.append(", httpStatusCode=").append(httpStatusCode);
        stringBuilder.append(", internalErrorCode='").append(internalErrorCode);
        stringBuilder.append(", time='").append(time);
        stringBuilder.append("}");

        return stringBuilder.toString();
    }
}

