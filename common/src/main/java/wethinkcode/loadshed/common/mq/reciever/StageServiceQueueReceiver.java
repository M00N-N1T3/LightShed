package wethinkcode.loadshed.common.mq.reciever;
import wethinkcode.loadshed.spikes.QueueReceiver;

public class StageServiceQueueReceiver extends QueueReceiver {
    public StageServiceQueueReceiver(){
        super("stage");
    }

    public void startQueueListener(){
        this.setRunningStatusTrue();
    }

    public void stopQueueListener(){
        this.setRunningStatusFalse();
    }
}
