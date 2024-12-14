package wethinkcode.loadshed.spikes;

import javax.jms.*;
import javax.swing.*;

import org.apache.activemq.ActiveMQConnectionFactory;
import wethinkcode.loadshed.common.mq.MQ;

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
        setUpMessageListener();
        strtConenction();
        while( running ){
//            System.out.println( "Still doing stufff..." );
            snooze();
        }
        closeConnection();
        System.out.println( "Bye..." );
    }

    private void setUpMessageListener(){
        try{
            final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory( MQ.URL );
            connection = factory.createConnection( MQ.USER, MQ.PASSWD );

            final Session session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
            final Destination dest = session.createTopic( MQ_TOPIC_NAME ); // <-- NB: Topic, not Queue!

            final MessageConsumer receiver = session.createConsumer( dest );

            receiver.setMessageListener( message->{
                // this is for debug purpose only
                System.out.println("Message From Topic: " + message.toString());
                if (message instanceof TextMessage){
                    handleMessage(message);
                }
            });

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

    private void strtConenction(){
        try{
            connection.start();
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
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

    public void setRunningStatusTrue() {
        running = true;
    }

    public void setRunningStatusFalse(){
        running = false;
    }

}
