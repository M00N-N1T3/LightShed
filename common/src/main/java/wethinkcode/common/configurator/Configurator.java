package wethinkcode.common.configurator;

import java.io.File;
import java.util.*;

import static wethinkcode.common.configurator.PropertiesFile.*;


public class Configurator {

    private String CFG_SERVICE_PORT;
    private String CFG_DATA_DIR;
    private String CFG_DATA_FILE;


    private String CFG_CONFIG_FILE;
    private final String USER_HOME;
    private final String CWD;
    private String APP_DIR ;
    private static String propertyFileName = "";

    private String CSV_FILE_DIR = "";
    private String CSV_FILE_NAME = "";
    private int PORT;
    private boolean loadCustom = false;
    private int DEFAULT_PORT;


public Configurator(String CFG_CONFIG_FILE, String CFG_SERVICE_PORT, String CFG_DATA_FILE,String CFG_DATA_DIR, String APP_DIR){
        this.CFG_CONFIG_FILE = CFG_CONFIG_FILE;
        this.CFG_DATA_DIR = CFG_DATA_DIR;
        this.CFG_DATA_FILE = CFG_DATA_FILE;
        this.CFG_SERVICE_PORT = CFG_SERVICE_PORT;
        this.APP_DIR = APP_DIR;
        this.CWD = System.getProperty("user.dir");
        this.USER_HOME = System.getenv("HOME");
    }


    public Configurator(String CFG_CONFIG_FILE, String CFG_SERVICE_PORT, String CFG_DATA_DIR, String APP_DIR, int PORT){
        this.CFG_CONFIG_FILE = CFG_CONFIG_FILE;
        this.CFG_DATA_DIR = CFG_DATA_DIR;
        this.CFG_SERVICE_PORT = CFG_SERVICE_PORT;
        this.APP_DIR = APP_DIR;
        this.CWD = System.getProperty("user.dir");
        this.USER_HOME = System.getenv("HOME");
        this.PORT = PORT;
        loadPort(this.PORT);
    }

    public void loadDataDirs(){
        if (loadCustom){
            loadCustomDirs();
        }else{
            loadDefaultDirs();}
    }

    public void setPORT(int PORT){
        this.PORT = loadPort(PORT);
    }

    public void setPORT(String port){
        this.PORT = Integer.parseInt(port);
    }

    public void loadDefaultDirs(){
        CSV_FILE_DIR =  (CSV_FILE_DIR.isEmpty()) ? getProperty(CFG_DATA_DIR,CWD,APP_DIR) : CSV_FILE_DIR;
        CSV_FILE_NAME = (CSV_FILE_NAME.isEmpty()) ? getProperty(CFG_DATA_FILE,CWD, APP_DIR) : CSV_FILE_NAME;
    }

    private void loadCustomDirs(){
        loadCustom = true;
        // we are checking whether the CWD is loading from our main folder such as ../places or ../stages
        // if the folder not the same as the APP_DIR (which would be stages or places) then we will append the APP_DIR to the end of CWD
        File serviceFolder = (Arrays.stream(CWD.split("/")).toList().contains(APP_DIR))
                ? new File(CWD) : new File(CWD,APP_DIR);

        boolean success = setFileDIRAndName(serviceFolder);
        if (!success) {
            loadBackupProperties(serviceFolder);
        }
    }

    private void loadBackupProperties(File serviceFolder){
        File backup = new File(serviceFolder.getPath(),"backup");
        setFileDIRAndName(backup);
    }

