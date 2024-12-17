package wethinkcode.schedule.mq.sender;

import wethinkcode.loadshed.spikes.QueueSender;

public class ScheduleServiceQueueSender extends QueueSender {
    public ScheduleServiceQueueSender(){
        super("schedule");
    }
}
