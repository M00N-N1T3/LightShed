package wethinkcode.schedule.mq.listener;


import com.fasterxml.jackson.databind.JsonNode;
import io.javalin.Javalin;
import wethinkcode.loadshed.common.modelview.ModelViewFormatter;
import wethinkcode.loadshed.common.transfer.StageDO;
import wethinkcode.loadshed.spikes.TopicReceiver;
import wethinkcode.schedule.ScheduleService;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.List;


public class ScheduleServiceTopicListener extends TopicReceiver {
    private final Javalin server;

    public ScheduleServiceTopicListener(ScheduleService scheduleService) {
        super("schedule");
        server = scheduleService.getScheduleServer();
    }

    public ScheduleServiceTopicListener(String topic, ScheduleService scheduleService) {
        super(topic);
        server = scheduleService.getScheduleServer();
    }

    public void start() {
        startConnection();
    }

    public void stop() {
        closeConnection();
    }


    @Override
    public void run() {
        setUpMessageConsumer();
        setMessageConsumerListener();
        checkOverrideOfMethod(List.of("setMessageConsumerListener", "handleMessage"));
        startConnection();
    }

    // learnt that overriding alters completely, meaning I do not need to call the method at all
    @Override
    public void setMessageConsumerListener() {
        try {
            // getting the consumer from the super class
            getMessageConsumer().setMessageListener(message -> {
                if (message instanceof TextMessage) {
                    handleMessage(message);
                }
            });
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void handleMessage(Message message) {
        try {

            String body = ((TextMessage) message).getText();

            if ("SHUTDOWN".equals(body)) {
                // closing our message que
                System.out.println("....shutting down the system....");
                closeConnection();
                System.exit(0);
            } else {
                JsonNode jsonNode = ModelViewFormatter.generateJSONNode(body);
                if (nodeIsValid(jsonNode)) {
                    setStage(jsonNode);
                }

            }

        } catch (JMSException exception) {
            System.out.println("Error : " + exception.getMessage());
        }
    }

    private boolean nodeIsValid(JsonNode jsonNode) {
        if (jsonNode.isNull()) return false;
        return jsonNode.has("stage");
    }

    private void setStage(JsonNode jsonNode) {
        StageDO currentStage = server.attribute("stage");
        String stage = jsonNode.get("stage").asText();
        currentStage.setNewStage(stage);
    }

}


