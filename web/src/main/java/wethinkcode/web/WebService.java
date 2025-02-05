package wethinkcode.web;

import com.google.common.annotations.VisibleForTesting;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.rendering.template.JavalinThymeleaf;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import wethinkcode.loadshed.common.mq.listener.ServiceTopicListener;
import wethinkcode.loadshed.common.transfer.StageDO;
import wethinkcode.web.router.Router;


/**
 * I am the front-end web server for the LightShed project.
 * <p>
 * Remember that we're not terribly interested in the web front-end part of this
 * server, more in the way it communicates and interacts with the back-end
 * services.
 */
public class WebService
{

    public static final int DEFAULT_PORT = 8011;
    public static final String PAGES_DIR = "/html";
    public static final String TEMPLATES_DIR = "/templates/";
    private static final StageDO stageDO = new StageDO(0);
    private static ServiceTopicListener webServiceTopicListener;

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
        configureHttpClient();
        webServer = configureHttpServer();
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
        webServiceTopicListener.run();
        run();
    }

    public void stop(){
        webServer.stop();
    }

    public void run(){
        webServer.start( servicePort );
    }

    private void configureHttpClient(){
        JavalinThymeleaf.configure(templateEngine());
        webServer = Javalin.create(javalinConfig -> {
            javalinConfig.addStaticFiles(PAGES_DIR, Location.CLASSPATH);
            javalinConfig.defaultContentType = "application/json";
//            javalinConfig.enableDevLogging();
            javalinConfig.showJavalinBanner = false;
        });

        webServiceTopicListener = new ServiceTopicListener("stage",webServer);
    }

    private Javalin configureHttpServer(){
        webServer.attribute("stage",stageDO);
        Router.configure(this);
        return webServer;
    }

    public Javalin getWebServer() {
        return webServer;
    }
}
