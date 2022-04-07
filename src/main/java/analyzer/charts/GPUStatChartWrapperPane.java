package analyzer.charts;

import analyzer.stat.Interval;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class GPUStatChartWrapperPane extends Pane {
    GPUStatChart gpuStatChart;
    AnchorPane anchorPane;

    public GPUStatChartWrapperPane() {
        super();
        AnchorPane.setTopAnchor(this, .0);
        AnchorPane.setBottomAnchor(this, .0);
        AnchorPane.setLeftAnchor(this, .0);
        AnchorPane.setRightAnchor(this, .0);

        anchorPane = new AnchorPane();
        anchorPane.setPrefSize(AnchorPane.USE_COMPUTED_SIZE, AnchorPane.USE_COMPUTED_SIZE);
        getChildren().add(anchorPane);

        gpuStatChart = new GPUStatChart();
        anchorPane.getChildren().add(gpuStatChart);
        AnchorPane.setTopAnchor(gpuStatChart, .0);
        AnchorPane.setBottomAnchor(gpuStatChart, .0);
        AnchorPane.setLeftAnchor(gpuStatChart, .0);
        AnchorPane.setRightAnchor(gpuStatChart, .0);
    }

    public void displayData(Interval interval) {
        gpuStatChart.displayData(interval);
    }

}
