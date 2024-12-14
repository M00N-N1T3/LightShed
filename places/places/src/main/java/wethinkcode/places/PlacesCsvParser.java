package wethinkcode.places;

import java.io.*;
import java.util.*;

import com.google.common.annotations.VisibleForTesting;
import com.opencsv.CSVParser;
import com.opencsv.exceptions.CsvException;
import wethinkcode.places.db.memory.PlacesDb;
import wethinkcode.places.model.Places;
import com.opencsv.CSVReader;
import wethinkcode.places.model.Town;

import static wethinkcode.loadshed.commonHelpers.capitalizeString;


/**
 * PlacesCsvParser : I parse a CSV file with each line containing the fields (in order):
 * <code>Name, Feature_Description, pklid, Latitude, Longitude, Date, MapInfo, Province,
 * fklFeatureSubTypeID, Previous_Name, fklMagisterialDistrictID, ProvinceID, fklLanguageID,
 * fklDisteral, Local Municipality, Sound, District Municipality, fklLocalMunic, Comments, Meaning</code>.
 * <p>
 * For the PlaceNameService we're only really interested in the <code>Name</code>,
 * <code>Feature_Description</code> and <code>Province</code> fields.
 * <code>Feature_Description</code> allows us to distinguish towns and urban areas from
 * (e.g.) rivers, mountains, etc. since our PlaceNameService is only concerned with occupied places.
 */
public class PlacesCsvParser
{
    public static final int FEATURE_COLUMN = 1;
    private LinkedHashMap<String, Integer> headers;
    private final List<String> features = List.of("Town", "Urban Area","Group of Huts","Village","Settlement");
    private final ArrayList<Map<String,String>> errors = new ArrayList<>();
    private final List<String> provinces = List.of("Western Cape","Eastern Cape","Northern Cape","KwaZulu-Natal","Free State","Gauteng","Limpopo","North West","Mpumalanga");
    private int maxLine = 0;
    public static int MIN_COLUMNS = 8;

    public  Places parseCsvSource( File csvFile ){
        headers = new LinkedHashMap<>();
        List<String[]> data = readCsv(csvFile);
        List<String[]> CSVData = cleanCSVData(data);
        return parseDataLines(CSVData);

    }
    
    public Places parseCsvSource(LineNumberReader lineNumberReader){
        return parseDataLines(lineNumberReader);
    }
    
    public String[] splitLineIntoValues(String dataLine){
        return dataLine.split(",");
    }

