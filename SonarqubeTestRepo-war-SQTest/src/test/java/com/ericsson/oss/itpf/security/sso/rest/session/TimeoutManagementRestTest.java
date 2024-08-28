package com.ericsson.oss.itpf.security.sso.rest.session;

import com.ericsson.oss.itpf.security.sso.ejb.services.TimeoutManagementService;
import com.ericsson.oss.itpf.security.sso.rest.TimeoutsJAXB;
import com.ericsson.oss.itpf.security.sso.utils.InvalidInputException;
import com.ericsson.oss.itpf.security.sso.utils.Timeouts;
import org.codehaus.jackson.JsonParseException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import java.io.IOException;


/**
 * Created by epatjuc on 4/12/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class TimeoutManagementRestTest {

    @Mock
    TimeoutManagementService timeoutManagementService;

    @InjectMocks
    private TimeoutManagementRest timeoutManagementRest = new TimeoutManagementRest();

    @Test
    public void testGetTimeouts() {
        //Mock timeoutManagementService.getTimeouts()
        Mockito.when(timeoutManagementService.getTimeouts()).thenReturn(new Timeouts(1000000l, 60, 600));

        //Invoke tested method
        TimeoutsJAXB response = timeoutManagementRest.getTimeouts();

        //Verify result
        Assert.assertEquals("1000000", response.getTimestamp());
        Assert.assertEquals("60", response.getIdleTimeout());
        Assert.assertEquals("600", response.getMaxTimeout());
        Mockito.verify(timeoutManagementService, Mockito.times(1)).getTimeouts();
    }

    @Test
    public void testSetTimeouts() throws IOException, InvalidInputException {
        //Mock timeoutManagementService.setTimeouts()
        Mockito.when(timeoutManagementService.setTimeouts(1000000l, 500, 50)).thenReturn(9000000l);

        //Invoke tested method
        TimeoutsJAXB response = timeoutManagementRest.setTimeouts(new TimeoutsJAXB(new Timeouts(1000000l, 50, 500)));

        //Verify result
        Assert.assertEquals("9000000", response.getTimestamp());
        Assert.assertEquals("50", response.getIdleTimeout());
        Assert.assertEquals("500", response.getMaxTimeout());
        Mockito.verify(timeoutManagementService, Mockito.times(1)).setTimeouts(1000000l, 500, 50);
    }

    /**
     The testcase verifies that method setTimeouts(TimeoutsJAXB) of class TimeoutManagementRest throws JsonParseException
     when method setTimeouts(Long, int, int) of TimeoutManagementService throws  InvalidInputException.
     */
    @Test(expected=JsonParseException.class)
    public void testSetTimeoutInconsistentTimestampException() throws IOException, InvalidInputException {
        //Mocks
        Mockito.when(timeoutManagementService.setTimeouts(1000000l, 500, 50)).thenThrow(InvalidInputException.class);

        //Invoke tested method
        timeoutManagementRest.setTimeouts(new TimeoutsJAXB(new Timeouts(1000000l, 50, 500)));
    }

    /**
     The testcase verifies that method setTimeouts(TimeoutsJAXB) of class TimeoutManagementRest throws JsonParseException
     when method setTimeouts(Long, int, int) of TimeoutManagementService throws  IOException.
     */
    @Test(expected=JsonParseException.class)
    public void testSetTimeoutIOException() throws IOException, InvalidInputException {
        //Mocks
        Mockito.when(timeoutManagementService.setTimeouts(1000000l, 500, 50)).thenThrow(IOException.class);

        //Invoke tested method
       timeoutManagementRest.setTimeouts(new TimeoutsJAXB(new Timeouts(1000000l, 50, 500)));
    }

}
