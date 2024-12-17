package wethinkcode.stage.mq.Listener;

import wethinkcode.loadshed.spikes.TopicReceiver;

import java.util.List;

public class StageServiceTopicListener extends TopicReceiver {
    public StageServiceTopicListener(){
        super("stage");
    }

    public void StartTopicListener(){
        this.setRunningStatusTrue();
    }

    public void stopTopicListener(){
        this.setRunningStatusFalse();
    }


    @Override
    public void run() {
        setUpMessageConsumer();
        setMessageConsumerListener();
        checkOverrideOfMethod(List.of("setMessageConsumerListener", "handleMessage"));
        startConnection();
    }
}
