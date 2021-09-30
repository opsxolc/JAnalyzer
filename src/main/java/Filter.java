import java.util.function.Predicate;

public class Filter {

    static public void reset(Interval interval){
        interval.setVisible(true);
        for (Interval inter : interval.intervals){
            reset(inter);
        }
    }

    static public void filter(Interval interval, Predicate<Interval> pred){
        interval.setVisible(interval.isVisible() && pred.test(interval));
        for (Interval inter : interval.intervals) {
            filter(inter, pred);
        }
    }

    static public void filterUndo(Interval interval, Predicate<Interval> pred){
        interval.setVisible(interval.isVisible() || !pred.test(interval));
        for (Interval inter : interval.intervals) {
            filterUndo(inter, pred);
        }
    }

}
