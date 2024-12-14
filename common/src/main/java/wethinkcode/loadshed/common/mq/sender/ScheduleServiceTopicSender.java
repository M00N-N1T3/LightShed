package wethinkcode.loadshed.common.mq.sender;

import wethinkcode.loadshed.spikes.TopicSender;

public class ScheduleServiceTopicSender extends TopicSender {
    public ScheduleServiceTopicSender(){
        super("schedule");
    }
}
