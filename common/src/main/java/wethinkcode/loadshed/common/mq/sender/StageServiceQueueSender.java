package wethinkcode.loadshed.common.mq.sender;

import wethinkcode.loadshed.spikes.QueueSender;

public class StageServiceQueueSender extends QueueSender {
    public StageServiceQueueSender(){
        super("stage");
    }

}
