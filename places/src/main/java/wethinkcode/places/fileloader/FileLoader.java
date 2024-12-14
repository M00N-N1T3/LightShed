package wethinkcode.places.fileloader;

import wethinkcode.loadshed.common.configurator.Configurator;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static wethinkcode.loadshed.common.configurator.PropertiesFile.getProperty;
import static wethinkcode.loadshed.common.configurator.PropertiesFile.getPropertyFromBackup;

public class FileLoader {
    private static File CSVFILE;

    // these come from our configurator object
    private String USER_HOME;
    private String CWD;
    private String APP_DIR ;
    private String DATA_DIR;
    private String CSV_FILE_NAME;
    private String CONFIGFILE;




    public  FileLoader(Configurator configurator){
        CWD = configurator.getCWD();
        USER_HOME = configurator.getUSER_HOME();
        APP_DIR = configurator.getAPP_DIR();
        DATA_DIR = configurator.getCSV_FILE_DIR();
        CSV_FILE_NAME = configurator.getCSVFileName();
        CONFIGFILE = configurator.getPropertyFilePath();
    }

    public FileLoader(Configurator configurator,String mainDir, String propertiesFile){
        CWD = configurator.getCWD();
        USER_HOME = configurator.getUSER_HOME();
        APP_DIR = configurator.getAPP_DIR();
        DATA_DIR = (mainDir.equals(configurator.getCSV_FILE_DIR())) ? configurator.getCSV_FILE_DIR() : mainDir;
        CSV_FILE_NAME = configurator.getCSVFileName();
        CONFIGFILE = configurator.getPropertyFilePath();
    }

    public  File loadCSVFile(){
        return CSVFILE = loadFile();
    };

    public File loadProperties(){
        return new File(DATA_DIR, CONFIGFILE);
    }

    public String getAbsPath() {
        return CSVFILE.getAbsolutePath();
    }

    private File loadFile(){
        File csvFile;
        File file = new File(DATA_DIR); // the directory specified by the user

        //check if just the csvdatafile  path leads to the file
        csvFile = retriveFile(CSV_FILE_NAME);
        if (validateFile(csvFile)) return csvFile;

        // quick search in the event the  datadir + csvdatafile file does exist and lead to our csv
        csvFile = retriveFile(file, CSV_FILE_NAME);
        if (validateFile(csvFile)) return csvFile;

        // Chances of searching for the file failing here is slim to none. But chances are that it might
        csvFile = findUsingParentDirAndFileName(file, CSV_FILE_NAME); // note that there is indeed a recursive call in here
        if(validateFile(csvFile)) return csvFile;


        // lastly if we do not find the file we want we will load from the resources
        csvFile =  forceLoadFromBackup();
        if (validateFile(csvFile)) return csvFile;

        return new File(" "); // TODO: HOLD THIS THOUGHT WE NEED TO SET UP A BACK UP FOR OUR RESOURCES
    }


   // TODO: discontinued
    private File loadUsingDFS(File file){
        File csvFile;
        // dfs algo using recursive technique
        File searchedFileDir = getFileUsingDepthSearch(USER_HOME,file.getName()); // searching for the specified directory
        if (validateMainDir(searchedFileDir)){ // if the directory does exist
            csvFile = retriveFile(searchedFileDir, CSV_FILE_NAME); // trying to get the csv file from that dir
            if (validateFile(csvFile)) return csvFile;
        }

        return new File("");
    }

    // TODO: discontinued
    private File fullSystemSearch(){
        // let's assume that the file we want has been moved to elsewhere. We will still attempt searching for it, directly from home
        // finally searching the entire pc
        File fullSearch = getFileUsingDepthSearch(USER_HOME, CSV_FILE_NAME);
        return (validateFile(fullSearch) && validateFileExtension(fullSearch))
                ? fullSearch
                : new File("");
    }


    // implementing caching that the results of the previous searches are kept in cache that way we do not
    // constantly compute when searching files
//    @Cacheable(lifetime = 5, unit = TimeUnit.SECONDS)
    private File findUsingParentDirAndFileName(File file, String csvdatafile){
        List<String> paths= Stream.of(file.getAbsolutePath().split("/"))
                .filter(string -> !string.isEmpty()).toList();

        List<String> getFileName = Stream.of(csvdatafile.split("/"))
                .filter(string -> !string.isEmpty()).toList();

        String parentDir = paths.get(0);
        String potentialFileName = paths.get(paths.size()-1); // this could be the name of the file or just another directory
        String fileName = getFileName.get(getFileName.size()-1); // this is the name of file that we want to load

        // we can start of trying to see if the file or directory we being given exists from the uses home
        File parentFolder = getFileUsingDepthSearch(USER_HOME,parentDir);

        // once the parent folder gets validated we can now check if the file exists in the parent folder
        if ( validateMainDir(parentFolder)){
            File csvFile = getFileUsingDepthSearch(parentFolder.getAbsolutePath(),fileName);
            if (validateFile(csvFile)) return csvFile;

            // what if the parent file does exist and the user combined the data dir along with the file name
            // then we want to combine the parentDir with the potential child
            File potentialCSV = getFileUsingDepthSearch(parentFolder.getAbsolutePath(),potentialFileName);
            if (validateFile(potentialCSV)) return potentialCSV;
        }

        return new File("");
    };

