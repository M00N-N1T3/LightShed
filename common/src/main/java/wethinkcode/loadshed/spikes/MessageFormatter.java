package wethinkcode.loadshed.spikes;

import javax.jms.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;


public class MessageFormatter {
    private TextMessage  textMessage;
    private StreamMessage streamMessage;
    private MapMessage mapMessage;

    public MessageFormatter(Session session){
        try {
            textMessage = session.createTextMessage(); // sends a String as a TextMessage
            streamMessage = session.createStreamMessage(); // sends a Stream of Objects
            mapMessage = session.createMapMessage(); // sends a map of objects
        } catch (JMSException error) {
            throw new RuntimeException(error);
        }
    }

    public MessageFormatter ConfigureMessageFormatter(TextMessage textMessage, StreamMessage streamMessage, MapMessage mapMessage){
        this.textMessage =textMessage;
        this.streamMessage = streamMessage;
        this.mapMessage = mapMessage;
        return this;
    }

    // package private methods
    TextMessage getTextMessage(String message) throws JMSException {
        textMessage.setText(message);
        return textMessage;
    }

    // when using a stream we need to write the message when we want to send it
    StreamMessage getStreamMessage(String[] messages){
        //
        try {
            for (String message : messages) {
                streamMessage.writeString(message);
            }
        } catch (JMSException error) {
            throw new RuntimeException(error);
        }
        return streamMessage;
    }

    /**
     * A Generic Message Mapper for the mapMessage for our message producer.
     * You can provide the map mixed data type, and it will find the correct setter for the specified data type
     * @param mapOfObjects The map of objects we want to set in our MapMessage
     * @return mapMessage
     * @param <T> Any primitive data type
     */
    <T> MapMessage getMapMessage(Map<String,T> mapOfObjects){
        Set<String> keysInTheMap = mapOfObjects.keySet();
        Object prevObject = new Object();
        Method methodToUseForObject = null;

        for (String keyForObject : keysInTheMap){
            Object object = mapOfObjects.get(keyForObject);
            // we only run this in the event the object type has changed
            if (!prevObject.getClass().equals(object.getClass())){
                methodToUseForObject = getMessageMethod(object,mapMessage,"set"); // all methods in the mapMessage starts with set                oldObject = object;
            }
            if (methodToUseForObject != null) invokeMapMethod(methodToUseForObject,keyForObject,object);
        }

        return mapMessage;
    }

    /**
     * Dynamically invoking the correct set method for our mapMessage object based on the class (data-type) of the object
     * @param object the object whose method we want to retrieve
     * @param messageClass the Message class we want to make use of, to generate our Message
     * @param methodPrefix the prefix of the method we want to retrieve
     * @return the relevant method
     * <br>
     * <p>Method names in our Message class are made up of a <code>prefix + dataType</code>
     * <br>Thus if our prefix is set and the dataType is boolean, our invoked method will be setBoolean</p>
     */
    private <T> Method getMessageMethod(T object, Message messageClass, String methodPrefix){
        String classNameOfObject = object.getClass().getSimpleName();
        Class<?>[] methodParameters = {String.class, getPrimitiveClass(classNameOfObject.toLowerCase())};
        try{
            Class<?> classForMethod = messageClass.getClass();
            classNameOfObject = ("Integer".equals(classNameOfObject))
                    ? classNameOfObject = "Int"
                    : classNameOfObject;

            String methodName = methodPrefix + classNameOfObject; // generating a class specific setter
            return classForMethod.getMethod(methodName,methodParameters); // the method we intend
        }catch (NoSuchMethodException e){
            throw new IllegalArgumentException("Method not found: " + e.getMessage());
        }
    }

    /**
     * By invoking the method we are calling it. each call is an execution during run tine
     * @param methodToInvoke the method we want to execute
     * @param key the key of the object we want to save
     * @param object the object we want to save
     * @param <T> generic signature
     */
    private <T> void invokeMapMethod(Method methodToInvoke, String key, T object){
        Object[] methodParameters = {key,object};
        try{
            methodToInvoke.invoke(mapMessage, methodParameters);
        } catch (InvocationTargetException | IllegalAccessException error) {
            throw new RuntimeException(error);
        }
    }


    private <T> void invokeStreamMethod(Method methodToInvoke,T object){
        try{
            methodToInvoke.invoke(streamMessage,object);
        } catch (InvocationTargetException| IllegalAccessException error) {
            throw new RuntimeException(error);
        }
    }

    private Class<?> getPrimitiveClass(String typeName) {
        return switch (typeName) {
            case "byte" -> byte.class;
            case "short" -> short.class;
            case "int" -> int.class;
            case "long" -> long.class;
            case "char" -> char.class;
            case "float" -> float.class;
            case "double" -> double.class;
            case "boolean" -> boolean.class;
            case "void" -> void.class;
            default -> throw new IllegalArgumentException("Not primitive type : " + typeName);
        };
    }


}
