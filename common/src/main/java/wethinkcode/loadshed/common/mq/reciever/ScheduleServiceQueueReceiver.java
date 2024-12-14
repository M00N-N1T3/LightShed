package wethinkcode.loadshed.common.mq.reciever;

import wethinkcode.loadshed.spikes.QueueReceiver;

public class ScheduleServiceQueueReceiver extends QueueReceiver {

    public ScheduleServiceQueueReceiver(){
        super("schedule");
    }

    public void startQueueListener(){
        this.setRunningStatusTrue();
    }

    public void stopQueueListener(){
        this.setRunningStatusFalse();
    }
}
