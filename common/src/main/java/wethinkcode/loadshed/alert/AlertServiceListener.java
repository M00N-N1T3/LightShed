package wethinkcode.loadshed.alert;

import wethinkcode.loadshed.spikes.QueueReceiver;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.List;


public class AlertServiceListener extends QueueReceiver {
    public AlertServiceListener(String topic){
        super(topic);
    }

    @Override
    public void run() {
        setUpConnection();
        setUpMessageConsumerListener();
        checkOverrideOfMethod(List.of("setUpMessageConsumerListener","handleMessage"));
        startConnection();

    }

    @Override
    protected void setUpMessageConsumerListener() {
        try {
            getMessageConsumer().setMessageListener(message -> {
                if (message instanceof TextMessage) {
                    handleMessage(message);
                }
            });
        }catch (JMSException exception){
            throw new RuntimeException(exception);
        }
    }

    @Override
    protected void handleMessage(Message message) {
        try {
            String body = ((TextMessage) message).getText();

            if("SHUTDOWN".equals(body)){
                System.out.println("...Shutting down the system...");
                closeConnection();
            }else{
                System.out.println("Alert: " + body);
            }

        }catch (JMSException exception){
            throw new RuntimeException(exception);
        }
    }

}
