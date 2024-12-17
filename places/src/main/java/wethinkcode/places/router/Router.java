package wethinkcode.places.router;


import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import wethinkcode.loadshed.common.modelview.ModelViewFormatter;
import wethinkcode.places.model.Places;
import wethinkcode.places.model.Town;
import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.*;

public class Router extends ModelViewFormatter {

    private static Javalin server;
    private static Places places;

    public static Javalin getRoutes(Javalin server, Places places){

        Router.server = server;
        Router.places = places;
        endpointNOT_FOUND_ERROR();
        apiHome();
        setProvincesEndpoint();
        setTownsEndpoint();

        return server;
    }

    private static void apiHome(){
        // guide - note ctx = context
        server.get("/", ctx -> {
            ctx.json(convertObejctToJsonString(guideJSON()));
        });

    }

    private static void setProvincesEndpoint(){
        server.get("/provinces", ctx ->{
            Collection<String> provinceList = places.provinces();

            String status = setStatusForProvinces(provinceList,ctx);
            String data = convertObejctToJsonString(provinceList.toArray());
            ctx.json(data);
        });
    }

    private static void endpointNOT_FOUND_ERROR() {
        server.error(HttpStatus.NOT_FOUND, ctx -> {
            ctx.status(HttpStatus.BAD_REQUEST);
            LinkedHashMap<String, String> guide = new LinkedHashMap<>(guideJSON());
            guide.put("message","endpoint error");
            JsonNode modelNode = convertModelToNode(guide);
            ctx.json(modelNode); // returning the model to the user so they can know how to use the api
        });
    }
    private static void setTownsEndpoint(){
        server.get("/towns/{province-name}", ctx -> {

            String aProvince = ctx.pathParam("province-name");
            Collection<Town> towns = places.townsIn(aProvince);


            String status = setStatus(towns,ctx);
            List<String> townsList = towns.stream().map(Town::getName).toList(); // a List of all the names in the town

            Map<String, Object> endPointInfo = endPointInfoWithArgs("GET","/towns/",List.of(aProvince));
            Map<String, Object> model = JSONModel("1.0", endPointInfo,status);
            model.put("data",townsList);

            // if there is no towns in a particular province
            if (towns.isEmpty()) model.put("message","No towns found in the province of %s".formatted(aProvince));
            if (!validateProvince(aProvince)) model.put("message","%s is not a valid province.".formatted(aProvince));

            ctx.status(HttpStatus.OK);

            if (!towns.isEmpty()){
                String data = convertObejctToJsonString(model);
                ctx.json(data);
            }else{
                ctx.json(new Object[0]);
            }
        });
    }

    private static boolean validateProvince(String aProvince){
        Collection<String> provinces = places.provinces().stream().map(String::toLowerCase).collect(Collectors.toSet());
        Collection<String> provincesWithDash = provinces.stream()
                .map(string ->string.replace(" ","-")).collect(Collectors.toSet());

        boolean provincesCollection = provinces.addAll(provincesWithDash);
        return provinces.contains(aProvince);
    }


    private static Map<String,Object> endPointInfoNoArgs(String type, String endPoint){
        return Map.of(type,endPoint);
    }

    private static Map<String,Object> endPointInfoWithArgs(String type, String endPoint,List<String> arguments){
        return Map.of(type,endPoint,
                "arguments",arguments);
    }




    private static Map<String,String> guideJSON(){
        return Map.of("name","Place-Name-Service"
                ,"author","Johnny-Ilanga"
                ,"version","1.0"
                ,"endpoint","{type: {GET:{/, /provinces, /towns/{province-name} }, POST:{}}");
    }

    private static String  setStatus(Collection<Town> towns, Context ctx){
        String status = (!towns.isEmpty()) ? "OK" : "ERROR";
        configureCtxHttp(status,ctx);
        return status;
    }

    private static String setStatusForProvinces(Collection<String> towns, Context ctx){
        String status = (!towns.isEmpty()) ? "OK" : "ERROR";
        configureCtxHttp(status,ctx);
        return status;
    }

    private static void configureCtxHttp(String status,Context ctx){
        if(status.equals("OK")){
            ctx.status(HttpStatus.OK);
        }else {
            ctx.status(HttpStatus.BAD_REQUEST);
        }
    }
}