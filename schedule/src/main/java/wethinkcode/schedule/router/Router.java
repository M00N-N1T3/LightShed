package wethinkcode.schedule.router;

import static io.javalin.apibuilder.ApiBuilder.*;

import io.javalin.Javalin;
import wethinkcode.loadshed.common.modelview.ModelViewFormatter;
import wethinkcode.schedule.ScheduleService;
import wethinkcode.schedule.controller.ScheduleServiceRouterController;

public class Router extends ModelViewFormatter {
    public static ScheduleService scheduleService;
    public static int loadSheddingStage = 0;

    // ROUTES
    private static final String HOME = "/";
    private static final String GET_SCHEDULE = "/{province}/{place}/{loadsheddingstage}";

    public static void configureRoutes(ScheduleService scheduleService){
        Router.scheduleService = scheduleService;
        scheduleService.routes(() ->{
            get(HOME, ScheduleServiceRouterController.guide);
            get(GET_SCHEDULE, ScheduleServiceRouterController.getSchedule);
        });
    }

    public static void configureEndpointNotFoundError(Javalin server){
        ScheduleServiceRouterController.configureEndpointNotFoundError(server);
    }
}

