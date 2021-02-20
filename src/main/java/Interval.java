import json.IntervalJson;
import json.ProcTimesJson;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Interval implements Cloneable {
    public List<Interval> intervals = new ArrayList<>();
    public IntervalJson info;
    public String text = "";

    public static final int SEQ = 21, PAR = 22, USER = 23;

    public Interval(){}

    public String getType(){
        switch (info.id.t) {
            case SEQ:
                return "Посл";
            case PAR:
                return "Пар";
            case USER:
                return String.valueOf(info.id.expr);
            default: return "";
        }
    }

    public Interval clone(){
        Interval res;
        try {
            res = (Interval) super.clone();
        } catch (Exception e) {
            System.out.println("Couldn't clone Interval");
            return null;
        }
        res.info = info;
        res.text = text;
        res.intervals = intervals.stream().map(Interval::clone).collect(Collectors.toList());
        return res;
    }

    public long getGPUNum(){
        return info.proc_times.stream().map(elt -> elt.num_gpu).reduce((long)0, Long::sum);
    }

//    public bool HasChildLoopInterval
//    {
//        get
//        {
//            foreach (var inter in Intervals)
//            {
//                if (inter.Info.id.t != (int)InterTypes.USER)
//                    return true;
//            }
//            bool result = false;
//            int i = 0;
//            while (i < Intervals.Count && !result)
//            {
//                result |= Intervals[i].HasChildLoopInterval;
//                ++i;
//            }
//            return result;
//        }
//    }

    public Interval(List<IntervalJson> intervals, String dir, boolean withText)
    {
        if (intervals == null || intervals.size() == 0)
            return;
        int i = 1;
        info = intervals.get(0);

        //TODO: Добавить чтение текста
//        if (withText)
//            try {
//                var Lines = File.ReadLines(dir + '/' + Info.id.pname);
//                for (int j = Info.id.nline - 1; j < Info.id.nline_end; ++j)
//                    Text += Lines.ElementAt(j) + '\n';
//                HasText = true;
//            } catch (Exception e)
//            {
//                Text = e.Message;
//            }
        while (i < intervals.size())
        {
            if (intervals.get(i).id.nlev == info.id.nlev + 1)
            {
                int j = i + 1;
                while (j < intervals.size() && intervals.get(j).id.nlev > intervals.get(i).id.nlev)
                    ++j;

                this.intervals.add(new Interval(intervals.subList(i, j), dir, withText));
                i = j - 1;
            }
            ++i;
        }
    }

    public String getGradient(double timeLost){
        String result = "-fx-background-color: linear-gradient(to right, transparent, transparent";
        if (info.times.comm >= timeLost * 0.2)
            result += ", rgb(173, 255, 47)";
        if (info.times.idle >= timeLost * 0.2)
            result += ", rgb(135, 206, 249)";
        if (info.times.insuf_user >= timeLost * 0.2)
            result += ", rgb(217, 113, 214)";
        if (info.times.insuf_sys >= timeLost * 0.2)
            result += ", rgb(255, 192, 203)";
        result += ")";
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < info.id.nlev; ++i)
            sb.append('\t');
        sb.append(info.id.expr).append(" - ").append(String.format("%.2f", info.times.exec_time));
        for (Interval inter : intervals)
            sb.append('\n').append(inter.toString());
        return sb.toString();
    }
}
