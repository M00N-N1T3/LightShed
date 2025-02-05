package wethinkcode.web.controller;

import io.javalin.http.Handler;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import wethinkcode.loadshed.alert.controllerMonitor;
import wethinkcode.places.PlaceNameService;


import java.util.*;

public class ProvinceController extends controllerMonitor {
    private static final String localhost = "http://localhost:";
    private static final int PLACE_NAME_API_PORT = PlaceNameService.DEFAULT_SERVICE_PORT;
    public static List provinces = new ArrayList<>();
    public static ArrayList<?> getProvinces(){
        try{
            String api = localhost + PLACE_NAME_API_PORT + "/provinces";
            HttpResponse<ArrayList> response =  Unirest.get(api).asObject(ArrayList.class);
            return response.getBody();
        }catch (Exception exception){
            sendAlert(ProvinceController.class,"severe",exception,"placeName service is offline");
            return new ArrayList<>();
        }
    }

    public static ArrayList<?> getTowns(String province){
        try {
            String api = localhost + PLACE_NAME_API_PORT + "/towns/%s".formatted(province);
            HttpResponse<JsonNode> response = Unirest.get(api).asJson();
            return (ArrayList<?>) response.getBody().getObject().getJSONArray("data").toList();
        } catch (Exception exception) {
            sendAlert(ProvinceController.class,"moderate",exception,"failed to get towns for province: " + province);
            return new ArrayList<>();
        }
    }

    @SuppressWarnings("unchecked")
    public static final Handler loadProvinces = ctx ->{
        provinces = (provinces.isEmpty()) ? getProvinces() : provinces;
        Collections.sort(provinces);
        Map<String, List> viewModel = Map.of("provinces", provinces);
        if (provinces.isEmpty()){
            ctx.render("offlineservices.html");
        }else{
            ctx.render("index.html",viewModel);
        }
    };

    public static final Handler loadTowns = ctx ->{
        try{
            String selectedProvince = ctx.queryParam("province");

            List towns = getTowns(selectedProvince);
            provinces = (provinces.isEmpty()) ? getProvinces() : provinces;

            // sorting the towns alphabetically
            towns = (towns == null) ? new ArrayList<>() : towns;
            Collections.sort(towns);

            Map<String, Object> viewModel = Map.of(
                    "towns", towns,
                    "selectedProvince",selectedProvince,
                    "provinces", provinces);

           ctx.render("towns.html",viewModel);
        } catch (Exception exception) {
            sendAlert(ProvinceController.class,"moderate",exception,"failed to load towns");
            ctx.render("offlineservices.html");
        }
    };


}