    private File forceLoadFromBackup(){
        File currentDataDir = new File(DATA_DIR);
        String cvsFile = getPropertyFromBackup("data.file.backup",currentDataDir,APP_DIR);
        return getFileUsingDepthSearch(currentDataDir.getAbsolutePath(),cvsFile);
    }

    private boolean validateFileExtension(File file){
        List<String> ext = List.of("txt","csv");
        String fileExt = file.getName().split("\\.")[1];
        return ext.contains(fileExt);
    }

    private File retriveFile(File file, String fileToRetrieve){
        String dir;
        File file1 = new File("");
        if(validateMainDir(file)){
            dir = file.getAbsolutePath();
            file1 = new File(dir,fileToRetrieve);
        }

        return (validateFile(file1)) ? file1 : new File("");
    }
    private File retriveFile(String fileToRetrieve){
        File file = new File(fileToRetrieve);
        return (validateFile(file)) ? file : new File("");
    }

    private Boolean validateMainDir(File file){
        return (file.exists() && file.isDirectory() && !file.isHidden());
    }

    private Boolean validateFile(File file){
        return (file.exists() && file.isFile());
    }



//    @Cacheable(lifetime = 5, unit = TimeUnit.SECONDS)
    private File getFileUsingDepthSearch(String startDir, String fileToFind){
        File folder = new File(startDir);

        // getting the files in the folders
        File[] files = folder.listFiles();

        // just realized that the file we're looking for copulae the parent of teh folder were in
        // so new approach
        File fileToFindIsParentFile = fileToFindIsParent(folder, fileToFind);
        if (validateMainDir(fileToFindIsParentFile)) return fileToFindIsParentFile;
        if (files == null || files.length == 0) return new File("");

        List<File> justFiles = Arrays.stream(files)
                .filter(File::isFile) // checking whether the file is a file
                .toList();// creating a list out of the files found

        List<File> justDirectories = Arrays.stream(files)
                .filter(file -> file.isDirectory()
                        && !file.isHidden() // the dir should not be hidden
                        && !file.getName().equals("target")) // the file is not target folder that java creates
                .toList(); // collecting only the directories

        if (!justFiles.isEmpty()){
            File fileFound =  justFiles.stream().filter(file -> file.getName().
                    equals(fileToFind)).findFirst().orElse(null); // retrieving the file if we have found it

            if (fileFound != null) return fileFound;
        }

        if (justDirectories.isEmpty()) return new File("");

        // what if the directory that we want is the one that we are looking
        File tmpFile = new File(startDir,fileToFind);
        if(tmpFile.exists()) return tmpFile;

        for (File nextDir : justDirectories){
            File file = getFileUsingDepthSearch(nextDir.getAbsolutePath(),fileToFind);
            if (file.getName().contains(fileToFind)) return file;

        }


        return new File("");
    }

    private File fileToFindIsParent(File folder, String fileToFind){
        List<String> pathName = Arrays.stream(folder.getAbsolutePath()
                .split("/")).filter(string -> !string.isEmpty()).toList();

        boolean fileNameFound = pathName.contains(fileToFind);
        if (!fileNameFound) return new File("");

        StringBuilder pathBuilder = new StringBuilder("/"); // starts with the '/' to show that it is a dir
        for (String name : pathName){
            pathBuilder.append(name);
            if (name.equals(fileToFind)) return new File(pathBuilder.toString()); // returning our new file
        }

        return new File("");
    }

    private boolean loadBackupFolder(String dir){
        DATA_DIR = getProperty("backup.data.file",dir, APP_DIR);
        return (!DATA_DIR.isEmpty());
    }

    private File loadFromBackup(){
        if (loadBackupFolder("backup/places.properties")){
            String fileName = getCsvdatafilePath("backup/places.properties",APP_DIR);
            File csvFile = getFileUsingDepthSearch(DATA_DIR,fileName);
            if(validateFile(csvFile)) return csvFile;
        }

        return new File("");
    }

    private String getCsvdatafilePath(String dir, String folder){
        List<String> keys = List.of("data.file","data.file.backup","backup.config.file");
        String path = "";

        for (String key : keys){
            path = getProperty(key,dir, folder);
            if (!path.isEmpty()) return path;
        }

        return "";
    }

}
