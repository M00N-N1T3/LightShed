package wethinkcode.loadshed.common.modelview;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

public class ModelViewFormatter {
    private static final ObjectMapper mapper = new ObjectMapper();
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected static <T> String stringifyObject(T object){
        try{
            return mapper.writeValueAsString(object);
        }catch (JsonProcessingException e){
            // do nothing
            return null;
        }
    }
    protected static String convertModelToJSON(Object data){
        try{
            return mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            //
        }
        return "";
    }

    protected static JsonNode generateJSONNode(String jsonString){
        try{
            return mapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            // do nothing
        }
        return null;
    }

    protected static JsonNode modelNode(Object model){
        String jsonString = convertModelToJSON(model);
        return generateJSONNode(jsonString);
    }

    protected static JsonNode modelNode(String modelAsString){
        return generateJSONNode(modelAsString);
    }

    protected static HashMap<String,Object> JSONModel(String api_version, Map<String,Object> endPointAndType, String status){
        Map<String, Object> hashThisMap =  Map.of("api_version",api_version
                ,"result",status
                ,"endpoint",endPointAndType);

        // found out only now that a Map is immutable while a hashMap is mutable thus the conversion
        return new HashMap<>(hashThisMap); // storing our model in a hashMap

    }
}