    private boolean setFileDIRAndName(File serviceFolder){
        if (CSV_FILE_NAME == null) CSV_FILE_NAME = "";
        if (CSV_FILE_DIR == null) CSV_FILE_DIR = ""; // safety nets

        CSV_FILE_DIR = (CSV_FILE_DIR.isEmpty()) ? getProperty(CFG_DATA_DIR,serviceFolder.getPath(), propertyFileName) : CFG_DATA_DIR ;
        CSV_FILE_NAME = (CSV_FILE_NAME.isEmpty()) ? getProperty(CFG_DATA_FILE, serviceFolder.getPath(),propertyFileName) : CSV_FILE_NAME;

        // FIXED
        if (!new File(CSV_FILE_DIR).exists()) CSV_FILE_DIR = correctFileDir(CSV_FILE_DIR);
        if (!new File(CSV_FILE_NAME).exists()) CSV_FILE_NAME =  correctFileDir(CSV_FILE_DIR);

        return !(CSV_FILE_DIR == null) || !(CSV_FILE_NAME == null);
    }
    public int getPORT(){
        return PORT;
    }

    public void setCSV_FILE_DIR(String CSV_FILE_DIR) {
        this.CSV_FILE_DIR = CSV_FILE_DIR;
    }

    public void setCSV_FILE_NAME(String CSV_FILE_NAME) {
        this.CSV_FILE_NAME = CSV_FILE_NAME;
    }

    public String getCWD(){
        return CWD;
    }

    public String getUSER_HOME(){
        return USER_HOME;
    }

    public String getAPP_DIR(){
        return APP_DIR;
    }

    public String getCSVFileName() {
        return CSV_FILE_NAME;
    }

    public String getCSV_FILE_DIR() {
        return CSV_FILE_DIR;
    }

    private String correctFileDir(String dir){
        List<String> cwdFolders = new ArrayList<> (Arrays.stream(CWD.split("/")).skip(1L).toList());
        List<String> CSV_FILE_DIR_Folders = Arrays.stream(dir.split("/")).skip(1L).toList();

        // app folder could be something like loadshedding-2, and it derives from the cwd
        String appFolder = cwdFolders.getLast();
        int indexOfAppFolder = CSV_FILE_DIR_Folders.indexOf(appFolder);

        // next we create a new list containing just what comes after the appFolder from our CSV_FILE_DIR list
        List<String> auxList = new ArrayList<>(CSV_FILE_DIR_Folders.stream() // ArrayLists makes the list mutable
                .skip(Long.parseLong(String.valueOf(indexOfAppFolder))).toList()); // creating a list that consists of just the last 2 elements of our orignal list (CSV_FILE_DIR_FOLDERS);

        String correctedDir = buildDirFromLists(cwdFolders,auxList);
        return correctedDir;
    }

    private String buildDirFromLists(List<String> mainList, List<String> secondaryList){
        StringBuilder sb = new StringBuilder();
        mainList.addAll(secondaryList);
        // combining our two lists
        Set<String> dir = new LinkedHashSet<>(mainList);


        for (String string : dir){
            sb.append("/").append(string);//appending the forward slash before the string is appended to the builder
        }

        return sb.toString();
    }

    public String getCFG_CONFIG_FILE() {
        return CFG_CONFIG_FILE;
    }

    public String getCFG_DATA_FILE() {
        return CFG_DATA_FILE;
    }

    public String getCFG_DATA_DIR() {
        return CFG_DATA_DIR;
    }

    public String getCFG_SERVICE_PORT() {
        return CFG_SERVICE_PORT;
    }

    public String getConfig(String config){
        return getProperty(config);
    }

    public String getConfig(String config, File propertyFle){
        return getProperty(config, propertyFle);
    }

    public void setPathToPropertyFileRelaventToCWD(String propertyFileName){
        loadCustom = true;
        Configurator.propertyFileName = propertyFileName;
    }

    public String getPropertyFilePath(){
        return propertyFileName;
    }

    private int loadPort(int port){
        PORT = (port == 0)
//                ? Integer.parseInt(getProperty(CFG_SERVICE_PORT, APP_DIR,propertyFileName))
                ? Integer.parseInt(getProperty(CFG_SERVICE_PORT, CWD ,propertyFileName))
                : PORT;
        return PORT;
    }

    public boolean saveConfiguration(String key, String value){
        return writeProperty(key,value);
    }

    public boolean saveConfiguration(LinkedHashMap<String, String> data, String configFIleAbsPath){
        return writeProperties(data, configFIleAbsPath);
    }

}
