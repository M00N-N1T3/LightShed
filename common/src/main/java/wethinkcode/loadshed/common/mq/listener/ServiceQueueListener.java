package wethinkcode.loadshed.common.mq.listener;

import wethinkcode.loadshed.spikes.QueueReceiver;

public class ServiceQueueListener extends QueueReceiver {

    public ServiceQueueListener(String topic){
        super(topic);
    }
    public void startQueueListener(){
        this.setRunningStatusTrue();
    }
    public void stopQueueListener(){
        this.setRunningStatusFalse();
    }

}
