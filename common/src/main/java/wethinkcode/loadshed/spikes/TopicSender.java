package wethinkcode.loadshed.spikes;

import javax.jms.*;

import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.Map;

/**
 * I am a small "maker" app for receiving MQ messages from the Stage Service.
 */
public class TopicSender implements Runnable {
    private static long NAP_TIME = 2000; //ms

    // connection const
    public static final String MQ_URL = "tcp://localhost:61616";
    public static final String MQ_USER = "admin";
    public static final String MQ_PASSWD = "admin";

    // topic const
    public static String MQ_TOPIC_NAME = "stage";
    public static Destination topicId;

    // message
    public static MessageProducer msgProducer;
    public static MessageFormatter messageFormatter;

    private String[] cmdLineMsgs;
    private Connection connection;
    private Session session;

    public static void main(String[] args) {
        final TopicSender app = new TopicSender();
        app.cmdLineMsgs = args;
        app.run();
    }

    public TopicSender(){

    }

    public TopicSender(String MQ_Topic_Name){
        MQ_TOPIC_NAME =MQ_Topic_Name;
    }


    @Override
    public void run() {

        if (setUpConnection()) {
            startConnection();
            try {
                sendMessages(cmdLineMsgs.length == 0
                        ? new String[]{"{ \"stage\":17 }"}
                        : cmdLineMsgs);

            } catch (JMSException error) {
                throw new RuntimeException(error);
            } finally {
                closeResources();
            }
            System.out.println("Bye...");
        } else {
            System.out.println("...failed to start system...");
            System.exit(1);
        }

    }

    private boolean setUpConnection() {
        try {
            // setting up connection
            final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(MQ_URL);
            connection = factory.createConnection(MQ_USER, MQ_PASSWD);
            connection.setExceptionListener(exceptionListener);
            session = connection.createSession();

            // setting up destination
            topicId = session.createTopic(MQ_TOPIC_NAME);

            // Messenger
            messageFormatter = new MessageFormatter(session);
            msgProducer = session.createProducer(topicId);

            return true;
        } catch (JMSException error) {
            throw new RuntimeException(error);
        }
    }

    private boolean startConnection() {
        try {
            connection.start();
            return true;
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    private void closeResources() {
        try {
            if (session != null) session.close();
            if (msgProducer != null) msgProducer.close();
            if (connection != null) connection.close();
        } catch (JMSException ex) {
            // wut?
        }
        session = null;
        msgProducer = null;
        connection = null;
    }

    private final ExceptionListener exceptionListener = new ExceptionListener() {
        @Override
        public void onException(JMSException e) {
            System.out.println("something went wrong when attempting to connect");
            System.out.println(e.getMessage());
        }
    };

    private void sendMessages(String[] messages) throws JMSException {
        sendAllMessages(messages);
    }

    // todo: fix the onCompletion it is currently broken
    private void sendAllMessages(String[] messages) throws JMSException {
        StreamMessage messageStream = messageFormatter.getStreamMessage(messages);
            msgProducer.send(messageStream);
    }

    // send message will be generic method, that can handle either Streams, Strings or Maps
    private void sendMessage(String message) {
        try {
            TextMessage textMessage = messageFormatter.getTextMessage(message);
            msgProducer.send(textMessage);
        } catch (JMSException error) {
            throw new RuntimeException(error);
        }
    }

    private void sendMapMessage(Map<String, ?> mappedMessage) {
        try {
            MapMessage message = messageFormatter.getMapMessage(mappedMessage);
            msgProducer.send(message);
        } catch (JMSException error) {
            throw new RuntimeException(error);
        }
    }

    private void sendMessage(Message message) {
        try {
            msgProducer.send(message);
        } catch (JMSException error) {
            throw new RuntimeException(error);
        }
    }

}
