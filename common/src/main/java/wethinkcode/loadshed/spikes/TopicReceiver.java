package wethinkcode.loadshed.spikes;

import javax.jms.*;

import org.apache.activemq.ActiveMQConnectionFactory;
import wethinkcode.loadshed.common.mq.MQ;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * I am a small "maker" app for receiving MQ messages from the Stage Service by
 * subscribing to a Topic.
 */
public class TopicReceiver implements Runnable
{
    private static long NAP_TIME = 2000; //ms

    public static String MQ_TOPIC_NAME = "stage";
    private boolean running = true;
    private Connection connection;
    private MessageConsumer messageConsumer;


    public static void main( String[] args ){
        final TopicReceiver app = new TopicReceiver();
        app.run();
    }

    public TopicReceiver(){

    }

    public TopicReceiver(String MQ_Topic_Name){
        MQ_TOPIC_NAME = MQ_Topic_Name;
    }

    @Override
    public void run(){
        setUpMessageConsumer();
        setMessageConsumerListener();
        startConnection();
        while( running ){
            snooze();
        }
        closeConnection();
        System.out.println( "Bye..." );
    }

    protected void setUpMessageConsumer(){
        try{
            final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory( MQ.URL );
            connection = factory.createConnection( MQ.USER, MQ.PASSWD );

            final Session session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
            final Destination destination = session.createTopic( MQ_TOPIC_NAME ); // <-- NB: Topic, not Queue!

            messageConsumer = session.createConsumer( destination );
        }catch( JMSException erk ){
            throw new RuntimeException( erk );
        }
    }

    protected MessageConsumer getMessageConsumer(){
        return messageConsumer;
    }

    public void setMessageConsumerListener(){
        try{
            messageConsumer.setMessageListener(message ->{
                if (message instanceof TextMessage){
                    handleMessage(message);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void snooze(){
        try{
            Thread.sleep( NAP_TIME );
        }catch( InterruptedException eek ){
            // meh...
        }
    }

    protected void startConnection(){
        try{
            connection.start();
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    protected void closeConnection(){
        if( connection != null ) try{
            running =false;
            connection.close();
        }catch( JMSException ex ){
            // meh
        }
    }

    public void handleMessage(Message message){
        try{
            String body = ((TextMessage) message).getText();

            if ("SHUTDOWN".equals(body)){
                // closing our message que
                System.out.println("....shutting down the system....");
                closeConnection();
                System.exit(0);
            }else{
                System.out.println("Received message: " + body);
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    protected void setRunningStatusTrue() {
        running = true;
    }

     protected void setRunningStatusFalse(){
        running = false;
    }

    protected void checkOverrideOfMethod(List<String> methods){
        String currentClass = this.getClass().getSimpleName();

        List<String> methodsInClass = Arrays.stream(this.getClass().getDeclaredMethods())
                .map(Method::getName).toList();

        for (String method : methods) {
            if (!methodsInClass.contains(method)){
                throw new RuntimeException(method + ": not found in " + currentClass + " class. Override method in " + currentClass +" from the parent class!");
            }
        }
    }
}
