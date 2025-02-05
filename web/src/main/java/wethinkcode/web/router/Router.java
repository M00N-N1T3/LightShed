package wethinkcode.web.router;

import wethinkcode.web.WebService;
import wethinkcode.web.controller.ProvinceController;
import wethinkcode.web.controller.ScheduleController;
import static io.javalin.apibuilder.ApiBuilder.*;

public class Router {
    // ROUTES
    private static final String HOME = "/";
    private static final String SELECT_PROVINCE = "/provinces.action";
    private static final String SCHEDULE = "/schedules.action";

    public static void configure(WebService server){
        server.routes(() -> {
            get(HOME,               ProvinceController.loadProvinces);
            get(SELECT_PROVINCE,        ProvinceController.loadTowns);
            get(SCHEDULE,       ScheduleController.getScheduleHandler);
        });

    }
}