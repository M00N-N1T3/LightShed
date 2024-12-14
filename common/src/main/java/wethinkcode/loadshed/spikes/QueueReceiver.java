package wethinkcode.loadshed.spikes;

import javax.jms.*;

import org.apache.activemq.ActiveMQConnectionFactory;
import wethinkcode.loadshed.common.mq.MQ;

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
        setUpMessageListener();
        startConnection();
        while( running ){
            // do other stuff...
//            System.out.println( "Still doing other things..." );
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
    private void setUpMessageListener(){
        try{
            final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory( MQ.URL );
            connection = factory.createConnection( MQ.USER, MQ.PASSWD );

            final Session session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
            final Destination queueId = session.createQueue( MQ_QUEUE_NAME );

            final MessageConsumer receiver = session.createConsumer( queueId );

            // the more reciever instances we run the more consumers register in our ActiveMq site
            receiver.setMessageListener(message -> {
                // all messages from the que comes here
                System.out.println("Message From Queue: " + message.toString());
                if (message instanceof TextMessage){
                    handleMessage(message);
                }
            }
            );

        }catch( JMSException erk ){
            throw new RuntimeException( erk );
        }
    }

    private void snooze(){
        try{
            Thread.sleep( NAP_TIME );
        }catch( InterruptedException eek ){
            // meh...
        }
    }

    private boolean startConnection(){
        try{
            connection.start();
            return true;
        } catch (JMSException e) {
            System.out.println("Failed to close...");
            return false;
        }

    }

    public void setRunningStatusTrue() {
        running = true;
    }

    public void setRunningStatusFalse(){
       running = false;
    }

    private void closeConnection(){
        if( connection != null ) try{
            connection.close();
        }catch( JMSException ex ){
            // meh
        }
    }

    private void handleMessage(Message message){
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
}
