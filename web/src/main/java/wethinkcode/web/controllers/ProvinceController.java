package wethinkcode.web.controllers;

import io.javalin.http.Handler;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import wethinkcode.places.PlaceNameService;

import java.util.*;


public class ProvinceController {
    private static final String localhost = "http://localhost:";
    private static final int PLACE_NAME_API_PORT = PlaceNameService.DEFAULT_SERVICE_PORT;
    public static List provinces = new ArrayList<>();
    public static List towns = new ArrayList<>();

    public static ArrayList<?> getProvinces(){
        String api = localhost + PLACE_NAME_API_PORT + "/provinces";
        HttpResponse<ArrayList> response =  Unirest.get(api).asObject(ArrayList.class);
        return response.getBody();
    }

    public static ArrayList<?> getTowns(String province){
        String api = localhost + PLACE_NAME_API_PORT + "/towns/%s".formatted(province);
        HttpResponse<JsonNode> response = Unirest.get(api).asJson();
        return (ArrayList<?>) response.getBody().getObject().getJSONArray("data").toList();
    }

    @SuppressWarnings("unchecked")
    public static final Handler loadProvinces = ctx ->{
        provinces = (provinces.isEmpty()) ? getProvinces() : provinces;
        Collections.sort(provinces);
        Map<String, List> viewModel = Map.of("provinces", provinces);
        ctx.render("index.html",viewModel);
    };

    public static final Handler loadTowns = ctx ->{
        String selectedProvince = ctx.queryParam("province");

        towns = getTowns(selectedProvince);
        provinces = (provinces.isEmpty()) ? getProvinces() : provinces;

        // sorting the towns alphabetically
        towns = (towns == null) ? new ArrayList<>() : towns;
        Collections.sort(towns);

        Map<String, Object> viewModel = Map.of(
                "towns", towns,
                "selectedProvince",selectedProvince,
                "provinces", provinces);

       ctx.render("towns.html",viewModel);
    };
}
