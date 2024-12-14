package wethinkcode.loadshed.common.mq.sender;

import wethinkcode.loadshed.spikes.TopicSender;

public class StageServiceTopicSender extends TopicSender {
    public StageServiceTopicSender(){
        super("stage");
    }
}
