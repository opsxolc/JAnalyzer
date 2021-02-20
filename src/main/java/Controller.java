import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Duration;
import json.IntervalJson;
import json.ProcTimesJson;
import org.apache.commons.io.FileUtils;

import java.io.*;
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
    @FXML private TextArea statIntervalText;
    @FXML private Label statLabel;
    @FXML private TreeView<IntervalPane> statTreeView;
    @FXML private TreeView<IntervalComparePane> statCompareTreeView;
    @FXML private SplitPane compareSplitPane;
    @FXML private SplitPane statSplitPane;
    @FXML private TableView<StatRow> statTableView;
    @FXML private StackedBarChart statChart;
    @FXML private StackedBarChart statCompareChart;
    @FXML private LineChart<String, Double> statCompareGPUChart;
    @FXML private MenuButton sortMenu;
    @FXML private ChoiceBox<String> compareTypeChoiceBox;
    @FXML private ToggleButton showCompareTreeButton;
    @FXML private Button resetStatButton;
    @FXML private Button resetCompareStatButton;

    enum CompareType {
        lostTime,
        GPU
    }

    private double lostTime = 0;
    private double lostCompareTime = 0;
    private double GPUCompareTime = 0;
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    public static ArrayList<Stat> compareList = new ArrayList<>();
    public CompareType curCompareType = CompareType.lostTime;

    private static final String numProcSort = "Кол-во процессоров", lostTimeSort = "Потерянное время",
            execTimeSort = "Время выполнения", coefSort = "Коэф. эффективности";

    private void selectTab(int tabIndex){
        tabPane.getSelectionModel().select(tabIndex);
    }

    public void initController(){
        initStatTable();
        resetLoadedStat();
        resetCompareStat();
        initSortMenu();
        initCompareTypeChoiceBox();

//        //Create PopOver and add look and feel
//        PopOver popOver = new PopOver(new AnchorPane());
//
//        statLabel.setOnMouseEntered(mouseEvent -> {
//            //Show PopOver when mouse enters label
//            popOver.show(statLabel);
//        });
//
//        statLabel.setOnMouseExited(mouseEvent -> {
//            //Hide PopOver when mouse exits label
//            popOver.hide();
//        });
    }

    private void initCompareTypeChoiceBox(){
        compareTypeChoiceBox.getItems().clear();
        compareTypeChoiceBox.getItems().addAll("Потерянное время", "Сравнение GPU");
        compareTypeChoiceBox.getSelectionModel().select(0);
        compareTypeChoiceBox.setOnAction(event -> {
            switch (compareTypeChoiceBox.getSelectionModel().getSelectedItem()) {
                case "Потерянное время":
                    switchCompareType(CompareType.lostTime);
                    break;
                case "Сравнение GPU":
                    switchCompareType(CompareType.GPU);
            }
        });
    }

    private void resetCompareTypeChoiceBox() {
        compareTypeChoiceBox.getSelectionModel().select(0);
    }

    private void switchCompareType(CompareType cType){
        if (curCompareType == cType)
            return;
        switch (cType) {
            case lostTime:
                curCompareType = CompareType.lostTime;
                statCompareChart.setVisible(true);
                statCompareGPUChart.setVisible(false);
                break;
            case GPU:
                curCompareType = CompareType.GPU;
                statCompareChart.setVisible(false);
                statCompareGPUChart.setVisible(true);
                break;
        }
        TreeItem<IntervalComparePane> selectedItem = statCompareTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null)
            initCompareChart(selectedItem.getValue().getIntervals(), selectedItem.getValue().getPHeadings());
    }

    public void compareSort(String typeCompare){
        if (compareList.size() == 0)
            return;
        List<Interval> selectedIntervals = statCompareTreeView.getSelectionModel().getSelectedItem()
                .getValue().getIntervals();
        compareList = (ArrayList<Stat>) IntStream.range(0, selectedIntervals.size())
                .boxed().sorted((i, j) -> {
                    Interval o1 = selectedIntervals.get(i), o2 = selectedIntervals.get(j);
                    switch (typeCompare) {
                        case numProcSort:
                            if (o1.info.times.nproc - o2.info.times.nproc > 0) {
                                return 1;
                            }
                            if (o1.info.times.nproc - o2.info.times.nproc < 0) {
                                return -1;
                            }
                            return 0;
                        case lostTimeSort:
                            if (o1.info.times.lost_time - o2.info.times.lost_time > 0) {
                                return 1;
                            }
                            if (o1.info.times.lost_time - o2.info.times.lost_time < 0) {
                                return -1;
                            }
                            return 0;
                        case execTimeSort:
                            if (o1.info.times.exec_time - o2.info.times.exec_time > 0) {
                                return 1;
                            }
                            if (o1.info.times.exec_time - o2.info.times.exec_time < 0) {
                                return -1;
                            }
                            return 0;
                        case coefSort:
                            if (o1.info.times.efficiency - o2.info.times.efficiency > 0) {
                                return 1;
                            }
                            if (o1.info.times.efficiency - o2.info.times.efficiency < 0) {
                                return -1;
                            }
                            return 0;
                    }
                    return 0;
                })
                .map(compareList::get).collect(Collectors.toList());
        updateCompareRoot(statCompareTreeView.getRoot(),
                compareList.stream().map(elt -> elt.interval).collect(Collectors.toList()),
                compareList.stream().map(elt -> elt.info.p_heading).collect(Collectors.toList()));
        IntervalComparePane selectedItem = statCompareTreeView.getSelectionModel().getSelectedItem().getValue();
        initCompareChart(selectedItem.getIntervals(), selectedItem.getPHeadings());
    }

    private void initSortMenu(){
        sortMenu.getItems().clear();
        MenuItem numProc = new MenuItem(numProcSort);
        MenuItem lostTime = new MenuItem(lostTimeSort);
        MenuItem execTime = new MenuItem(execTimeSort);
        MenuItem coef = new MenuItem(coefSort);
        sortMenu.getItems().addAll(numProc, lostTime, execTime, coef);
        numProc.setOnAction(event -> compareSort(numProcSort));
        lostTime.setOnAction(event -> compareSort(lostTimeSort));
        execTime.setOnAction(event -> compareSort(execTimeSort));
        coef.setOnAction(event -> compareSort(coefSort));
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

    //-----  Recursive function to add blink for expanded items  -----//
    private void addBlink(TreeItem treeItem) {
        new Timeline(
                new KeyFrame(Duration.seconds(0),
                        new KeyValue(((AnchorPane)treeItem.getValue()).opacityProperty(), 1.0)),
                new KeyFrame(Duration.seconds(0.3),
                        new KeyValue(((AnchorPane)treeItem.getValue()).opacityProperty(), 0.3, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.seconds(0.7),
                        new KeyValue(((AnchorPane)treeItem.getValue()).opacityProperty(), 1.0, Interpolator.EASE_OUT))
        ).play();
        for (Object item : treeItem.getChildren()) {
            addBlink((TreeItem) item);
        }
    }

    //------  Recursive function to get the root for statTreeView  ------//
    private TreeItem<IntervalPane> getRootWithChildren(Interval interval) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        IntervalPane p = new IntervalPane(fxmlLoader.load(getClass().getResource("statTreeItem.fxml").openStream()));
        StatTreeItemController controller = fxmlLoader.getController();
        controller.init(interval.info.times.exec_time, interval.info.times.efficiency, interval.getType());
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

    //-----  Initializes compare chart based on active mode  -----//
    private void initCompareChart(List<Interval> intervals, List<String> p_headings){
        switch (curCompareType) {
            case lostTime:
                initCompareLostChart(intervals, p_headings);
                break;
            case GPU:
                initCompareGPUChart(intervals, p_headings);
                break;
        }
    }

    //-----  Initializes all compare charts  -----//
    private void initAllCompareCharts(List<Interval> intervals, List<String> p_headings){
        initCompareLostChart(intervals, p_headings);
        initCompareGPUChart(intervals, p_headings);
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

    //-----  Initializes line chart for GPU comparison selected interval  -----//
    private void initCompareGPUChart(List<Interval> intervals, List<String> p_headings){
        XYChart.Series<String, Double> seriesGPUProd = new XYChart.Series<>();
        XYChart.Series<String, Double> seriesGPULost = new XYChart.Series<>();
        XYChart.Series<String, Double> seriesExec = new XYChart.Series<>();
        seriesGPUProd.setName("Продуктивное время на GPU");
        seriesGPULost.setName("Потерянное время на GPU");
        seriesExec.setName("Время выполнения");

        //-----  Init series with GPU times data  -----//
        for (int i = 0; i < intervals.size(); ++i){
            String xname = "GPU Units: " + intervals.get(i).getGPUNum() + "\n"
                    + p_headings.get(i) + "\n"
                    + String.format("%.2f", intervals.get(i).info.times.exec_time) + "\n"
                    + String.format("%.2f", intervals.get(i).info.times.efficiency) + "\n"
                    + intervals.get(i).info.id.pname + " (" + i + ")";

            seriesGPUProd.getData().add(new XYChart.Data<>(xname, intervals.get(i).info.times.gpu_time_prod));
            seriesGPULost.getData().add(new XYChart.Data<>(xname, intervals.get(i).info.times.gpu_time_lost));
            seriesExec.getData().add(new XYChart.Data<>(xname, intervals.get(i).info.times.exec_time));
        }

        statCompareGPUChart.getYAxis().setAutoRanging(intervals.get(0).info.id.nlev == 0);
        statCompareGPUChart.getYAxis().setTickMarkVisible(true);
        statCompareGPUChart.getYAxis().setTickLabelsVisible(true);

        statCompareGPUChart.getData().clear();
        statCompareGPUChart.getData().addAll(seriesGPUProd, seriesGPULost, seriesExec);
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
        statSplitPane.setDividerPositions(statChart.getWidth() / statSplitPane.getWidth(), 1);

    }

    private void updateCompareRoot(TreeItem<IntervalComparePane> item,
                                   List<Interval> intervals, List<String> pHeadings){
        item.getValue().setIntervals(intervals);
        item.getValue().setPHeadings(pHeadings);
        for (int i = 0; i < item.getChildren().size(); ++i) {
            int finalI = i;
            List<Interval> subIntervals = intervals.stream().map(elt -> elt.intervals.get(finalI)).collect(Collectors.toList());
            updateCompareRoot(item.getChildren().get(i), subIntervals, pHeadings);
        }
    }

    private TreeItem<IntervalComparePane> getRootWithChildren(List<Interval> intervals,
                                                              List<String> pHeadings) {
        FXMLLoader fxmlLoader = new FXMLLoader();
        IntervalComparePane p;
        try {
             p = new IntervalComparePane(fxmlLoader.load(getClass().getResource("statCompareTreeItem1.fxml").openStream()));
        } catch (Exception e) {
            System.out.println("Error loading statCompareTreeItem1.fxml\n" + e.toString());
            return null;
        }
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
                intervals.get(max_time_index).info.times.exec_time, intervals.get(min_time_index).info.times.exec_time,
                intervals.get(0).getType());
        p.setStyle(intervals.get(max_time_index).getGradient(lostCompareTime));
        p.setIntervals(intervals);
        p.setPHeadings(pHeadings);
        TreeItem<IntervalComparePane> treeItem = new TreeItem<>(p);
        for (int i = 0; i < intervals.get(0).intervals.size(); ++i) {
            int finalI = i;
            List<Interval> subIntervals = intervals.stream().map(elt -> elt.intervals.get(finalI)).collect(Collectors.toList());
            treeItem.getChildren().add(getRootWithChildren(subIntervals, pHeadings));
        }
        treeItem.expandedProperty().addListener((observable, oldValue, newValue) -> addBlink(treeItem));
        treeItem.setExpanded(true);
        return treeItem;
    }

    private void initCompareIntervalTree(List<Stat> compareList) {
        //-----  Init tree  -----//
        lostCompareTime = Collections.max(compareList.stream().map(elt -> elt.interval.info.times.lost_time).collect(Collectors.toList()));
        GPUCompareTime = Collections.max(compareList.stream()
                .map(elt -> Math.max(Math.max(elt.interval.info.times.gpu_time_prod, elt.interval.info.times.gpu_time_lost),
                        elt.interval.info.times.exec_time)
        ).collect(Collectors.toList()));
        List<Interval> intervals = compareList.stream().map(elt -> elt.interval).collect(Collectors.toList());
        List<String> p_headings = compareList.stream().map(elt -> elt.info.p_heading).collect(Collectors.toList());
        TreeItem<IntervalComparePane> root = getRootWithChildren(intervals, p_headings);
        statCompareTreeView.setRoot(root);

        statCompareTreeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        statCompareTreeView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null)
                        initCompareChart(newValue.getValue().getIntervals(), newValue.getValue().getPHeadings());
                });
        statCompareTreeView.getSelectionModel().select(0);
        //-----  Init all charts to set max Y values  -----//
        initAllCompareCharts(intervals, p_headings);
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
        setDisableLoadedStat(false);
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
        initCompareIntervalTree(compareList);
        setDisableCompare(false);
        selectTab(2);
    }

    @FXML public void resetLoadedStat() {
        statLabel.setText("");
        statIntervalText.setText("");
        statChart.getData().clear();
        statTreeView.setRoot(null);
        statSplitPane.setDividerPositions(0, 1);
        setDisableLoadedStat(true);
        selectTab(0);
    }

    private void setDisableLoadedStat(boolean disable) {
        resetStatButton.setDisable(disable);
        statSplitPane.setDisable(disable);
    }

    @FXML public void resetCompareStat() {
        statCompareChart.getData().clear();
        statCompareTreeView.setRoot(null);
        compareSplitPane.setDividerPositions(1);
        showCompareTreeButton.setSelected(false);
        setDisableCompare(true);
        resetCompareTypeChoiceBox();
        selectTab(0);
    }

    private void setDisableCompare(boolean disable) {
        sortMenu.setDisable(disable);
        showCompareTreeButton.setDisable(disable);
        compareSplitPane.setDisable(disable);
        resetCompareStatButton.setDisable(disable);
        compareTypeChoiceBox.setDisable(disable);
    }

    @FXML public void compareShowIntervals() {
        if (showCompareTreeButton.isSelected()) {
            compareSplitPane.setDividerPositions((compareSplitPane.getWidth() - statCompareTreeView.getPrefWidth())
                    / compareSplitPane.getWidth());
        } else {
            statCompareTreeView.getSelectionModel().select(0);
            compareSplitPane.setDividerPositions(1);
        }
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
        List<StatRow> statRows = statTableView.getSelectionModel().getSelectedItems();
        for (StatRow statRow : statRows) {
            try {
                FileUtils.deleteDirectory(new File(statRow.getStat().dir));
            } catch (Exception e) {
                System.out.println("Error deleting dir " + statRow.getStat().dir);
                return;
            }
        }
        statTableView.getItems().removeAll(statRows);
    }

}
