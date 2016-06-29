package org.jboss.rhiot.simulators.kura;

import javax.servlet.Servlet;

import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.InstanceHandle;

/**
 * Created by starksm on 6/20/16.
 */
public class RHIoTServletFactory implements InstanceFactory<Servlet> {
    private Servlet servlet;

    public RHIoTServletFactory(Servlet servlet) {
        this.servlet = servlet;
    }
    @Override
    public InstanceHandle<Servlet> createInstance() throws InstantiationException {
        return new InstanceHandle<Servlet>() {

            @Override
            public Servlet getInstance() {
                return servlet;
            }

            @Override
            public void release() {

            }
        };
    }
}
