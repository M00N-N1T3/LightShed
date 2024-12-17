package wethinkcode.schedule.mq.listener;

import io.javalin.Javalin;
import wethinkcode.loadshed.spikes.QueueReceiver;
import wethinkcode.schedule.ScheduleService;

import java.util.List;

public class ScheduleServiceQueueListener extends QueueReceiver {
    private final Javalin server;

    public ScheduleServiceQueueListener(ScheduleService scheduleService) {
        super("schedule");
        server = scheduleService.getScheduleServer();
    }

    public ScheduleServiceQueueListener(String queue,ScheduleService scheduleService) {
        super(queue);
        server = scheduleService.getScheduleServer();
    }



    public void startQueueListener(){
        this.setRunningStatusTrue();
    }

    public void stopQueueListener(){
        this.setRunningStatusFalse();
    }


}
