package wethinkcode.stage.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import wethinkcode.loadshed.common.modelview.ModelViewFormatter;
import wethinkcode.loadshed.common.transfer.StageDO;
import wethinkcode.stage.mq.sender.StageServiceTopicSender;

import java.util.LinkedHashMap;
import java.util.Map;

//import static wethinkcode.stage.StageService.stageDO;


public class StageServiceRouterController extends ModelViewFormatter {
    private static final StageServiceTopicSender topicSender = new StageServiceTopicSender();

    public static final Handler guide = ctx -> {
        setCTXHeader(ctx);
        JsonNode modelNode = convertModelToNode(guideJSON());
        ctx.json(modelNode);
    };

    public static final Handler getStage = ctx -> {
        StageDO stageDO = ctx.appAttribute("stage");
        setCTXHeader(ctx);
        JsonNode modelNode = convertModelToNode(stageDO);
        ctx.json(modelNode); // returning the model
    };

    public static final Handler setStage = ctx -> {
        StageDO stage = ctx.bodyAsClass(StageDO.class);
        StageDO stageDO = ctx.appAttribute("stage");

        if (stageIsValid(stage)) {
            ctx.status(HttpStatus.OK);
            stageDO.setStage(stage.getStage());
            String[] messagesToSend = new String[]{stageDO.toString()};
            topicSender.execute(messagesToSend);
        }else{
            ctx.status(HttpStatus.BAD_REQUEST);
        }

        JsonNode modelNode = convertModelToNode(stageDO.toString());
        ctx.json(modelNode);
    };

    public static void configureExecutionHandler(Javalin server){
        server.exception(Exception.class, (e, ctx) -> {
            System.out.println("Ran into an error: " + e.getMessage());
            System.out.println("Class: " + e.getClass());
            e.printStackTrace();
        });
    }

    public static void configureEndpointNotFoundError(Javalin server) {
        server.error(HttpStatus.NOT_FOUND, ctx -> {
            ctx.status(HttpStatus.BAD_REQUEST);
            LinkedHashMap<String, String> guide = new LinkedHashMap<>(guideJSON());
            guide.put("message","endpoint error");
            JsonNode modelNode = convertModelToNode(guide);
            ctx.json(modelNode); // returning the model to the user so they can know how to use the api
        });
    }

    private static void setCTXHeader(Context ctx){
        ctx.status(HttpStatus.OK);
        ctx.contentType(ContentType.APPLICATION_JSON);
    }

    private static Map<String,String> guideJSON(){
        return Map.of("name","Stage-Service"
                ,"author","Johnny-Ilanga"
                ,"version","1.0"
                ,"endpoint","{type: {GET:{/, /stage}, POST:{/stage/<stage> }}");
    }

    private static boolean stageIsValid(StageDO stageDO){
        return stageDO.getStage() > -1 && stageDO.getStage() <= 8;
    }

}
