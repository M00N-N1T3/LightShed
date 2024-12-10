package wethinkcode.stage.router;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import wethinkcode.common.transfer.StageDO;

import java.util.LinkedHashMap;
import java.util.Map;

public class Router {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static Javalin server;
    private static StageDO stageDO;

    public static Javalin getRoutes(Javalin server, StageDO stageDO){
        Router.stageDO = stageDO;
        Router.server = server;

        endpointNOT_FOUND_ERROR();
        setDefaultRoute();
        setStageGETRoute();
        setStagePOSTRoute();
        return Router.server;
    }


    private static void setDefaultRoute(){
        server.get("/", ctx ->{
            setCTXHeader(ctx);
            JsonNode modelNode = modelNode(guideJSON());
            ctx.json(modelNode);
        });
    }

    private static void setStageGETRoute(){
        server.get("/stage", ctx -> {
            setCTXHeader(ctx);

            JsonNode modelNode = modelNode(stringifyObject(stageDO));
            ctx.json(modelNode); // returning the model
        });
    }

    private static void setStagePOSTRoute(){
        server.post("/stage", ctx ->{
            StageDO postedStageDO = ctx.bodyAsClass(StageDO.class);

            if (validateStage(postedStageDO)) {
                ctx.status(HttpStatus.OK);
                stageDO = postedStageDO; // changing teh current stage
            }else{
                ctx.status(HttpStatus.BAD_REQUEST);
            }

            JsonNode modelNode = modelNode(stringifyObject(stageDO));
            ctx.json(modelNode);
        });
    }

    private static void endpointNOT_FOUND_ERROR() {
        server.error(HttpStatus.NOT_FOUND, ctx -> {
            ctx.status(HttpStatus.BAD_REQUEST);
            LinkedHashMap<String, String> guide = new LinkedHashMap<>(guideJSON());
            guide.put("message","endpoint error");
            JsonNode modelNode = modelNode(guide);
            ctx.json(modelNode); // returning the model to the user so they can know how to use the api
        });
    }

    private static boolean validateStage(StageDO stageDO){
        return stageDO.getStage() > -1;
    }

    private static Map<String,String> guideJSON(){
        return Map.of("name","Stage-Service"
                ,"author","Johnny-Ilanga"
                ,"version","1.0"
                ,"endpoint","{type: {GET:{/, /stage}, POST:{/stage/<stage> }}");
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static String stringifyObject(Object object){
        try{
            return mapper.writeValueAsString(object);
        }catch (JsonProcessingException e){
            // do nothing
            return null;
        }
    }
    private static String jsonifyModel(Object data){

        try{
            return mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            //
        }
        return "";
    }

    private static JsonNode generateJSONNode(String jsonString){
        try{
            return mapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            // do nothing
        }
        return null;
    }

    private static JsonNode modelNode(Object model){
        String jsonString = jsonifyModel(model);
        return generateJSONNode(jsonString);
    }


    private static JsonNode modelNode(String modelAsString){
        return generateJSONNode(modelAsString);
    }


    private static void setCTXHeader(Context ctx){
        ctx.status(HttpStatus.OK);
        ctx.contentType(ContentType.APPLICATION_JSON);
    }


}
