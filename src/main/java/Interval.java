import json.IntervalJson;

import java.util.ArrayList;
import java.util.List;

public class Interval {
    public List<Interval> intervals = new ArrayList<Interval>();
    public IntervalJson info;
    public String text = "";

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

    public String getGradient(){
        String result = "-fx-background-color: linear-gradient(to right, transparent, #B5FF33)";

        return result;
    }
}
