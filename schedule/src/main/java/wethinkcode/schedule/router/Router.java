package wethinkcode.schedule.router;

import static io.javalin.apibuilder.ApiBuilder.*;
import wethinkcode.schedule.ScheduleService;
import wethinkcode.schedule.controller.ScheduleServiceRouterController;

public class Router{
    // ROUTES
    private static final String HOME = "/";
    private static final String SCHEDULE_WITH_PROVIDED_STAGE = "/{province}/{place}/{loadsheddingstage}";
    private static final String SCHEDULE_WITHOUT_PROVIDED_STAGE = "/{province}/{place}";


    public static void configureRoutes(ScheduleService scheduleService){
            scheduleService.routes(() ->{
            get(HOME,                               ScheduleServiceRouterController.guide);
            get(SCHEDULE_WITHOUT_PROVIDED_STAGE,    ScheduleServiceRouterController.getSchedule);
            get(SCHEDULE_WITH_PROVIDED_STAGE,       ScheduleServiceRouterController.getScheduleWithProvidedStage);
        });

        ScheduleServiceRouterController
                .configureExecutionHandler(scheduleService.getScheduleServer());
    }

}

