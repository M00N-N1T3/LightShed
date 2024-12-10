package wethinkcode.stage;

import com.google.common.annotations.VisibleForTesting;
import io.javalin.Javalin;
import wethinkcode.common.configurator.Configurator;
import wethinkcode.common.transfer.StageDO;
import wethinkcode.stage.router.Router;

/**
 * I provide a REST API that reports the current loadshedding "stage". I provide
 * two endpoints:
 * <dl>
 * <dt>GET /stage
 * <dd>report the current stage of loadshedding as a JSON serialisation
 *      of a {@code StageDO} data/transfer object
 * <dt>POST /stage
 * <dd>set a new loadshedding stage/level by POSTing a JSON-serialised {@code StageDO}
 *      instance as the body of the request.
 * </ul>
 */
public class StageService implements Runnable
{
    public static final int DEFAULT_STAGE = 0; // no loadshedding. Ha!

    private int loadSheddingStage;
    private Javalin server;
    public static int DEFAULT_PORT = 7001;
    private String APP_DIR = "stage";
    private int servicePort;
    private StageDO stageDO = new StageDO();

    // Configuration keys
    public static final String CFG_CONFIG_FILE = "config.file";
    public static final String CFG_DATA_DIR = "data.dir";
    public static final String CFG_SERVICE_PORT = "server.port";


    public static void main( String[] args ){
        final StageService svc = new StageService().initialise();
        svc.start();
    }


    @VisibleForTesting
    StageService initialise(){
        return initialise( DEFAULT_STAGE );
    }

    @VisibleForTesting
    StageService initialise( int initialStage ){
        loadConfiguration();
        loadSheddingStage = initialStage;
        assert loadSheddingStage >= 0;
        server = initHttpServer();
        return this;
    }

    public void start(){
        start(DEFAULT_PORT);
    }

    @VisibleForTesting
    void start( int networkPort ){
        servicePort = networkPort;
        run();
    }

    public void stop(){
        server.stop();
    }

    @Override
    public void run(){
        server.start( servicePort );
    }

    private Javalin initHttpServer(){
        server = Javalin.create( javalinConfig -> {
//            javalinConfig.enableDevLogging();
            javalinConfig.showJavalinBanner = false;
        });
        stageDO.setStage(loadSheddingStage);
        Router.getRoutes(server,stageDO);
        return server;
    }

    /**
     * loads the configuration files of our server. This includes the ports and the files for the database
     */
    private void loadConfiguration(){

        Configurator configurator = new Configurator(CFG_CONFIG_FILE
                ,CFG_SERVICE_PORT
                ,CFG_DATA_DIR
                ,APP_DIR
                , DEFAULT_PORT);

        DEFAULT_PORT = configurator.getPORT();
    }

}
