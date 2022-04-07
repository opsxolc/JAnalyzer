package analyzer.charts;

import analyzer.stat.Interval;
import javafx.scene.chart.*;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GPUStatChart extends LineChart {
    BiConsumer<Boolean, Boolean> resetProc;
    Consumer<Integer> selectProc;

    public GPUStatChart(
            BiConsumer<Boolean, Boolean> resetProc,
            Consumer<Integer> selectProc
    ) {
        super(new CategoryAxis(), new NumberAxis());
        this.resetProc = resetProc;
        this.selectProc = selectProc;
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

    }

}
