import json.StatJson;
import json.UseStatJson;

public class Stat {
    public StatJson info;
    public Interval interval;

    public Stat(StatJson statJson, String dir, boolean withText){
        info = statJson;
        interval = new Interval(info.inter, dir, withText);
    }

    public Stat(String stat, String dir, boolean withText){
        info = UseStatJson.GetStat(stat);
        interval = new Interval(info.inter, dir, withText);
    }

    public String getHeader(){
        return interval.info.id.pname + " | " + info.p_heading + " | "
                + String.format("%.3f", interval.info.times.exec_time) + "s";
    }

}
