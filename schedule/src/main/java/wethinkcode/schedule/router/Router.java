package wethinkcode.schedule.router;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.util.*;

// jackson
import com.fasterxml.jackson.databind.*;
import wethinkcode.schedule.ScheduleService;
import wethinkcode.common.transfer.ScheduleDO;

public class Router {
    private static ScheduleService scheduleService;
    private static Javalin server;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static int loadSheddingStage = 0;
    private static int maxLoadSheddingStage = 8;

    public static Javalin getRoutes(Javalin server, ScheduleService scheduleService){
        Router.scheduleService = scheduleService;
        Router.server = server;

        // routes
//        endpointNOT_FOUND_ERROR();
        getGuideRoute();
        getScheduleRoute();
        return server;
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

    private static void getScheduleRoute(){
        // {parameter} allows slashes as part of the parameter
        // <parameter> does not allow any slashes as part of the parameter
        server.get("/{province}/{place}/{loadsheddingstage}", ctx ->{
            String province = ctx.pathParam("province");
            String place = ctx.pathParam("place");
            String loadsheddingstage = ctx.pathParam("loadsheddingstage");

            if (isDigit(loadsheddingstage)){

                if (validateStage(Integer.parseInt(loadsheddingstage))){
                    stageIsValid(ctx,province,place);
                }else {
                    badRequest(ctx,"parameter \"loadsheddingstage\", must be an integer between 0 and 8");
                }

            }else{
                badRequest(ctx,"parameter \"loadsheddingstage\", must be an integer between 0 and 8");
            }

        });
    }

    private static void stageIsValid(Context ctx, String province,String place){

        Optional<ScheduleDO> schedulesDO = scheduleService.getSchedule(
                province
                ,place
                ,loadSheddingStage);

        HttpStatus status = (schedulesDO.isPresent()
                && schedulesDO.get().numberOfDays() > 0) ? HttpStatus.OK : HttpStatus.NOT_FOUND;

        // todo: fix error 400, it must give us 404 not 400
        ctx.status(status);
        schedulesDO.ifPresent(ctx::json);

    }
    private static void badRequest(Context ctx, String message){
        ctx.status(HttpStatus.BAD_REQUEST);
        JsonNode modelNode = modelNode(Map.of("message",message));
        ctx.json(modelNode);
    }

    private static void getGuideRoute(){
        server.get("/",ctx ->{
            JsonNode modelNode = modelNode(guideJSON());
            ctx.json(modelNode);
        });
    }

    private static boolean validateStage(int stage){
        return stage >= 0 && stage <=8;
    }

    private static JsonNode generateJSONNode(String jsonString){
        try{
            return mapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            // do nothing
        }
        return null;
    }

    /**
     * @param model can be a JAVA-OBJECT, LinkedHashMap or Map
     * @return JsonNode
     */
    private static JsonNode modelNode(Object model){
        String jsonString = jsonifyModel(model);
        return generateJSONNode(jsonString);
    }


    private static JsonNode modelNode(String modelAsString){
        return generateJSONNode(modelAsString);
    }



    private static Map<String,String> guideJSON(){
        return Map.of("name","Schedule-Service"
                ,"author","Johnny-Ilanga"
                ,"version","1.0"
                ,"endpoint","{type: {GET:{/,/{province/{place}/{loadsheddingstage}}");
    }

    private static LinkedHashMap<String, Object> endpointMap(String endpoint){
        return new LinkedHashMap<>(Map.of("endpoint",endpoint));
    }

    private static String jsonifyModel(Object data){

        try{
            return mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            //
        }
        return "";
    }

    private static boolean isDigit(String string){
        try {
            Integer.parseInt(string);
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
