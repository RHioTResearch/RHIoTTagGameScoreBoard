package org.jboss.rhiot.simulators.kura;

import java.util.Dictionary;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

public class MockHttpService implements HttpService {
    @Override
    public void registerServlet(String s, Servlet servlet, Dictionary dictionary, HttpContext httpContext) throws ServletException, NamespaceException {

    }

    @Override
    public void registerResources(String s, String s1, HttpContext httpContext) throws NamespaceException {

    }

    @Override
    public void unregister(String s) {

    }

    @Override
    public HttpContext createDefaultHttpContext() {
        return null;
    }
}
