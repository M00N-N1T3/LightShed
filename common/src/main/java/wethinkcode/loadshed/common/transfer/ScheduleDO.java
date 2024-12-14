package wethinkcode.loadshed.common.transfer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// The following nested classes are the "domain model" for this service.

// Note that a real model would be a bit more sophisticated and complex,
// and we'd want to put the source in its own package;
// this model is just a dummy "the minimum thing that could possible work"
// so that we can stay focussed on service interconnections and communication
// without getting bogged down in modelling.
public class ScheduleDO
{
    // Using LocalDate.now() is probably a bug! Is the server in the
    // same timezone as the client? Maybe not.
    // What if the server creates an instance one nanosecond before midnight
    // before sending it to the client who then gets it "the next day"?
    // What could you do about all that?
    private LocalDate startDate = LocalDate.now();
    private LocalDate endDate = startDate.plusDays(3L);

    private List<DayDO> loadSheddingDays;

    public ScheduleDO(){
        startDate = setStartDate();
    }

    @JsonCreator
    public ScheduleDO(
        @JsonProperty( value = "days" ) List<DayDO> days ){
        startDate = setStartDate();
        loadSheddingDays = days;
    }

    public List<DayDO> getDays(){
        return loadSheddingDays;
    }

    public int numberOfDays(){
        return getDays().size();
    }

    public LocalDate getStartDate(){
        return LocalDate.from(startDate);
    }

    public LocalDate getEndDate(){
       return endDate;
    }

    private LocalDate setStartDate(){
        LocalDateTime temp = LocalDateTime.now();

        if (temp.getHour() > 23){
            return LocalDate.from(temp.plusDays(1));
        }else{
            return LocalDate.from(temp);
        }
    }

}
