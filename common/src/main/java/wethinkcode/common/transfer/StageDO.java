package wethinkcode.common.transfer;

/**
 * I am a data/transfer object for communicating the loadshedding stage.
 */
public class StageDO
{
    private int stage;

    /**
     * Default constructor is needed otherwise the JSON mapper
     * can't create an instance.
     */
    public StageDO(){
        stage = 0;
    }

    public StageDO( int stage ){
        this.stage = stage;
    }

    public StageDO(String stage){
        this.stage = Integer.parseInt(stage);
    }

    public void setStage(int stage){
        this.stage = stage;
    }

    public int getStage(){
        return stage;
    }

}
