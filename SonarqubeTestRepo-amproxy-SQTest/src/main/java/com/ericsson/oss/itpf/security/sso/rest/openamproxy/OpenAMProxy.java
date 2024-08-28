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
package com.ericsson.oss.itpf.security.sso.rest.openamproxy;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.security.sso.rest.openamproxy.client.OpenAMClient;

@Path("heimdallr/")
@Produces(MediaType.APPLICATION_JSON)
public class OpenAMProxy {

    private static final String OPENAM_CONTEXT = "/heimdallr";

    private Logger logger = LoggerFactory.getLogger(OpenAMProxy.class);

    @Context
    private UriInfo uri;

    private OpenAMClient openAMClient;

    public void initialize() {
        if (openAMClient == null) {
            ClientRequestFactory crf = new ClientRequestFactory();
            String openamUrl = uri.getBaseUriBuilder().replacePath(OPENAM_CONTEXT).build().toString();
            openAMClient = crf.createProxy(OpenAMClient.class, openamUrl);
        }
    }

    @POST
    @Path("json/sessions/{tokenId}")
    public Response validateToken(@PathParam("tokenId") String tokenId, @QueryParam("_action") String action) {
        if ("validate".equals(action)) {
            try {
                initialize();
                ClientResponse<String> isTokenValidResponse = openAMClient.isTokenValid(tokenId);
                String valid = (String) isTokenValidResponse.getEntity(String.class);
                if (valid.contains("boolean=true")) {
                    String username = "";
                    ClientResponse<String> getUsersResponse = openAMClient.getAttributes(tokenId, "uid");
                    String users = (String) getUsersResponse.getEntity(String.class);
                    for (String argument : users.split("\\n")) {
                        if (argument.startsWith("userdetails.attribute.value")) {
                            String[] values = argument.split("=");
                            username = values[1];
                        }
                    }
                    return buildValidateTokenResponse(true, username, "/");
                } else {
                    return buildValidateTokenResponse(false, null, null);
                }
            } catch (Exception ex) {
                logger.error("There was an error in validateToken method, returning false", ex);
                return buildValidateTokenResponse(false, null, null);
            }
        }
        return Response.noContent().build();
    }

    private Response buildValidateTokenResponse(boolean valid, String uid, String realm) {
        ValidateTokenResponse response = new ValidateTokenResponse();
        response.setRealm(realm);
        response.setUid(uid);
        response.setValid(valid);
        return Response.ok().entity(response).build();
    }

    @GET
    @Path("json/users/{uid}")
    public Response getUserFields(@PathParam("uid") String uid, @QueryParam("_fields") String fields) {
        UsersResponse response = new UsersResponse();
        response.setUid(new String[] { uid });
        return Response.ok().entity(response).build();
    }
}
