import com.sun.tools.corba.se.idl.ExceptionEntry;
import json.StatJson;
import json.UseStatJson;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Stat implements Cloneable, Comparable<Stat>{

    public StatJson info;
    public Interval interval;
    public String dir;

    public Stat(){}

    public int compareTo(@NotNull Stat compareStat) {
        return 1;
    }

    public Stat clone(){
        Stat res;
        try {
            res = (Stat) super.clone();
        } catch (Exception e) {
            System.out.println("Couldn't clone Stat");
            return null;
        }
        res.info = info;
        res.interval = interval.clone();
        res.dir = dir;
        return res;
    }

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

    public static void cutIntervals(List<List<Interval>> statsIntervals){
        List<Interval> intervals = statsIntervals.get(0);
        ArrayList<Interval> remInters = new ArrayList<>();
        for (Interval inter : intervals) {
            int type = inter.info.id.t;
            int expr = inter.info.id.expr;
            ArrayList<List<Interval>> intersectInters = new ArrayList<>();
            intersectInters.add(inter.intervals);
            boolean found = true;
            for (int j = 0; found && j < statsIntervals.size(); ++j) {
                int offset = 0;
                while (offset < statsIntervals.get(j).size()) {
                    if (statsIntervals.get(j).get(offset).info.id.t == type &&
                            statsIntervals.get(j).get(offset).info.id.expr == expr) {
                        intersectInters.add(statsIntervals.get(j).get(offset).intervals);
                        break;
                    }
                    ++offset;
                }
                found = offset < statsIntervals.get(j).size();
            }
            if (found) {
                cutIntervals(intersectInters);
            } else {
                remInters.add(inter);
            }
        }
        if (!remInters.isEmpty())
            intervals.removeAll(remInters);
    }

    // функция подрезает дерево statIntervals под структуру дерева intervals
    // intervals - образец, statInters - обрезаемое дерево
    public static void rmRedIntervals(List<Interval> intervals, List<Interval> statIntervals){
        ArrayList<Interval> remInters = new ArrayList<>();
        if (intervals.size() == 0) {
            statIntervals.clear();
            return;
        }
        int i = 0;
        for (Interval inter : statIntervals) {
            if (inter.info.id.t == intervals.get(i).info.id.t &&
                    inter.info.id.expr == intervals.get(i).info.id.expr) {
                // нашли очередной интервал из intervals
                // обрезаем его в рекурсивном вызове
                rmRedIntervals(intervals.get(i).intervals, inter.intervals);
                ++i;
            } else {
                remInters.add(inter);
            }
        }
        // удаляем все лишние интервалы
        statIntervals.removeAll(remInters);
    }

    public static void intersectStats(ArrayList<Stat> stats){
        if (stats.size() <= 1)
            return;
        cutIntervals(stats.stream().map(elt -> elt.interval.intervals).collect(Collectors.toList()));
        Stat stat = stats.get(0);
        for (int i = 1; i < stats.size(); ++i){
            rmRedIntervals(stat.interval.intervals, stats.get(i).interval.intervals);
        }
    }

    @Override
    public String toString() {
        return getHeader() + "\n" + interval.toString();
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
