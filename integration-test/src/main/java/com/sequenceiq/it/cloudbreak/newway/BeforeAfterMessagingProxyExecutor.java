package com.sequenceiq.it.cloudbreak.newway;

import static com.sequenceiq.it.cloudbreak.newway.log.Log.log;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;

import javax.ws.rs.WebApplicationException;

import com.sequenceiq.it.cloudbreak.exception.ProxyMethodInvocationException;

public final class BeforeAfterMessagingProxyExecutor extends GenericProxyExecutor {

    @Override
    public <R> R exec(Callable<R> method) {
        try {
            return method.call();
        } catch (InvocationTargetException ite) {
            if (ite.getCause() instanceof WebApplicationException) {
                String content = ((WebApplicationException) ite.getCause()).getResponse().readEntity(String.class);
                log(content);
                throw (WebApplicationException) ite.getCause();
            } else {
                throw new ProxyMethodInvocationException(getGenericExceptionMessage(), ite);
            }
        } catch (Exception e) {
            throw new ProxyMethodInvocationException(getGenericExceptionMessage(), e);
        }
    }
}