    @VisibleForTesting
    Places parseDataLines( final LineNumberReader in ){

        String data;
        Set <Town> towns = new LinkedHashSet<>();
        try{
            // as long as we have data being fed from the stream we shall continue creating town objects
            while ((data = in.readLine())!=null){
                CSVParser parser = new CSVParser();
                String[] csvLine = parser.parseLine(data);

                if (maxLine == 0){
                    maxLine = (in.getLineNumber() == 0) ? csvLine.length : maxLine;
                };

                if (validateLine(data) && validateCSVLine(csvLine) ) {
                    towns.add(new Town(csvLine[0] // town name
                            , csvLine[7] // province
                            , csvLine[1])); // description

                }
                else {
                    if (in.getLineNumber() == 0) {
                        errors.add(generateError(ParserErrors.HEADERS,in.getLineNumber(),csvLine.length,maxLine));
                        continue;
                    }

                }

                if (maxLine != 0 && csvLine.length != maxLine){
                    errors.add(generateError(ParserErrors.LINE_ELEMENT_RATIO_ERROR,in.getLineNumber(),csvLine.length,maxLine));
                }

                if(!validateCSVLine(csvLine)){
                    errors.add(generateError(ParserErrors.NOT_A_SETTLEMENT,in.getLineNumber(),csvLine.length,maxLine));
                }
            }

            return new PlacesDb(towns);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    Places parseDataLines(List<String[]> csvData){
        String dataStrings = prepareDataForLineNumberReader(csvData);
        LineNumberReader in = new LineNumberReader( new StringReader(dataStrings) );

        String data;
        Set <Town> towns = new LinkedHashSet<>();
        try{
            // as long as we have data being fed from the stream we shall continue creating town objects
            while ((data = in.readLine())!=null){
                CSVParser parser = new CSVParser();
                String[] csvLine = parser.parseLine(data);

                if (maxLine == 0){
                    maxLine = (in.getLineNumber() == 0) ? csvLine.length : maxLine;
                };

                if( validateCSVLine(csvLine) ) {
                    towns.add(new Town(csvLine[0] // town name
                            , csvLine[7] // province
                            , csvLine[1])); // description

                }
                else {
                    if (in.getLineNumber() == 0) {
                        errors.add(generateError(ParserErrors.HEADERS,in.getLineNumber(),csvLine.length,maxLine));
                        continue;
                    }

                }

                if (maxLine != 0 && csvLine.length != maxLine){
                    errors.add(generateError(ParserErrors.LINE_ELEMENT_RATIO_ERROR,in.getLineNumber(),csvLine.length,maxLine));
                }

                if(!validateCSVLine(csvLine)){
                    errors.add(generateError(ParserErrors.NOT_A_SETTLEMENT,in.getLineNumber(),csvLine.length,maxLine));
                }

            }

            return new PlacesDb(towns);

        } catch (Exception e) {
            System.out.println(towns.size());
            throw new RuntimeException(e);
        }

    }

    private String prepareDataForLineNumberReader(List<String[]> csvData){
        StringBuilder stringCollection = new StringBuilder();

        for (String[] strings: csvData){
            int charCount = 1;
            StringBuilder tmp = new StringBuilder();
            for (String string: strings){
                if (charCount == 20) {
                    tmp.append(string);
                }else {
                    tmp.append(string).append(",");
                }
                charCount ++;
            }
            stringCollection.append(tmp).append("\n");

        }
        return stringCollection.toString();
    }

    public ArrayList<Map<String,String>> getErrors(){
        return this.errors;
    }

    public void setMaxLine(int maxLine){
        this.maxLine = maxLine;
    }

    public int getMaxLine(){
        return maxLine;
    }

    private Map<String, String> generateError(String errorMessage, int lineNumber, int lineLength,int maxLine){


     return Map.of("error",errorMessage,
             "elementPerLine",String.valueOf(maxLine),
             "lineNumber",String.valueOf(lineNumber),
             "lineLength",String.valueOf(lineLength));
    }

    private int getMaxLine(String[] firstLine){
        return firstLine.length;
    }

    public void main(String[] args) throws IOException {
        parseCsvSource(new File("places/resources/PlaceNamesZA2008.csv"));
    }

    private boolean validateLine(String Line){
        return (!Line.contains("Name")
                && !Line.contains("Description"));
    }

    private boolean validateCSVLine(String[] csvLine){
        return csvLine.length > 8
                && features.contains(csvLine[1])
                && provinces.contains(csvLine[7])
                && csvLine[0] != "name";
    }

    @VisibleForTesting
    protected List<String[]> cleanCSVData (List<String[]> data){

        // getting our headers
        List<String> headersInFile = List.of(data.get(0));
        maxLine = headersInFile.size();
        headers = getHeaders(headersInFile,
                List.of("name","province","feature_description"));

        List<String[]> filteredData = filterData(headers,data,"feature_description",features);

        return filteredData;
    };



    @VisibleForTesting
    protected String[] parseCSVLines(String Lines){
        try{

            CSVParser parser = new CSVParser();
            String[] csvLines = parser.parseLineMulti(Lines);
            return csvLines;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @VisibleForTesting
    protected LinkedHashMap<String, Integer> getHeaders(List<String> headers, List<String> desired_fields){
        // todo: correct this with stream neh
        LinkedHashMap<String, Integer> headerIndex = new LinkedHashMap<>();
        for (String header : headers){
            if (desired_fields == null || desired_fields.isEmpty()){
                headerIndex.put(header,headers.indexOf(header));
            }else{
                // filtering the headers and only retrieving the headers we want
                if (desired_fields.contains(header.toLowerCase())){
                    headerIndex.put(header,headers.indexOf(header));
                }
            }
        }

        return headerIndex;
    }

//    @VisibleForTesting
    protected List<String[]> readCsv(File csvFile){
        try{
            String filepath = csvFile.getAbsolutePath();
            FileReader in = new FileReader(filepath);
            CSVReader csv = new CSVReader(in);
            return csv.readAll(); // the file we want to read
        } catch (IOException | CsvException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Generates a
     * @param headers a map containing the column index of the headers.[-header- name : 0 -index-]
     * @param data a list/Array containing all the lines in our csv. Note: Each line is an array of its own
     * @param mapKey the key we wish to filter by. This key is used to access the index of the header in our map
     * @param filters keywords that we wish to filter by
     * @return List containing our filtered data
     */
    @VisibleForTesting
    protected List<String[]> filterData(Map<String, Integer> headers, List<String[]> data, String mapKey, List<String> filters){
        List<String[]> filteredData = new ArrayList<>();


        for (String[] element : data){
            // revealing the data in the list
            List<String> eleToList = Arrays.stream(element).toList();
            String feature_description = eleToList.get(headers.get(capitalizeString(mapKey)));
            if (filters.contains(feature_description)) filteredData.add(element);
        }

        return filteredData;
    };

    public boolean isLineAWantedFeature(String[] values) {
        return features.contains(values[FEATURE_COLUMN]);
    }

}
