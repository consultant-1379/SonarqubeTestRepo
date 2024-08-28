package com.ericsson.oss.itpf.security.sso.ejb.sysinit;

import java.io.BufferedReader;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.*;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.slf4j.Logger;

/**
 * Created by epatjuc on 4/14/2016.
 */
@PrepareForTest(ServiceBootstrap.class)
public class ServiceBootstrapTest {

    @Mock
    private Logger logger;

    @InjectMocks
    private ServiceBootstrap serviceBootstrap = Mockito.spy(ServiceBootstrap.class);

    @Test
    public void testOnServiceStart() throws Exception {

        final BufferedReader bufferedReader = Mockito.mock(BufferedReader.class);
        Mockito.when(bufferedReader.readLine()).thenReturn("DDC_ON_CLOUD=false").thenReturn("UI_PRES_SERVER=enmapache.athtem.eei.ericsson.se")
                .thenReturn("sso_instances=sso-instance-1,sso-instance-2").thenReturn(null).thenReturn("sso_instances=sso-instance-1,sso-instance-2")
                .thenReturn(null);

        serviceBootstrap = Mockito.spy(ServiceBootstrap.class);
        serviceBootstrap.setLogger(Mockito.mock(Logger.class));
        Mockito.doReturn(bufferedReader).when(serviceBootstrap).getBufferedReader();

        serviceBootstrap.onServiceStart();
        //Verify result
        final List<String> ssoInstancesUrls = serviceBootstrap.getSsoInstancesUrls();
        Assert.assertEquals(2, ssoInstancesUrls.size());
        Assert.assertEquals("http://sso-instance-1.enmapache.athtem.eei.ericsson.se:8080/heimdallr", ssoInstancesUrls.get(0));
        Assert.assertEquals("http://sso-instance-2.enmapache.athtem.eei.ericsson.se:8080/heimdallr", ssoInstancesUrls.get(1));
    }

}
