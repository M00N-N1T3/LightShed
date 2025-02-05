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
import wethinkcode.loadshed.common.transfer.StageDO;
import wethinkcode.schedule.ScheduleService;

import static wethinkcode.loadshed.common.Helpers.isDigit;


public class ScheduleServiceRouterController extends ModelViewFormatter {

    public static final Handler guide = ctx -> {
        JsonNode modelNode = convertModelToNode(guideJSON());
        ctx.json(modelNode);
    };


    public static final Handler getScheduleWithProvidedStage = ctx -> {
        String province = ctx.pathParam("province");
        String place = ctx.pathParam("place");
        String loadsheddingstage = ctx.pathParam("loadsheddingstage");

        if (isDigit(loadsheddingstage)){
            int stage = Integer.parseInt(loadsheddingstage);
            if (stageIsValid(stage)){
                generateSchedule(ctx,province,place,stage);
            }else {
                badRequest(ctx,"parameter loadsheddingstage :1 must be an integer between 0 and 8");
            }

        }else{
            badRequest(ctx,"parameter loadsheddingstage: must be an integer between 0 and 8");
        }
    };

    public static final Handler getSchedule = ctx -> {
        String province = ctx.pathParam("province");
        String place = ctx.pathParam("place");
        StageDO stageDO = ctx.appAttribute("stage");
        generateSchedule(ctx,province,place,stageDO.getStage());
    };

    public static void configureEndpointNotFoundError(Javalin server){
        server.error(HttpStatus.NOT_FOUND, ctx -> {
            LinkedHashMap<String, String> guide = new LinkedHashMap<>(guideJSON());
            guide.put("message","endpoint error");
            JsonNode modelNode = convertModelToNode(guide);
            ctx.json(modelNode); // returning the model to the user so they can know how to use the api
        });
    }

    public static void configureExecutionHandler(Javalin server){
        server.exception(Exception.class, (e, ctx) -> {
            System.out.println("Ran into an error: " + e.getMessage());
            System.out.println("Class: " + e.getClass());
            e.printStackTrace();
        });
    }

    private static void generateSchedule(Context ctx, String province, String place,int stage){
        ScheduleService scheduleService = ctx.appAttribute("schedule");
        Optional<ScheduleDO> scheduleDO = getSchedules(scheduleService,province,place,stage);
        scheduleDO.ifPresent(schedules -> modelContext(ctx, schedules));
    }

    private static void modelContext(Context ctx,ScheduleDO schedule){
        Optional<ScheduleDO> scheduleDO = Optional.of(schedule);
        HttpStatus status = getHttpStatus(scheduleDO);
        ctx.status(status);
        scheduleDO.ifPresent(ctx::json);
    }
    private static HttpStatus getHttpStatus(Optional<ScheduleDO> scheduleDO){
        return (scheduleDO.isPresent()
                && scheduleDO.get().numberOfDays() > 0)
                ? HttpStatus.OK
                : HttpStatus.NOT_FOUND;
    }

    private static Optional<ScheduleDO> getSchedules(ScheduleService scheduleService, String province, String place, int stage){
        return  scheduleService.getSchedule(
                province
                ,place
                ,stage);
    }

    private static boolean stageIsValid(int stage){
        return stage >= 0 && stage <=8;
    }

    private static void badRequest(Context ctx, String message){
        ctx.status(HttpStatus.BAD_REQUEST);
        JsonNode modelNode = convertModelToNode(Map.of("message",message));
        ctx.json(modelNode);
    };



    private static Map<String,String> guideJSON(){
        return Map.of("name","Schedule-Service"
                ,"author","Johnny-Ilanga"
                ,"version","1.0"
                ,"endpoint","{type: {GET:{/,/{province/{place}/{loadsheddingstage}}");
    }
}

