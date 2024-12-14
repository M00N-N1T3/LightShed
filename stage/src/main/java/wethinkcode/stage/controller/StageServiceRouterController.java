package wethinkcode.stage.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import wethinkcode.loadshed.common.modelview.ModelViewFormatter;
import wethinkcode.loadshed.common.transfer.StageDO;

import java.util.LinkedHashMap;
import java.util.Map;

import static wethinkcode.stage.StageService.stageDO;


public class StageServiceRouterController extends ModelViewFormatter {

    public static final Handler guide = ctx -> {
        setCTXHeader(ctx);
        JsonNode modelNode = modelNode(guideJSON());
        ctx.json(modelNode);
    };

    public static final Handler getStage = ctx -> {
        setCTXHeader(ctx);
        JsonNode modelNode = modelNode(stringifyObject(stageDO));
        ctx.json(modelNode); // returning the model
    };

    public static final Handler setStage = ctx -> {
        StageDO postedStageDO = ctx.bodyAsClass(StageDO.class);

        if (validateStage(postedStageDO)) {
            ctx.status(HttpStatus.OK);
            stageDO = postedStageDO; // changing teh current stage
        }else{
            ctx.status(HttpStatus.BAD_REQUEST);
        }

        JsonNode modelNode = modelNode(stringifyObject(stageDO));
        ctx.json(modelNode);
    };

    public static void configureEndpointNotFoundError(Javalin server) {
        server.error(HttpStatus.NOT_FOUND, ctx -> {
            ctx.status(HttpStatus.BAD_REQUEST);
            LinkedHashMap<String, String> guide = new LinkedHashMap<>(guideJSON());
            guide.put("message","endpoint error");
            JsonNode modelNode = modelNode(guide);
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

    private static boolean validateStage(StageDO stageDO){
        return stageDO.getStage() > -1;
    }

}
