package com.ericsson.oss.itpf.security.sso.rest.session;

import com.ericsson.oss.itpf.security.sso.ejb.services.UserManagementService;
import com.ericsson.oss.itpf.security.sso.rest.io.ActiveUsersResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by epatjuc on 4/15/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserManagementRestTest {

    @Mock
    private UserManagementService userManagementService;

    @InjectMocks
    private UserManagementRest userManagementRest = new UserManagementRest();

    @Test
    public void testGetActiveUsers() {
        //Mock userManagementService.getActiveUsers();
        Map<String, Integer> userList = new HashMap<>();
        userList.put("user1", 1);
        userList.put("user2", 3);
        Mockito.doReturn(userList).when(userManagementService).getActiveUsers();

        //Invoke tested method
        Response response = userManagementRest.getActiveUsers();

        //Verify result
        Assert.assertEquals(200, response.getStatus());
        GenericEntity<ActiveUsersResponse> genericEntity = (GenericEntity<ActiveUsersResponse>) response.getEntity();
        ActiveUsersResponse activeUsersResponse = genericEntity.getEntity();
        Assert.assertEquals(2, activeUsersResponse.users.size());
        Assert.assertEquals(1, activeUsersResponse.users.get("user1"));
        Assert.assertEquals(3, activeUsersResponse.users.get("user2"));
        Mockito.verify(userManagementService, Mockito.times(1)).getActiveUsers();
    }

    @Test
    public void testDeactivateUser() {
        //Mock userManagementService.deactivateUser
        Mockito.doNothing().when(userManagementService).deactivateUser(Mockito.anyString());

        //Invoke tested method
        Response response = userManagementRest.deactivateUser("user1");

        //Verify result
        Assert.assertEquals(204, response.getStatus());
        Mockito.verify(userManagementService, Mockito.times(1)).deactivateUser("user1");
    }
}
