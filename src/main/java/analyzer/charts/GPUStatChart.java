package analyzer.charts;

import analyzer.json.GPUTimesJson;
import analyzer.json.ProcTimesJson;
import analyzer.stat.DVMHStatMetrics;
import analyzer.stat.Interval;
import javafx.scene.chart.*;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GPUStatChart extends LineChart {
//    BiConsumer<Boolean, Boolean> resetProc;
//    Consumer<Integer> selectProc;

    public GPUStatChart() {
        super(new CategoryAxis(), new NumberAxis());
//        this.resetProc = resetProc;
//        this.selectProc = selectProc;
        setTitle("Характеристики работы на GPU");
    }

    //-----  Displays Labels for Data in Chart  -----//
    private void displayLabelForData(XYChart.Data<String, Double> data) {
        StackPane bar = (StackPane) data.getNode();
        Text dataText = new Text(String.format("%.2f", data.getYValue()));
        dataText.setMouseTransparent(true);
        dataText.setPickOnBounds(false);
        dataText.setStyle("-fx-font-size: 10px;");
        bar.getChildren().add(dataText);
    }

    public void displayData(Interval interval) {
        XYChart.Series<Integer, Double> prod = new XYChart.Series<>();
        XYChart.Series<Integer, Double> lost = new XYChart.Series<>();
        XYChart.Series<Integer, Double> copy = new XYChart.Series<>();
        XYChart.Series<Integer, Double> cycles = new XYChart.Series<>();

        prod.setName("Полезное время");
        lost.setName("Потерянное время");
        copy.setName("Копирование");
        cycles.setName("Выполнение циклов");
        int i = 0;
        for (ProcTimesJson procTimes : interval.info.proc_times) {
            if (procTimes.gpu_times == null)
                continue;
            for (GPUTimesJson gpuTimes : procTimes.gpu_times) {
                prod.getData().add(new Data<>(i, gpuTimes.prod_time));
                lost.getData().add(new Data<>(i, gpuTimes.lost_time));
                copy.getData().add(new Data<>(i,
                        gpuTimes.metrics.get(DVMHStatMetrics.DVMH_STAT_METRIC_CPY_DTOD.ordinal()).sum +
                        gpuTimes.metrics.get(DVMHStatMetrics.DVMH_STAT_METRIC_CPY_DTOH.ordinal()).sum +
                        gpuTimes.metrics.get(DVMHStatMetrics.DVMH_STAT_METRIC_CPY_HTOD.ordinal()).sum +

                        gpuTimes.metrics.get(DVMHStatMetrics.DVMH_STAT_METRIC_CPY_SHADOW_DTOD.ordinal()).sum +
                        gpuTimes.metrics.get(DVMHStatMetrics.DVMH_STAT_METRIC_CPY_SHADOW_DTOH.ordinal()).sum +
                        gpuTimes.metrics.get(DVMHStatMetrics.DVMH_STAT_METRIC_CPY_SHADOW_HTOD.ordinal()).sum +

                        gpuTimes.metrics.get(DVMHStatMetrics.DVMH_STAT_METRIC_CPY_IN_REG_DTOD.ordinal()).sum +
                        gpuTimes.metrics.get(DVMHStatMetrics.DVMH_STAT_METRIC_CPY_IN_REG_DTOH.ordinal()).sum +
                        gpuTimes.metrics.get(DVMHStatMetrics.DVMH_STAT_METRIC_CPY_IN_REG_HTOD.ordinal()).sum +

                        gpuTimes.metrics.get(DVMHStatMetrics.DVMH_STAT_METRIC_CPY_REDIST_DTOD.ordinal()).sum +
                        gpuTimes.metrics.get(DVMHStatMetrics.DVMH_STAT_METRIC_CPY_REDIST_DTOH.ordinal()).sum +
                        gpuTimes.metrics.get(DVMHStatMetrics.DVMH_STAT_METRIC_CPY_REDIST_HTOD.ordinal()).sum +

                        gpuTimes.metrics.get(DVMHStatMetrics.DVMH_STAT_METRIC_CPY_REMOTE_DTOD.ordinal()).sum +
                        gpuTimes.metrics.get(DVMHStatMetrics.DVMH_STAT_METRIC_CPY_REMOTE_DTOH.ordinal()).sum +
                        gpuTimes.metrics.get(DVMHStatMetrics.DVMH_STAT_METRIC_CPY_REMOTE_HTOD.ordinal()).sum +

                        gpuTimes.metrics.get(DVMHStatMetrics.DVMH_STAT_METRIC_CPY_GET_ACTUAL.ordinal()).sum
                ));

                cycles.getData().add(new Data<>(i, gpuTimes.metrics.get(
                        DVMHStatMetrics.DVMH_STAT_METRIC_LOOP_PORTION_TIME.ordinal()).sum
                ));

                ++i;
            }
        }
    }

}
