package json;

import java.util.List;

public class IntervalJson
{
    public IdentJson id;
    public InterTimesJson times;
    public List<ColOpJson> col_op;
    public List<ProcTimesJson> proc_times;

    public void addInterval(IntervalJson inter) {
        times.addTimes(inter.times);

        for (int i = 0; i < col_op.size(); ++i) {
            if (i >= inter.col_op.size()) {
                System.out.println("[WARN] col_op wrong size: " + inter.col_op.size());
                break;
            }
            col_op.get(i).addColOp(inter.col_op.get(i));
        }

        proc_times.addAll(inter.proc_times);

        if (proc_times.size() != times.nproc)
            System.out.println("[WARN] proc_times wrong size: " + proc_times.size() + ", must be " + times.nproc);
    }
}