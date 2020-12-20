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
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Duration;
import jdk.nashorn.internal.runtime.regexp.joni.Regex;
import json.IntervalJson;
import json.ProcTimesJson;
import json.UseStatJson;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller {

    @FXML private TabPane tabPane;
    @FXML private TextField loadPath;
    @FXML private Label statLabel;
    @FXML private TreeView<IntervalPane> statTreeView;
    @FXML private TreeView<IntervalComparePane> statCompareTreeView;
    @FXML private SplitPane compareSplitPane;
    @FXML private TableView<StatRow> statTableView;
    @FXML private StackedBarChart statChart;
    @FXML private StackedBarChart statCompareChart;
    private double lostTime = 0;
    private double lostCompareTime = 0;
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    public static ArrayList<Stat> compareList = new ArrayList<>();

    private void selectTab(int tabIndex){
        tabPane.getSelectionModel().select(tabIndex);
    }

    public void initController(){
        initStatTable();
    }

    private void initStatTable(){
        statTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        for (Object o : statTableView.getColumns()){
            TableColumn<StatRow, String> tc = (TableColumn<StatRow, String>)o;
            switch (tc.getText()) {
                case "Статистика выполнения":
                    tc.setCellValueFactory(new PropertyValueFactory<>("statInfo"));
                    break;
                case "Дата загрузки":
                    tc.setCellValueFactory(new PropertyValueFactory<>("creationTime"));
                    break;
            }
        }
        LoadStatList();
    }

    //-----  Recursive function to add blink for expanded items in statTreeView  -----//
    private void addBlink(TreeItem<IntervalPane> treeItem) {
        new Timeline(
                new KeyFrame(Duration.seconds(0),
                        new KeyValue(treeItem.getValue().opacityProperty(), 1.0)),
                new KeyFrame(Duration.seconds(0.3),
                        new KeyValue(treeItem.getValue().opacityProperty(), 0.3, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.seconds(0.7),
                        new KeyValue(treeItem.getValue().opacityProperty(), 1.0, Interpolator.EASE_OUT))
        ).play();
        for (TreeItem<IntervalPane> item : treeItem.getChildren()) {
            addBlink(item);
        }
    }

    //-----  Recursive function to add blink for expanded items in statCompareTreeView  -----//
    private void addBlinkCompare(TreeItem<IntervalComparePane> treeItem) {
        new Timeline(
                new KeyFrame(Duration.seconds(0),
                        new KeyValue(treeItem.getValue().opacityProperty(), 1.0)),
                new KeyFrame(Duration.seconds(0.3),
                        new KeyValue(treeItem.getValue().opacityProperty(), 0.3, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.seconds(0.7),
                        new KeyValue(treeItem.getValue().opacityProperty(), 1.0, Interpolator.EASE_OUT))
        ).play();
        for (TreeItem<IntervalComparePane> item : treeItem.getChildren()) {
            addBlinkCompare(item);
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


        statChart.getYAxis().setAutoRanging(interval.info.id.nlev == 0);
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

    //-----  Initializes stacked bar chart for comparison selected interval  -----//
    private void initCompareLostChart(List<Interval> intervals, List<String> p_headings){
        XYChart.Series<String, Double> series1 = new XYChart.Series<>();
        XYChart.Series<String, Double> series2 = new XYChart.Series<>();
        XYChart.Series<String, Double> series3 = new XYChart.Series<>();
        XYChart.Series<String, Double> series4 = new XYChart.Series<>();
        series1.setName("Недостаточный параллелизм (sys)");
        series2.setName("Недостаточный параллелизм (user)");
        series3.setName("Простои");
        series4.setName("Коммуникации");

        //-----  Init lost time data  ------//
        for (int i = 0; i < intervals.size(); ++i){
            String xname = p_headings.get(i) + "\n"
                    + String.format("%.2f", intervals.get(i).info.times.exec_time) + "\n"
                    + String.format("%.2f", intervals.get(i).info.times.efficiency) + "\n"
                    + intervals.get(i).info.id.pname + " (" + i + ")";
            series1.getData().add(new XYChart.Data<>(xname, intervals.get(i).info.times.insuf_sys));
            series2.getData().add(new XYChart.Data<>(xname, intervals.get(i).info.times.insuf_user));
            series3.getData().add(new XYChart.Data<>(xname, intervals.get(i).info.times.idle));
            series4.getData().add(new XYChart.Data<>(xname, intervals.get(i).info.times.comm));
        }

        statCompareChart.getYAxis().setAutoRanging(intervals.get(0).info.id.nlev == 0);
        statCompareChart.getYAxis().setTickMarkVisible(true);
        statCompareChart.getYAxis().setTickLabelsVisible(true);

        statCompareChart.getData().clear();
        statCompareChart.getData().addAll(series1, series2, series3, series4);

        //-----  Display labels  -----//
        for (int i = 0; i < intervals.size(); ++i){
            displayLabelForData(series1.getData().get(i));
            displayLabelForData(series2.getData().get(i));
            displayLabelForData(series3.getData().get(i));
            displayLabelForData(series4.getData().get(i));
        }
        statCompareChart.setCategoryGap(20);
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
                    if (newValue != null)
                        initStatChart(newValue.getValue().getInterval());
        });

        statTreeView.getSelectionModel().select(0);
    }

    private TreeItem<IntervalComparePane> getRootWithChildren(List<Interval> intervals,
                                                              List<String> pHeadings) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader();
        IntervalComparePane p = new IntervalComparePane(fxmlLoader.load(getClass().getResource("statCompareTreeItem1.fxml").openStream()));
        StatCompareTreeItem1Controller controller = fxmlLoader.getController();
        int max_time_index = 0;
        int min_time_index = 0;
        for (int i = 0; i < intervals.size(); ++i) {
            if (intervals.get(max_time_index).info.times.exec_time < intervals.get(i).info.times.exec_time) {
                max_time_index = i;
            } else {
                min_time_index = i;
            }
        }
        controller.init(pHeadings.get(max_time_index), pHeadings.get(min_time_index),
                intervals.get(max_time_index).info.times.exec_time, intervals.get(min_time_index).info.times.exec_time);
        //TODO: определять тип интервала и показывать слева значение
        p.setStyle(intervals.get(max_time_index).getGradient(lostCompareTime));
        p.setIntervals(intervals);
        p.setPHeadings(pHeadings);
        TreeItem<IntervalComparePane> treeItem = new TreeItem<>(p);
        for (int i = 0; i < intervals.get(0).intervals.size(); ++i) {
            int finalI = i;
            List<Interval> subIntervals = intervals.stream().map(elt -> elt.intervals.get(finalI)).collect(Collectors.toList());
            treeItem.getChildren().add(getRootWithChildren(subIntervals, pHeadings));
        }
        treeItem.expandedProperty().addListener((observable, oldValue, newValue) -> addBlinkCompare(treeItem));
        treeItem.setExpanded(true);
        return treeItem;
    }

    private void initCompareIntervalTree(List<Stat> compareList) throws Exception{

        //-----  Init tree  -----//
        lostCompareTime = Collections.max(compareList.stream().map(elt -> elt.interval.info.times.lost_time).collect(Collectors.toList()));
        TreeItem<IntervalComparePane> root = getRootWithChildren(compareList.stream().map(elt -> elt.interval).collect(Collectors.toList()),
                compareList.stream().map(elt -> elt.info.p_heading).collect(Collectors.toList()));
        statCompareTreeView.setRoot(root);

        statCompareTreeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        statCompareTreeView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null)
                        initCompareLostChart(newValue.getValue().getIntervals(), newValue.getValue().getPHeadings());
                });
        initCompareLostChart(compareList.stream().map(elt -> elt.interval).collect(Collectors.toList()),
                compareList.stream().map(elt -> elt.info.p_heading).collect(Collectors.toList()));
        statTreeView.getSelectionModel().select(0);
    }

    @FXML public void loadStat() throws Exception{
        int size = statTableView.getSelectionModel().getSelectedItems().size();
        if (size != 1){
            ErrorDialog errorDialog = new ErrorDialog("Пожалуйста, выберите одну статистику для загрузки.");
            errorDialog.showDialog();
            return;
        }
        StatRow statRow = (StatRow) statTableView.getSelectionModel().getSelectedItem();
        initStat(statRow.getStat());
        selectTab(1);
    }

    // Добавить статистику в StatTableView
    private void AddStatToList(Stat stat, String creationTime)
    {
        statTableView.getItems().add(new StatRow(stat.getHeader(), creationTime, stat));
    }

    public void LoadStatList()
    {
        statTableView.getItems().clear();
        File statDir = new File(Main.StatDirPath);
        File[] dirs = statDir.listFiles((current, name) ->
                new File(current, name).isDirectory() && Pattern.compile("-?[0-9]*$").matcher(name).matches());
        File file;
        String creationTime;
        if (dirs == null)
            return;
        for (File dir : dirs)
        {
            try {
                File[] files = dir.listFiles((current, name) -> Pattern.compile(".*json$").matcher(name).matches());
                if (files == null || files.length == 0)
                    throw new Exception("No json stat file in directory " + dir.getName());
                file = files[0];
                long cTime = ((FileTime) Files.getAttribute(file.toPath(), "creationTime")).toMillis();
                ZonedDateTime t = Instant.ofEpochMilli(cTime).atZone(ZoneId.systemDefault());
                creationTime = dtf.format(t);
                String json = Main.readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
                Stat stat = new Stat(json, dir.getAbsolutePath(), false);
                AddStatToList(stat, creationTime);
            } catch (Exception e){
                System.out.println("Error occurred loading dir " + dir);
                System.out.println(e.toString());
            }

        }
    }

    @FXML public void compareStats(){
        ObservableList<StatRow> selected = statTableView.getSelectionModel().getSelectedItems();
        if (selected.size() <= 1) {
            ErrorDialog errorDialog = new ErrorDialog("Пожалуйста, выберите две или более статистики для сравнения.");
            errorDialog.showDialog();
            return;
        }
        compareList = (ArrayList<Stat>) selected.stream().map(elt -> elt.getStat().clone()).collect(Collectors.toList());
        Stat.intersectStats(compareList);
        try {
            initCompareIntervalTree(compareList);
        } catch (Exception e) {
            System.out.println(e.toString());
            return;
        }
        selectTab(2);
    }

    @FXML public void saveStat() throws Exception{
        Stat stat;
        String res = LibraryImport.readStat(loadPath.getText());
        if (res == null) {
            ErrorDialog errorDialog = new ErrorDialog("Не найден файл \"" + loadPath.getText()
                    + "\".");
            errorDialog.showDialog();
            return;
        }
        // Директория с загружаемым файлом
        String tmpFileLocDir = new File(loadPath.getText()).getParentFile().getAbsolutePath();
        stat = new Stat(res, tmpFileLocDir, false);
        System.out.println(tmpFileLocDir);
        //---  Save files  ---//
        String tmpDirPath = Main.StatDirPath + stat.hashCode();
        Files.createDirectory(Paths.get(tmpDirPath));
        LocalDateTime creationTime = LocalDateTime.now();
        FileWriter writer = new FileWriter(tmpDirPath + "/stat.json");
        writer.write(res);
        writer.close();

        for (IntervalJson inter : stat.info.inter)
        {
            if (!Files.exists(Paths.get(tmpDirPath + '/' + inter.id.pname)))
                try
                {
                    if (Files.exists(Paths.get(tmpFileLocDir + '/' + inter.id.pname)))
                        Files.copy(Paths.get(tmpFileLocDir + '/' + inter.id.pname),
                                Paths.get(tmpDirPath + '/' + inter.id.pname));
                }
                catch (Exception e)
                {
                    System.out.println("Could not copy file '" + inter.id.pname + "'");
                }
        }
        AddStatToList(stat, dtf.format(creationTime));
    }

    @FXML public void deleteStat(){
        int size = statTableView.getSelectionModel().getSelectedItems().size();
        if (size == 0){
            ErrorDialog errorDialog = new ErrorDialog("Пожалуйста, выберите статистики для удаления.");
            errorDialog.showDialog();
            return;
        }
        // TODO: delete stats
//        StatRow statRow = (StatRow) statTableView.getSelectionModel().getSelectedItem();
    }

}
