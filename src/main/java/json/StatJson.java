package json;

import java.util.List;

public class StatJson
{
    public int nproc;
    public boolean iscomp;
    public String p_heading;
    public List<ProcInfoJson> proc;
    public List<IntervalJson> inter;

    public String getHeader(){
        return inter.get(0).id.pname + " | " + p_heading + " | "
                + String.format("%.3f",inter.get(0).times.exec_time) + "s";
    }
}