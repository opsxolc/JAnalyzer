import json.StatJson;
import json.UseStatJson;

public class Stat {

    public StatJson info;
    public Interval interval;
    public String dir;

    public Stat(StatJson statJson, String dir, boolean withText){
        this.dir = dir;
        info = statJson;
        interval = new Interval(info.inter, dir, withText);
    }

    public Stat(String stat, String dir, boolean withText){
        this.dir = dir;
        info = UseStatJson.GetStat(stat);
        interval = new Interval(info.inter, dir, withText);
    }

    public String getHeader(){
        return interval.info.id.pname + "  |  " + info.p_heading + "  |  "
                + String.format("%.3f", interval.info.times.exec_time) + "s";
    }

//    public Stat(File path, boolean withText)
//    {
//        dir = Path.GetDirectoryName(path);
//        StreamReader reader = new StreamReader(path);
//        string json = reader.ReadToEnd();
//        reader.Close();
//        Info = UseStatJson.GetStat(json);
//        Interval = new Interval(Info.inter, Dir, withText);
//    }

//    public Stat(String json, String dir, boolean withText)
//    {
//        this.dir = dir;
//        info = UseStatJson.GetStat(json);
//        interval = new Interval(info.inter, dir, withText);
//    }

}
