package wethinkcode.common.transfer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalTime;

/**
 * The time slots when teh load shedding will take place in the day
 * Each time slot consists of a <code>start-time</code> and an <code>end-time</code>
 */
public class SlotDO
{
    private LocalTime start;

    private LocalTime end;

    public SlotDO(){
    }

    @JsonCreator
    public SlotDO(
        @JsonProperty( value = "from" ) LocalTime from,
        @JsonProperty( value = "to" ) LocalTime to ){
        start = from;
        end = to;
    }

    public LocalTime getStart(){
        return start;
    }

    public LocalTime getEnd(){
        return end;
    }

}
