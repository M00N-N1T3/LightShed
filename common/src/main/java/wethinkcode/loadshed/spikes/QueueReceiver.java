package wethinkcode.loadshed.spikes;

import javax.jms.*;

import org.apache.activemq.ActiveMQConnectionFactory;
import wethinkcode.loadshed.common.mq.MQ;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * I am a small "maker" app for receiving MQ messages from the Stage Service by
 * reading messages from a Queue.
 */
public class QueueReceiver implements Runnable
{
    private static long NAP_TIME = 2000; //ms

    public static String MQ_QUEUE_NAME = "stage";

    private boolean running = true;

    private Connection connection;

    private MessageConsumer messageConsumer;

    public static void main( String[] args ){
        final QueueReceiver app = new QueueReceiver();
        app.run();
    }

    public QueueReceiver(String MQ_Queue_Name){
        MQ_QUEUE_NAME = MQ_Queue_Name;
    }
    
    public QueueReceiver(){
        
    }
    @Override
    public void run(){
        setUpConnection();
        setUpMessageConsumerListener();
        startConnection();
        while( running ){
            snooze();
        }
        closeConnection();
        System.out.println( "Bye..." );
    }

    /**
     * Set up a MQ Session and hook a MessageListener into the MQ machinery. Do this right and,
     * whenever a new Message arrives on the Queue we want to watch, our MessageListener's
     * `onMessage()` method will get called so that we can do something useful with the message.
     */
    protected void setUpConnection(){
        try{
            final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory( MQ.URL );
            connection = factory.createConnection( MQ.USER, MQ.PASSWD );

            final Session session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
            final Destination queueId = session.createQueue( MQ_QUEUE_NAME );

            messageConsumer = session.createConsumer( queueId );
        }catch( JMSException erk ){
            throw new RuntimeException( erk );
        }
    }

    protected void setUpMessageConsumerListener(){
        try{
            // the more reciever instances we run the more consumers register in our ActiveMq site
           messageConsumer.setMessageListener(message -> {
                        // all messages from the que comes here
                        System.out.println("Message From Queue: " + message.toString());
                        if (message instanceof TextMessage){
                            handleMessage(message);
                        }
                    }
            );
        }catch (JMSException exception){
            throw new RuntimeException(exception);
        }
    }
    private void snooze(){
        try{
            Thread.sleep( NAP_TIME );
        }catch( InterruptedException eek ){
            // meh...
        }
    }

    protected boolean startConnection(){
        try{
            connection.start();
            return true;
        } catch (JMSException e) {
            System.out.println("Failed to close...");
            return false;
        }

    }

    protected void setRunningStatusTrue() {
        running = true;
    }

    protected void setRunningStatusFalse(){
       running = false;
    }

    protected void closeConnection(){
        if( connection != null ) try{
            connection.close();
            messageConsumer.close();
        }catch( JMSException ex ){
            // meh
        }
    }

    protected void handleMessage(Message message){
        try{
            String body = ((TextMessage) message).getText();
            if ("SHUTDOWN".equals(body)){
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

    protected MessageConsumer getMessageConsumer(){
        return messageConsumer;
    }

}
