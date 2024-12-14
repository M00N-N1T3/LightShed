package wethinkcode.loadshed.common.mq.reciever;

import wethinkcode.loadshed.spikes.TopicReceiver;

public class ScheduleServiceTopicReceiver  extends TopicReceiver {
    public ScheduleServiceTopicReceiver(){
        super("schedule");
    }

    public void StartTopicListener(){
        this.setRunningStatusTrue();
    }

    public void stopTopicListener(){
        this.setRunningStatusFalse();
    }
}


