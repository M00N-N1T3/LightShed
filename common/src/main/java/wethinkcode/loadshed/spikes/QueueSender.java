package wethinkcode.loadshed.spikes;

import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import java.util.*;


/**
 * I am a small "maker" app for receiving MQ messages from the Stage Service.
 */
public class QueueSender implements Runnable
{
    private static long NAP_TIME = 2000; //ms

    public static final String MQ_URL = "tcp://localhost:61616";

    public static final String MQ_USER = "admin";

    public static final String MQ_PASSWD = "admin";

    public static String MQ_QUEUE_NAME = "stage";

    private String[] cmdLineMsgs={};

    private Connection connection;

    private Session session;

    private MessageProducer msgProducer;
    private Destination queueId;
    private MessageFormatter messageFormatter;

    public static void main( String[] args ){
        final QueueSender app = new QueueSender();
        app.run();
    }

    public QueueSender(){

    }

    public QueueSender(String MQ_QueueName){
        MQ_QUEUE_NAME = MQ_QueueName;
    }

    @Override
    public void run() {

        if (setUpConnection()){
            startConnection();
            try {
                sendAllMessages(cmdLineMsgs.length == 0
                        ? new String[]{"{ \"stage\":17 }"}
                        : cmdLineMsgs);

            } catch (JMSException error) {
                throw new RuntimeException(error);
            } finally {
                closeResources();
            }
            System.out.println("Bye...");
        }else {
            System.out.println("...failed to start system...");
            System.exit(1);
        }

    }

    private boolean setUpConnection(){
        try{
            // setting up connection
            final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(MQ_URL); // the address we're connecting to
            connection = factory.createConnection(MQ_USER, MQ_PASSWD); // takes our username and password to establish connections
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // destination, the destination to which we will be sending our message too
            queueId = session.createQueue(MQ_QUEUE_NAME);

            // sets up my sessions TextMessage, StreamMessage and MapMessage objects
            messageFormatter = new MessageFormatter(session); // Do read this class, It is very intriguing
            msgProducer = session.createProducer(queueId); // the producer will be used to send the message
            return true;
        } catch (JMSException error) {
            return false;
        }
    }

    private boolean startConnection(){
        try{
            connection.start();
            return true;
        } catch (JMSException error) {
            return false;
        }
    }

    private void sendAllMessages( String[] messages) throws JMSException {
        try{
            StreamMessage messageStream = messageFormatter.getStreamMessage(messages);
            msgProducer.send(messageStream);
        }catch (JMSException error){
            throw new RuntimeException(error);
        }
    }


    // send message will be generic method, that can handle either Streams, Strings or Maps
    private void sendMessage(String message){
        try{
            TextMessage textMessage = messageFormatter.getTextMessage(message);
            msgProducer.send(textMessage);
        } catch (JMSException error) {
            throw new RuntimeException(error);
        }
    }

    private void sendMapMessage(Map<String, ?> mappedMessage){
        try{
            MapMessage message = messageFormatter.getMapMessage(mappedMessage);
            msgProducer.send(message);
        } catch (JMSException error) {
            throw new RuntimeException(error);
        }
    }
    private void sendMessage(Message message){
        try{
            msgProducer.send(message);
        }catch (JMSException error){
            throw new RuntimeException(error);
        }
    }

    private void closeResources(){
        try{
            if( session != null ) session.close();
            if(msgProducer != null) msgProducer.close();
            if( connection != null ) connection.close();
        }catch( JMSException error ){
            //
        }
        session = null;
        msgProducer = null;
        connection = null;
    }

}


