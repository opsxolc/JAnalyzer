package analyzer.json;

import java.util.List;

public class ProcTimesJson {
    public double prod_cpu;
    public double prod_sys;
    public double prod_io;
    public double exec_time;
    public double sys_time;
    public double real_comm;
    public double lost_time;
    public double insuf_user;
    public double insuf_sys;
    public double comm;
    public double idle;
    public double load_imb;
    public double synch;
    public double time_var;
    public double overlap;
    public double thr_user_time;
    public double thr_sys_time;
    public double gpu_time_prod;
    public double gpu_time_lost;
    public long num_threads;
    public long num_gpu;
    public List<ThTimesJson> th_times;
    public List<GPUTimesJson> gpu_times;
}
