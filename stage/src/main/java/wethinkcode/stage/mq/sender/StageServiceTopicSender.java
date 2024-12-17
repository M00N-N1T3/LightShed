package wethinkcode.stage.mq.sender;

import wethinkcode.loadshed.spikes.TopicSender;

public class StageServiceTopicSender extends TopicSender {
    public StageServiceTopicSender(){
        super("stage");
    }

    public void execute(String[] messagesToSend){
        this.setCmdLineMsgs(messagesToSend);
        this.run();
    }
}
