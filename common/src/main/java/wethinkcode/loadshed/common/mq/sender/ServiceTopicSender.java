package wethinkcode.loadshed.common.mq.sender;

import wethinkcode.loadshed.spikes.TopicSender;

public class ServiceTopicSender extends TopicSender {
    public ServiceTopicSender(){
        super("stage");
    }

    public ServiceTopicSender(String topic){
        super(topic);
    }

    public void execute(String[] messagesToSend){
        this.setCmdLineMsgs(messagesToSend);
        this.run();
    }
}
