import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import json.GPUMetricJson;
import json.GPUTimesJson;

import java.text.DecimalFormat;
import java.text.NumberFormat;

//---  Названия метрик для отображения ГПУ  ---//
enum DVMHStatMetrics{
    DVMH_STAT_METRIC_KERNEL_EXEC,
    /* DVMH-CUDA memcpy */
    DVMH_STAT_METRIC_CPY_DTOH,
    DVMH_STAT_METRIC_CPY_HTOD,
    DVMH_STAT_METRIC_CPY_DTOD,
    /* DVMH memcpy */
    DVMH_STAT_METRIC_CPY_SHADOW_DTOH,
    DVMH_STAT_METRIC_CPY_SHADOW_HTOD,
    DVMH_STAT_METRIC_CPY_SHADOW_DTOD,
    DVMH_STAT_METRIC_CPY_REMOTE_DTOH,
    DVMH_STAT_METRIC_CPY_REMOTE_HTOD,
    DVMH_STAT_METRIC_CPY_REMOTE_DTOD,
    DVMH_STAT_METRIC_CPY_REDIST_DTOH,
    DVMH_STAT_METRIC_CPY_REDIST_HTOD,
    DVMH_STAT_METRIC_CPY_REDIST_DTOD,
    DVMH_STAT_METRIC_CPY_IN_REG_DTOH,
    DVMH_STAT_METRIC_CPY_IN_REG_HTOD,
    DVMH_STAT_METRIC_CPY_IN_REG_DTOD,
    DVMH_STAT_METRIC_CPY_GET_ACTUAL,
    /* DVMH loop events */
    DVMH_STAT_METRIC_LOOP_PORTION_TIME,
    /* DVMH utility functions events */
    DVMH_STAT_METRIC_UTIL_ARRAY_TRANSFORMATION,
    DVMH_STAT_METRIC_UTIL_ARRAY_REDUCTION,
    DVMH_STAT_METRIC_UTIL_RTC_COMPILATION,
    DVMH_STAT_METRIC_UTIL_PAGE_LOCK_HOST_MEM,
    // --
    DVMH_STAT_METRIC_FORCE_INT
};

public class GPUPane extends GridPane {
    private final Label titleLabel = new Label();

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    private static final int DVMHStatMetricCount =
            DVMHStatMetrics.DVMH_STAT_METRIC_FORCE_INT.ordinal();
    private static final String[] dvmhStatMetricsTitles = {
        "Kernel executions",
        "Copy GPU to CPU",
        "Copy CPU to GPU",
        "Copy GPU to GPU",
        "[Shadow] Copy GPU to CPU",
        "[Shadow] Copy CPU to GPU",
        "[Shadow] Copy GPU to GPU",
        "[Remote] Copy GPU to CPU",
        "[Remote] Copy CPU to GPU",
        "[Remote] Copy GPU to GPU",
        "[Redistribution] Copy GPU to CPU",
        "[Redistribution] Copy CPU to GPU",
        "[Redistribution] Copy GPU to GPU",
        "[Region IN] Copy GPU to CPU",
        "[Region IN] Copy CPU to GPU",
        "[Region IN] Copy GPU to GPU",
        "GET_ACTUAL",
        "Loop execution",
        "Data reorganization",
        "Reduction",
        "GPU Runtime compilation",
        "Page lock host memory"
    };

    NumberFormat f4 = new DecimalFormat("#0.0000");

    private String prepareValue(double value, boolean isPositive, boolean isSize,
                                boolean dashOnZero, boolean isTime)
    {
        if (isPositive && value < 0)
            return "⏤";
        if (value == 0 && dashOnZero)
            return "⏤";
        if (isSize)
        {
            if (value - 0.001 < 0) return "⏤";
            if (value >= ((long)1 << 30))
                return value / ((long)1 << 30) +"G";
            if (value >= ((long)1 << 20))
                return value / ((long)1 << 20) +"M";
            if (value >= ((long)1 << 10))
                return value / ((long)1 << 10) +"K";
            if (value >= 0)
                return value + "B";
            return "?";
        }
        if (isTime)
            return f4.format(value) + "s";
        return f4.format(value);
    }

    public GPUPane() {
        super();
        add(titleLabel, 0, 0, 2, 1); // TODO: Style title and pane
        add(new Label("#"), 1, 1);
        add(new Label("min"), 2, 1);
        add(new Label("max"), 3, 1);
        add(new Label("Sum"), 4, 1);
        add(new Label("Average"), 5, 1);
        add(new Label("Productive"), 6, 1);
        add(new Label("Lost"), 7, 1);
        for (Node node: getChildren()) {
            setHalignment(node, HPos.CENTER);
            setValignment(node, VPos.CENTER);
        }
    }

    public void Init(GPUTimesJson gpuTimes, int gpuNum){
        String title = "GPU #" + gpuNum + "(" + gpuTimes.gpu_name + ")";
        setTitle(title);
        int rowNum = 2;
        for (int i = 0; i < DVMHStatMetricCount; ++i)
        {
            GPUMetricJson metric = gpuTimes.metrics.get(i);
            if (metric.countMeasures <= 0)
                continue;
            boolean isSize = i >= DVMHStatMetrics.DVMH_STAT_METRIC_CPY_DTOH.ordinal() &&
                i <= DVMHStatMetrics.DVMH_STAT_METRIC_CPY_GET_ACTUAL.ordinal() ||
                i == DVMHStatMetrics.DVMH_STAT_METRIC_UTIL_ARRAY_TRANSFORMATION.ordinal();
            addRow(rowNum++,
                    new Label(dvmhStatMetricsTitles[i]),
                    new Label(String.valueOf(metric.countMeasures)),
                    new Label(prepareValue(metric.min, isSize, isSize, false, false)),
                    new Label(prepareValue(metric.max, isSize, isSize, false, false)),
                    new Label(prepareValue(metric.sum, isSize, isSize, false, false)),
                    new Label(prepareValue(metric.mean, isSize, isSize, false, false)),
                    new Label(prepareValue(metric.timeProductive, true, false, true, true)),
                    new Label(prepareValue(metric.timeLost, true, false, true, true))
                );
        }
        addRow(rowNum++, new Label("Productive time      "
                + prepareValue(gpuTimes.prod_time, true, false, true, true)));
        addRow(rowNum, new Label("Lost time                "
                + prepareValue(gpuTimes.lost_time, true, false, true, true)));
        System.out.println("GPUPane Init done");
    }
}
