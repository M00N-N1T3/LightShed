package wethinkcode.loadshed.alert;

import wethinkcode.loadshed.spikes.QueueSender;

import javax.jms.JMSException;

public class AlertServiceSender extends QueueSender {

    public AlertServiceSender(String queue){
        super(queue);
        setUpConnection(); // from parent class
    }

    public void sendAllMessage(String[] message){
        setMessage(message);
    }

    public void execute(){
        this.run();
    }

    public void setMessagePriority(String messagePriority){
        int priority = 4;
        if ("severe".equals(messagePriority)) priority = 9;
        if ("moderate".equals(messagePriority)) priority = 6;
        try{
            setUpConnection();
            getMsgProducer().setPriority(priority);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
