/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.itpf.security.sso.rest.openamproxy.client;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.ClientResponse;

public interface OpenAMClient {

    public static final String IS_TOKEN_VALID_ENDPOINT = "/identity/isTokenValid";

    public static final String ATTRIBUTES_ENDPOINT = "/identity/attributes";

    public static final String TOKEN_ID = "tokenid";

    public static final String SUBJECT_ID = "subjectid";

    @POST
    @Path(IS_TOKEN_VALID_ENDPOINT)
    @Produces(MediaType.APPLICATION_FORM_URLENCODED)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public ClientResponse<String> isTokenValid(@FormParam(TOKEN_ID) String tokenId);

    @GET
    @Path(ATTRIBUTES_ENDPOINT)
    @Produces(MediaType.APPLICATION_FORM_URLENCODED)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public ClientResponse<String> getAttributes(@QueryParam(SUBJECT_ID) String subjectId,
            @QueryParam("attributenames") String attributeName);

}
