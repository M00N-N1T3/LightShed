package wethinkcode.loadshed.common.configurator;

import java.io.*;
import java.util.Map;
import java.util.Properties;


public class PropertiesFile {
    private static final Properties properties = new Properties();
    public static String getProperty(String key){

        try{
            InputStream propertiesFile = new FileInputStream("resources/application.properties");
            properties.load(propertiesFile); // loading the properties file
            // returning the property that we require
            return properties.getProperty(key);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getPropertyFromBackup(String key, File cwd, String APP_NAME){

        try{
            File backup = new File("backup",APP_NAME + ".properties");
            File files = new File(cwd.getPath(),backup.getPath());
            InputStream propertiesFile = new FileInputStream(files);
            properties.load(propertiesFile); // loading the properties file
            // returning the property that we require
            return properties.getProperty(key);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public static String getProperty(String mainDir,String customPropertiesFile, String key, String dir, String folderName){
        File resources = new File(mainDir,customPropertiesFile);

        try{
            File executionDir = combinedFile(new File(dir), folderName);
            File combinedPath = new File(executionDir.getAbsolutePath(), resources.getPath());
            InputStream propertiesFile = new FileInputStream(combinedPath.getAbsoluteFile());
            properties.load(propertiesFile); // loading the properties file
            // returning the property that we require
            return properties.getProperty(key);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean writeProperty(String key, String data){
        try{
            OutputStream propertiesFile = new FileOutputStream("resources/application.properties");
            properties.setProperty(key,data);
            properties.store(propertiesFile,null);
            return true;
        } catch (IOException e) {
            //
        }
        return false;
    }

    public static  boolean writeProperties(Map<String, String> properties){
        try{
            OutputStream propertiesFile = new FileOutputStream("resources/application.properties");
            PropertiesFile.properties.putAll(properties);
            PropertiesFile.properties.store(propertiesFile,null);
            return true;
        } catch (IOException e) {
            //
        }
        return false;
    }


    // extras
    public static String getProperty(String key, String propertyFileDir, String propertyFileName){


        try{
            File file= new File(propertyFileDir,propertyFileName);

            InputStream propertiesFile = new FileInputStream(file.getPath());
            properties.load(propertiesFile); // loading the properties file
            // returning the property that we require
            return properties.getProperty(key);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getProperty(String key, File propertyFile){

        try{
            InputStream propertiesFile = new FileInputStream(propertyFile.getAbsoluteFile());
            properties.load(propertiesFile); // loading the properties file
            // returning the property that we require
            return properties.getProperty(key);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static String getPropertyFromCustomFile(String key, String absFile){
        try{
            File resources = new File(absFile);
            InputStream propertiesFile = new FileInputStream(resources.getAbsoluteFile());
            properties.load(propertiesFile); // loading the properties file
            // returning the property that we require
            return properties.getProperty(key);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static File combinedFile(File exeDir, String folderName){
        return checkFolderCombination(exeDir,folderName)
                ? new File(exeDir.getAbsoluteFile(), folderName)
                : exeDir;
    }

    private static boolean checkFolderCombination(File exeDir, String folderName){
        return !exeDir.getName().equals(folderName);
    }
    public static boolean writeProperty(String key, String data, String dir, String folderName){

        File resources = new File("resources","application.properties");

        try{
            File executionDir = new File(dir, folderName);
            File combinedPath = new File(executionDir.getAbsolutePath(), resources.getPath());
            OutputStream propertiesFile = new FileOutputStream(combinedPath);
            properties.setProperty(key,data);
            properties.store(propertiesFile,null);
            return true;
        } catch (IOException e) {
            //
        }
        return false;
    }

    public static  boolean writeProperties(Map<String, String> properties, String dir, String folderName){
        File resources = new File("resources","application.properties");

        try{
            File executionDir = new File(dir, folderName);
            File combinedPath = new File(executionDir.getAbsolutePath(), resources.getPath());
            OutputStream propertiesFile = new FileOutputStream(combinedPath);
            PropertiesFile.properties.putAll(properties);
            PropertiesFile.properties.store(propertiesFile,null);
            return true;
        } catch (IOException e) {
            //
        }
        return false;
    }

    public static  boolean writeProperties(Map<String, String> propertiesToWrite, String propertyFileAbsolutePath ){

        File file = new File(propertyFileAbsolutePath);
        Object tmp = null;

        InputStream propertiesFileReader = null;
        OutputStream propertiesFileWriter = null;

        // storing a copy
        try {
            propertiesFileReader = new FileInputStream(file);
            properties.load(propertiesFileReader);
            tmp = properties.clone();
        }catch (Exception e) {
            //
        }

        try {
            propertiesFileWriter = new FileOutputStream(file);
            properties.putAll(propertiesToWrite);
            properties.store(propertiesFileWriter,null);
            propertiesFileWriter.close();
            return true;
        } catch (Exception e) {

            try{
                properties.putAll((Map<?, ?>) tmp);
                properties.store(propertiesFileWriter,null);
                assert propertiesFileWriter != null;
                propertiesFileWriter.close();
                System.out.println("Unable to save session properties");

            }catch (IOException failedToWrite){
                //
            }

        }

        return false;
    }

    public static boolean writeProperty(String key, String data, File propertyFile){

        try{
            OutputStream propertiesFile = new FileOutputStream(propertyFile.getAbsoluteFile());
            properties.setProperty(key,data); // loading the properties file
            properties.store(propertiesFile,null);
            return true;
        } catch (IOException e) {
            System.out.println("Unable to save property");
            return false;
        }
    }

}
