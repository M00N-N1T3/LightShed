package wethinkcode.schedule.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import wethinkcode.loadshed.common.transfer.ScheduleDO;
import wethinkcode.loadshed.common.modelview.ModelViewFormatter;
import static wethinkcode.loadshed.common.Helpers.isDigit;
import static wethinkcode.schedule.router.Router.loadSheddingStage;
import static wethinkcode.schedule.router.Router.scheduleService;

public class ScheduleServiceRouterController extends ModelViewFormatter {

    public static final Handler guide = ctx -> {
        JsonNode modelNode = modelNode(guideJSON());
        ctx.json(modelNode);
    };


    public static final Handler getSchedule = ctx -> {
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
    };

    public static void configureEndpointNotFoundError(Javalin server){
        server.error(HttpStatus.NOT_FOUND, ctx -> {
            ctx.status(HttpStatus.BAD_REQUEST);
            LinkedHashMap<String, String> guide = new LinkedHashMap<>(guideJSON());
            guide.put("message","endpoint error");
            JsonNode modelNode = modelNode(guide);
            ctx.json(modelNode); // returning the model to the user so they can know how to use the api
        });
    }

    private static void stageIsValid(Context ctx, String province, String place){

        Optional<ScheduleDO> schedulesDO = scheduleService.getSchedule(
                province
                ,place
                ,loadSheddingStage);

        HttpStatus status = (schedulesDO.isPresent()
                && schedulesDO.get().numberOfDays() > 0)
                ? HttpStatus.OK
                : HttpStatus.NOT_FOUND;

        ctx.status(status);
        schedulesDO.ifPresent(ctx::json);

    }

    private static boolean validateStage(int stage){
        return stage >= 0 && stage <=8;
    }

    private static void badRequest(Context ctx, String message){
        ctx.status(HttpStatus.BAD_REQUEST);
        JsonNode modelNode = modelNode(Map.of("message",message));
        ctx.json(modelNode);
    };



    private static Map<String,String> guideJSON(){
        return Map.of("name","Schedule-Service"
                ,"author","Johnny-Ilanga"
                ,"version","1.0"
                ,"endpoint","{type: {GET:{/,/{province/{place}/{loadsheddingstage}}");
    }
}
