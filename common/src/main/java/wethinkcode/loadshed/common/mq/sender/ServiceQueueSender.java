package wethinkcode.loadshed.common.mq.sender;

import wethinkcode.loadshed.spikes.QueueReceiver;

public class ServiceQueueSender extends QueueReceiver {
    public ServiceQueueSender(){
        super("stage");
    }

}
