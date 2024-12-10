package wethinkcode.web.controllers;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import wethinkcode.common.transfer.StageDO;
import wethinkcode.stage.StageService;


public class StageController {
    private static final String LOCALHOST = "http://localhost:";
    private static final int PORT = StageService.DEFAULT_PORT;


    public static final Handler getStage = ctx -> {
        String api = LOCALHOST + PORT + "/stages";
        HttpResponse<StageDO> response = Unirest.get(api).asObject(StageDO.class);
        StageDO newStageDO = response.getBody();
        newStageDO = (newStageDO != null) ? newStageDO : new StageDO();
        ctx.json(newStageDO);
    };


    public static final Handler postStage = ctx -> {
        String stage = ctx.pathParam("stage");
        String api = LOCALHOST + PORT + "/stages";
        HttpResponse<JsonNode> post = Unirest.post(api)
                .header("Content-Type", "application/json")
                .body(new StageDO(stage)).asJson();
    };

    public static StageDO getStage(Context ctx) {
        // calling the stages api
        String api = LOCALHOST +PORT + "/stage";
        HttpResponse<JsonNode> jsonNode = Unirest.get(api).asJson();
        String  stage =  jsonNode.getBody().getObject().getString("stage");
        return new StageDO(stage);
    }

}