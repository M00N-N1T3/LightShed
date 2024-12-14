package wethinkcode.loadshed.common.mq.sender;

import wethinkcode.loadshed.spikes.QueueSender;

public class ScheduleServiceQueueSender extends QueueSender {
    public ScheduleServiceQueueSender(){
        super("schedule");
    }
}
