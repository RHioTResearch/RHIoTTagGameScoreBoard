package org.jboss.rhiot.simulators.kura;

import java.util.Dictionary;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import static io.undertow.servlet.Servlets.servlet;

/**
 * Created by starksm on 6/20/16.
 */
public class UndertowHttpService implements HttpService {
    private Undertow server;

    public UndertowHttpService() {
    }

    @Override
    public void registerServlet(String path, Servlet servlet, Dictionary dictionary, HttpContext httpContext) throws ServletException, NamespaceException {
        DeploymentInfo servletBuilder = deployment()
                .setClassLoader(UndertowHttpService.class.getClassLoader())
                .setContextPath(path)
                .setDeploymentName("rhiot.war")
                .addServlets(
                        servlet("RHIoTServlet", servlet.getClass(), new RHIoTServletFactory(servlet))
                                .addInitParam("message", servlet.getClass().getName())
                                .addMapping("/*"));

        DeploymentManager manager = defaultContainer().addDeployment(servletBuilder);
        manager.deploy();

        HttpHandler servletHandler = manager.start();
        PathHandler pathHandler = Handlers.path(Handlers.redirect(path))
                .addPrefixPath(path, servletHandler);
        server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(pathHandler)
                .build();
        server.start();
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
