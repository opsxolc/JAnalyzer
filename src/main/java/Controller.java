import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.jmx.MXNodeAlgorithm;
import com.sun.javafx.jmx.MXNodeAlgorithmContext;
import com.sun.javafx.sg.prism.NGNode;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Duration;
import json.ProcTimesJson;
import json.UseStatJson;

import java.io.IOException;
import java.util.List;

public class Controller {

    @FXML private TabPane tabPane;
    @FXML private Button loadStatButton;
    @FXML private Label label1;
    @FXML private TextField loadPath;
    @FXML private Label statLabel;
    @FXML private TreeView<IntervalPane> statTreeView;
    @FXML private TableView statTableView;
    @FXML private StackedBarChart statChart;
    private double lostTime = 0;

    private void selectTab(int tabIndex){
        tabPane.getSelectionModel().select(tabIndex);
    }

    //-----  Recursive function to add blink for expanded items in statTreeView  -----//
    private void addBlink(TreeItem<IntervalPane> treeItem){
        new Timeline(
                new KeyFrame(Duration.seconds(0),
                        new KeyValue(treeItem.getValue().opacityProperty(), 1.0)),
                new KeyFrame(Duration.seconds(0.3),
                        new KeyValue(treeItem.getValue().opacityProperty(), 0.3, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.seconds(0.7),
                        new KeyValue(treeItem.getValue().opacityProperty(), 1.0, Interpolator.EASE_OUT))
        ).play();
        for (TreeItem<IntervalPane> item: treeItem.getChildren()) {
            addBlink(item);
        }
    }

    //------  Recursive function to get the root for statTreeView  ------//
    private TreeItem<IntervalPane> getRootWithChildren(Interval interval) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        IntervalPane p = new IntervalPane(fxmlLoader.load(getClass().getResource("statTreeItem.fxml").openStream()));
        StatTreeItemController controller = fxmlLoader.getController();
        controller.init(interval.info.times.exec_time, interval.info.times.efficiency);
        //TODO: определять тип интервала и показывать слева значение
        p.setStyle(interval.getGradient(lostTime));
        p.setInterval(interval);
        TreeItem<IntervalPane> treeItem = new TreeItem<>(p);
        for (Interval inter: interval.intervals) {
            treeItem.getChildren().add(getRootWithChildren(inter));
        }
        treeItem.expandedProperty().addListener((observable, oldValue, newValue) -> addBlink(treeItem));
        treeItem.setExpanded(true);
        return treeItem;
    }

    private void displayLabelForData(XYChart.Data<String, Double> data) {
        StackPane bar = (StackPane) data.getNode();
        final Text dataText = new Text(String.format("%.2f", data.getYValue()));
        bar.getChildren().add(dataText);
    }

    //-----  Initializes stacked bar chart for selected interval  -----//
    private void initStatChart(Interval interval){
        XYChart.Series<String, Double> series1 = new XYChart.Series<>();
        XYChart.Series<String, Double> series2 = new XYChart.Series<>();
        XYChart.Series<String, Double> series3 = new XYChart.Series<>();
        XYChart.Series<String, Double> series4 = new XYChart.Series<>();
        series1.setName("Недостаточный параллелизм (sys)");
        series2.setName("Недостаточный параллелизм (user)");
        series3.setName("Простои");
        series4.setName("Коммуникации");

        List<ProcTimesJson> procTimes = interval.info.proc_times;

        //-----  Init proc data  ------//
        for (int i = 0; i < procTimes.size(); ++i){
            series1.getData().add(new XYChart.Data<>(Integer.toString(i + 1), procTimes.get(i).insuf_sys));
            series2.getData().add(new XYChart.Data<>(Integer.toString(i + 1), procTimes.get(i).insuf_user));
            series3.getData().add(new XYChart.Data<>(Integer.toString(i + 1), procTimes.get(i).idle));
            series4.getData().add(new XYChart.Data<>(Integer.toString(i + 1), procTimes.get(i).comm));
        }


        if (interval.info.id.nlev == 0)
            statChart.getYAxis().setAutoRanging(true);
        else
            statChart.getYAxis().setAutoRanging(false);
        statChart.getYAxis().setTickMarkVisible(true);
        statChart.getYAxis().setTickLabelsVisible(true);

        statChart.getData().clear();
        statChart.getData().addAll(series1, series2, series3, series4);

        //-----  Display labels  -----//
        for (int i = 0; i < procTimes.size(); ++i){
            displayLabelForData(series1.getData().get(i));
            displayLabelForData(series2.getData().get(i));
            displayLabelForData(series3.getData().get(i));
            displayLabelForData(series4.getData().get(i));
        }
        statChart.setCategoryGap(20);
    }

    private void initStat(@org.jetbrains.annotations.NotNull Stat stat) throws Exception{
        statLabel.setText(stat.getHeader());

        //-----  Init tree  -----//
        lostTime = stat.interval.info.times.lost_time;
        TreeItem<IntervalPane> root = getRootWithChildren(stat.interval);
        statTreeView.setRoot(root);

        statTreeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        statTreeView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    initStatChart(newValue.getValue().getInterval());
        });

        statTreeView.getSelectionModel().select(0);


    }

    @FXML public void LoadStat() throws Exception{
        String stat = LibraryImport.readStat(loadPath.getText());
        if (stat == null) {
            ErrorDialog errorDialog = new ErrorDialog("Не найден файл \"" + loadPath.getText()
                + "\".");
            errorDialog.showDialog();
            return;
        }
        initStat(new Stat(UseStatJson.GetStat(stat), "", false));
        label1.setText("Loaded!");
        selectTab(1);
    }

}
