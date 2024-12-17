package wethinkcode.stage.router;

import wethinkcode.loadshed.common.modelview.ModelViewFormatter;
import static io.javalin.apibuilder.ApiBuilder.*;
import wethinkcode.stage.StageService;
import wethinkcode.stage.controller.StageServiceRouterController;

public class Router extends ModelViewFormatter {

    private static final String GUIDE = "/";
    private static final String STAGE = "/stage";

    public static void configureRoutes(StageService stageService) {
        stageService.routes(() -> {
            get(GUIDE, StageServiceRouterController.guide);
            get(STAGE, StageServiceRouterController.getStage);
            post(STAGE, StageServiceRouterController.setStage);
        });

        // MSC
        StageServiceRouterController.configureEndpointNotFoundError(stageService.getStageServer());
        StageServiceRouterController.configureExecutionHandler(stageService.getStageServer());
    }
}