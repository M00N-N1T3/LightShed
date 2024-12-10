package wethinkcode.web;

import com.google.common.annotations.VisibleForTesting;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.rendering.template.JavalinThymeleaf;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import wethinkcode.places.PlaceNameService;
import wethinkcode.web.router.Router;

import java.util.ArrayList;
import java.util.List;

/**
 * I am the front-end web server for the LightSched project.
 * <p>
 * Remember that we're not terribly interested in the web front-end part of this
 * server, more in the way it communicates and interacts with the back-end
 * services.
 */
public class WebService
{

    public static final int DEFAULT_PORT = 8011;
    public static final String SESSION_USER_KEY = "user";
    public static final String PAGES_DIR = "/html";
    public static final String TEMPLATES_DIR = "/templates/";
    private List<String> provinces;

    private TemplateEngine templateEngine(){
        TemplateEngine templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        // setting the prefix to where my template is
        resolver.setPrefix(TEMPLATES_DIR);
        // setting my resolver
        templateEngine.setTemplateResolver(resolver);
        templateEngine.addDialect(new LayoutDialect());
        return templateEngine;
    }

    public static void main( String[] args ){
        final WebService svc = new WebService().initialise();
        svc.start();
    }

    private Javalin webServer;

    private int servicePort;

    @VisibleForTesting
    WebService initialise(){
        JavalinThymeleaf.configure(templateEngine());
        webServer = Javalin.create(javalinConfig -> {
            javalinConfig.addStaticFiles(PAGES_DIR, Location.CLASSPATH);
            javalinConfig.defaultContentType = "application/json";
//            javalinConfig.enableDevLogging();
            javalinConfig.showJavalinBanner = false;
        });

        Router.configure(this);
        // TODO: add http client and server configuration here
        return this;
    }

    public void routes(EndpointGroup group) {
        webServer.routes(group);
    }

    public void start(){
        start( DEFAULT_PORT );
    }

    @VisibleForTesting
    void start( int networkPort ){
        servicePort = networkPort;
        run();
    }

    public void stop(){
        webServer.stop();
    }

    public void run(){
        webServer.start( servicePort );
    }

    private void configureHttpClient(){
        throw new UnsupportedOperationException( "TODO" );
    }

    private Javalin configureHttpServer(){
        throw new UnsupportedOperationException( "TODO" );
    }
}
