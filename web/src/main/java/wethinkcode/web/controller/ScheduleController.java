package wethinkcode.web.controller;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.http.Handler;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import wethinkcode.loadshed.alert.controllerMonitor;
import wethinkcode.loadshed.common.transfer.ScheduleDO;
import wethinkcode.schedule.ScheduleService;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ScheduleController  extends controllerMonitor {
    private static final String LOCALHOST = "http://localhost:";
    private static final int PORT = ScheduleService.DEFAULT_PORT;


    public static void initJsonMapper(){
        // a custom json mapper for our schedule object, this way we can pass our scheduleDO class
        // to our Unirest request and get an object from it
        final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.registerModule( new JavaTimeModule() );
        mapper.disable( SerializationFeature.WRITE_DATES_AS_TIMESTAMPS ); // ensures that all our dates are made from time stamps
        Unirest.config().setObjectMapper( new kong.unirest.jackson.JacksonObjectMapper( mapper ) ); // setting our custom mapper
    }

    public static final Handler getScheduleHandler = ctx-> {
        try {
            initJsonMapper(); // initializing our custom mapper
            String api;
            // learnt something new, keep your submission last when using selections as medium of submitting  your form
            // Or it will not capture all the information on the form
            String province = ctx.queryParam("province");
            String town = ctx.queryParam("town");


            api = LOCALHOST + PORT + "/%s/%s".formatted(province, town);
            HttpResponse<ScheduleDO> response = Unirest.get(api).asObject(ScheduleDO.class);
            ScheduleDO scheduleDO = response.getBody();

            List<Date> dates = new ArrayList<>();

            for (int i = 0; i < scheduleDO.numberOfDays(); i++) {
                // adding the dates of the loadshedding days
                dates.add(Date.from(scheduleDO.getStartDate()
                        .plusDays(i)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()));
            }


            List provinces = (ProvinceController.provinces.isEmpty()) ? ProvinceController.getProvinces() : ProvinceController.provinces;

            Map<String, Object> viewModel = Map.of(
                    "selectedProvince", province,
                    "selectedTown", town,
                    "towns", ProvinceController.getTowns(province),
                    "provinces", provinces,
                    "schedule", scheduleDO,
                    "scheduleDates", dates);

            ctx.render("schedules.html", viewModel);
        } catch (Exception exception) {
            sendAlert(ScheduleController.class,"severe",exception,"scheduleService is offline. Needs attention");

        }
    };




}
