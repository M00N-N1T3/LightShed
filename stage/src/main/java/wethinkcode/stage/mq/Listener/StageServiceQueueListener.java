package wethinkcode.stage.mq.Listener;
import wethinkcode.loadshed.spikes.QueueReceiver;

public class StageServiceQueueListener extends QueueReceiver {
    public StageServiceQueueListener(){
        super("stage");
    }

    public void startQueueListener(){
        this.setRunningStatusTrue();
    }

    public void stopQueueListener(){
        this.setRunningStatusFalse();
    }
}
