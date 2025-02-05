package wethinkcode.loadshed.alert;

import java.io.Serializable;
import java.util.Map;

public class controllerMonitor {
    private static final AlertServiceSender alert =new AlertServiceSender("lightShed_alerts");

    private static Map<String, Serializable> generateAlert(String logLevel, String message, Exception e, String className){
        return Map.of("level",logLevel,
                "class",className,
                "message",message,
                "error", e.getMessage(),
                "cause",e.getCause(),
                "stack",e.getStackTrace());
    }

    public static void sendAlert(Class<?> cls,String logLevel, Exception exception,String message){
        Map<String, Serializable> alertMessage = generateAlert(logLevel,
                message,
                exception,
                cls.getName());
        alert.setMessagePriority(logLevel);
        alert.sendAllMessage(new String[]{alertMessage.toString()});
        //
        alert.execute();
    }
}
