package wethinkcode.schedule;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.annotations.VisibleForTesting;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;

import wethinkcode.loadshed.common.mq.listener.ServiceTopicListener;
import wethinkcode.loadshed.common.transfer.StageDO;
import wethinkcode.schedule.router.Router;
import wethinkcode.loadshed.common.transfer.DayDO;
import wethinkcode.loadshed.common.transfer.ScheduleDO;
import wethinkcode.loadshed.common.transfer.SlotDO;

/**
 * I provide a REST API providing the current loadshedding schedule for a
 * given town (in a specific province) at a given loadshedding stage.
 */
public class ScheduleService {
    public static final int DEFAULT_STAGE = 0; // no loadshedding. Ha!
    private final static StageDO stageDO = new StageDO(DEFAULT_STAGE);
    public static final int DEFAULT_PORT = 7002;
    private ServiceTopicListener scheduleServiceTopicListener;

    Javalin scheduleServer;

    private int servicePort;

    public static void main( String[] args ){
        final ScheduleService svc = new ScheduleService().initialise();
        svc.start();
    }

    @VisibleForTesting
    ScheduleService initialise(){
        scheduleServer = Javalin.create(javalinConfig -> {
//            javalinConfig.enableDevLogging();
            javalinConfig.showJavalinBanner = false;
            javalinConfig.http.defaultContentType = "application/json";
            javalinConfig.routing.treatMultipleSlashesAsSingleSlash = true;
        });

        scheduleServer.attribute("schedule",this);
        scheduleServer.attribute("stage",stageDO);
        scheduleServer = initHttpServer();

        // Topic Listener
        scheduleServiceTopicListener = new ServiceTopicListener("stage",scheduleServer);
        return this;
    }

    public Javalin getScheduleServer(){
        return scheduleServer;
    }

    public void start(){
        start( DEFAULT_PORT );
    }

    @VisibleForTesting
    void start( int networkPort ){
        servicePort = networkPort;
        scheduleServiceTopicListener.run();
        run();
    }


    public void stop(){
        scheduleServer.stop();
        scheduleServiceTopicListener.stop();
    }

    public void run(){
        scheduleServer.start( servicePort );
    }


    public void routes(EndpointGroup group){
        scheduleServer.routes(group);
    }

    private Javalin initHttpServer(){
        Router.configureRoutes(this);
        return scheduleServer;
    }

    // There *must* be a better way than this...
    // See Steps 4 and 5 (the optional ones!) in the course notes.
     public Optional<ScheduleDO> getSchedule( String province, String town, int stage ){
        return province.equalsIgnoreCase( "Mars" )
            ? Optional.of(emptySchedule())
            : Optional.of( mockSchedule() );
    }

    /**
     * Answer with a hard-coded/mock Schedule.
     * @return A non-null, slightly plausible Schedule.
     */
    private static ScheduleDO mockSchedule(){
        final List<SlotDO> slots = List.of(
            new SlotDO( LocalTime.of( 2, 0 ), LocalTime.of( 4, 0 )),
            new SlotDO( LocalTime.of( 10, 0 ), LocalTime.of( 12, 0 )),
            new SlotDO( LocalTime.of( 18, 0 ), LocalTime.of( 20, 0 ))
        );
        final List<DayDO> days = List.of(
            new DayDO( slots ),
            new DayDO( slots ),
            new DayDO( slots ),
            new DayDO( slots )
        );
        return new ScheduleDO( days );
    }

    /**
     * Answer with a non-null but empty Schedule.
     * @return The empty Schedule.
     */
    private static ScheduleDO emptySchedule(){
        final List<SlotDO> slots = Collections.emptyList();
        final List<DayDO> days = Collections.emptyList();
        return new ScheduleDO( days );
    }
}
