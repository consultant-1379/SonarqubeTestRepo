package com.ericsson.oss.itpf.security.sso.rest.session;

import com.ericsson.oss.itpf.security.sso.ejb.qualifiers.Rest;
import com.ericsson.oss.itpf.security.sso.ejb.qualifiers.Sdk;

import com.ericsson.oss.itpf.security.sso.ejb.services.UserManagementService;
import com.ericsson.oss.itpf.security.sso.rest.io.ActiveUsersResponse;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by ejakgub on 2015-11-27.
 */


@Path("/")
public class UserManagementRest {

  @Inject
  @Sdk
  private UserManagementService userManagementServiceSdk;


  @GET
  @Path("/users")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getActiveUsers() {
    ActiveUsersResponse auResp = new ActiveUsersResponse(userManagementServiceSdk.getActiveUsers());
    GenericEntity<ActiveUsersResponse> activeUsersEntity = new GenericEntity<ActiveUsersResponse>(auResp) { };
    Response reponse = Response.ok(activeUsersEntity).build();
    return reponse;
  }

  @DELETE
  @Path("/users/{user_name}")
  public Response deactivateUser(@PathParam("user_name") String userId) {
    userManagementServiceSdk.deactivateUser(userId);
    return Response.ok().status(204).build();
  }

}
