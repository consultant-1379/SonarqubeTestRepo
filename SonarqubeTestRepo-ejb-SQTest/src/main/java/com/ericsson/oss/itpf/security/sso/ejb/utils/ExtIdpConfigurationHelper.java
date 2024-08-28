package com.ericsson.oss.itpf.security.sso.ejb.utils;

import com.ericsson.oss.itpf.security.sso.ejb.listeners.ExternalIdpConfigProvider;
import org.slf4j.Logger;

import javax.ejb.LocalBean;
import javax.inject.Inject;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

@LocalBean
public class ExtIdpConfigurationHelper {

    @Inject
    Logger logger;

    @Inject
    ExternalIdpConfigProvider provider;

    public void setParam(String parameter) throws Exception{


            BeanInfo beaninfo = Introspector.getBeanInfo(ExternalIdpConfigProvider.class);
            PropertyDescriptor pds[] = beaninfo.getPropertyDescriptors();
            Method setterMethod=null;
            Method getterMethod=null;
            boolean found = false;
            for(PropertyDescriptor pd : pds) {
                if(pd.getName().equals(parameter)) {

                    found=true;
                    setterMethod = pd.getWriteMethod();
                    getterMethod = pd.getReadMethod();

                    if (getterMethod==null && setterMethod == null) {
                        logger.error("{} Could no find getter and setter methods for: {}", getClass().getName(), parameter);
                        throw new Exception();

                    } else {
                        //read from PIB modeled value
                        String value = (String) getterMethod.invoke(provider);

                        //set to openAM
                        if(value!=null)
                            setterMethod.invoke(provider, value);
                    }
                }
            }

            if(!found){
                logger.error("{} Could no find getter and setter methods for: {}", getClass().getName(), parameter);
                throw new Exception();
            }

    }

}
