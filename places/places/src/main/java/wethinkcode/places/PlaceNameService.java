package wethinkcode.places;


import com.google.common.annotations.VisibleForTesting;
import io.javalin.Javalin;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import wethinkcode.common.configurator.Configurator;

import wethinkcode.places.fileloader.FileLoader;
import wethinkcode.places.model.Places;
import wethinkcode.places.router.Router;


import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * I provide a Place-names Service for places in South Africa.
 * <p>
 * I read place-name data from a CSV file that I read and
 * parse into the objects (domain model) that I use,
 * discarding unwanted data in the file (things like mountain/river names). With my "database"
 * built, I then serve-up place-name data as JSON to clients.
 * <p>
 * Clients can request:
 * <ul>
 * <li>a list of available Provinces
 * <li>a list of all Towns/PlaceNameService in a given Province
 * <li>a list of all neighbourhoods in a given Town
 * </ul>
 * I understand the following command-line arguments:
 * <dl>
 * <dt>-c | --config &lt;configfile&gt;
 * <dd>a file pathname referring to an (existing!) configuration file in standard Java
 *      properties-file format
 * <dt>-d | --datadir &lt;datadirectory&gt;
 * <dd>the name of a directory where CSV datafiles may be found. This option <em>overrides</em>
 *      and data-directory setting in a configuration file.
 * <dt>-p | --places &lt;csvdatafile&gt;
 * <dd>a file pathname referring to a CSV file of place-name data. This option
 *      <em>overrides</em> any value in a configuration file and will bypass any
 *      data-directory set via command-line or configuration.
 */
@CommandLine.Command(
        name= "Places",
        mixinStandardHelpOptions = true,
        version = "1.0",
        description = "Place-names Service for places in South Africa")
public class PlaceNameService implements Runnable {

    // server related fields
    public static final int DEFAULT_SERVICE_PORT = 7000;
    private Javalin server;
    private Places places;
    private int PORT;
    private File csvFile;
    private File configFile;

    // Configuration keys
    public static  String CFG_CONFIG_FILE = "config.file";
    public static  String CFG_DATA_DIR = "data.dir";
    public static  String CFG_DATA_FILE = "data.file";
    public static  String CFG_SERVICE_PORT = "server.port";
    private static final String APP_DIR = "places";
    private static Configurator configurator;

    private String sessionConfigFile = "";
    private String sessionCSVFile = "";




    // cli arguments and options
    @Option(
            names = {"-c","--config"},
            defaultValue = "",
            description = "a file pathname referring to an (existing!) configuration file in standard Java properties-file format")
    private String configfile = "";

    @Option(
            names = {"-d","--datadir"},
            defaultValue = "",
            description = "the name of a directory where CSV datafiles may be found")
    private String datadir = "" ;

    @Option(
            names = {"-f","--file"},
            defaultValue = "",
            description = "csvdatafile that contains the places"
    )
    private String csvdatafile ="" ;

    @Option(
            names = {"-p","--port"},
            defaultValue = "0",
            description = "The port number to start the server on"
    )
    private int port;



    public static void main( String[] args ){
        final PlaceNameService svc = new PlaceNameService();
        final int exitCode = new CommandLine( svc ).execute( args );
//        System.exit( exitCode );
    }


    // Instance state
    public PlaceNameService(){
        preLoad();
    }

    private void preLoad(){
        setupConfigurator();
        loadConfigPreLoad();
        setSessionConfigs();
    }

    /**
     * loads the configuration files of our server. This includes the ports and the files for the database
     */
    private void finalizeConfiguration(){
        configurator.setPORT(port);
        this.PORT = configurator.getPORT();
        if (this.csvFile != null) saveSessionConfig();
    }

    private void loadConfigPreLoad(){
        configurator.loadDataDirs();
        FileLoader fileLoader = new FileLoader(configurator);
        this.configFile = fileLoader.loadProperties();
        this.csvFile = fileLoader.loadCSVFile();
    }

    private void setSessionConfigs(){
        sessionCSVFile = csvFile.getPath();
        sessionConfigFile = configFile.getPath();
    }


    private Configurator setupConfigurator(){
        configurator = new Configurator(CFG_CONFIG_FILE
                ,CFG_SERVICE_PORT
                ,CFG_DATA_FILE
                ,CFG_DATA_DIR
                ,APP_DIR);

        configurator.setPathToPropertyFileRelaventToCWD("places.properties");
        configurator.setCSV_FILE_DIR(datadir);
        configurator.setCSV_FILE_NAME(csvdatafile);

        return configurator;
    }

    public String getConfig(String config){
        return configurator.getConfig(config,configFile);
    }


    private void saveSessionConfig(){
        // save the current session configuration
        String dataDirParentPath = configFile.getParent();
        String csvFileAbsolutePath = csvFile.getAbsolutePath();
        String configFileAbsolutePath = configFile.getAbsolutePath();

        Map<String, String> configData = Map.of(
                CFG_CONFIG_FILE,configFileAbsolutePath,
                CFG_DATA_DIR,dataDirParentPath,
                CFG_DATA_FILE, csvFileAbsolutePath);

        configurator.saveConfiguration(new LinkedHashMap<>(configData), configFileAbsolutePath);
    }


    public Places getDb(){
//        this.initialise();
        return places;
    }
    public void start(int port){
        if (preConfigurationHook()) preLoad();
        finalizeConfiguration();
        this.PORT = port;
        server.start(this.PORT);
    }

    private void reconfigureProperties(){
        String csvPath = csvFile.getAbsolutePath();
    }

    public void start(){
        this.initialise();
        server.start(this.PORT);

    }

    public File configFile(){
        String dataFile = (configfile.isEmpty()) ? sessionConfigFile : configfile;
        return new File(dataFile);
    }

    public File dataFile(){
        String dataFile = (csvdatafile.isEmpty()) ? sessionCSVFile : csvdatafile;
        return new File(dataFile);
    }

    public File dataDir(){
        return new File(datadir);
    }

    public void stop(){
        server.stop();
    }

    /**
     * Why not put all of this into the constructor? Well, this way makes
     * it easier (possible!) to test an instance of PlaceNameService without
     * starting up all the big machinery (i.e. without calling initialise()).
     */
    @VisibleForTesting
    PlaceNameService initialise(){
        if (preConfigurationHook()) preLoad();
        finalizeConfiguration();
        places = initPlacesDb();
        server = initHttpServer();
        return this;
    }

    @VisibleForTesting
    PlaceNameService initialise(Places places){
        this.places = places;
        server = initHttpServer();
        return this;
    }

    @Override
    public void run(){
        start();

    }

    private Places initPlacesDb(){
        Places database = new PlacesCsvParser().parseCsvSource(csvFile);
        return database;
    }

    private boolean preConfigurationHook() {
         return verifyFileExistance(csvdatafile) || verifyFileExistance(configfile);
    }

    private boolean verifyFileExistance(String filepath){
        boolean loader;
        loader = (!filepath.isEmpty()); // if it is empty loader remains false
        if (loader) loader = new File(filepath).exists(); // checking whether it exists

        return loader;
    }


    private Javalin initHttpServer(){
        Javalin server =  Javalin.create();
        Router.getRoutes(server,places);
        return server;
    }

    @VisibleForTesting
    protected Places getPlaces(){
        return places;
    }
}