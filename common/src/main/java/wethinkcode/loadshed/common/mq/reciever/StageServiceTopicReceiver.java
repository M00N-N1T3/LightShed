package wethinkcode.loadshed.common.mq.reciever;

import wethinkcode.loadshed.spikes.TopicReceiver;

public class StageServiceTopicReceiver extends TopicReceiver {
    public StageServiceTopicReceiver(){
        super("stage");
    }

    public void StartTopicListener(){
        this.setRunningStatusTrue();
    }

    public void stopTopicListener(){
        this.setRunningStatusFalse();
    }
}
