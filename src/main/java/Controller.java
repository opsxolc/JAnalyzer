import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import json.GPUTimesJson;
import json.IntervalJson;
import json.ProcTimesJson;
import org.apache.commons.io.FileUtils;
import org.controlsfx.control.PopOver;

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
    @FXML private VBox GPUVBox;
    @FXML private SplitPane GPUSplitPane;
    @FXML private ScrollPane GPUScrollPane;
    @FXML private ToggleButton procAnalysisButton;
    @FXML private TreeView<CharacteristicPane> statAnalysisView;
    @FXML private Label characteristicLabel;

    enum CompareType {
        lostTime,
        GPU
    }

    enum LostTimeType {
        insufSys,
        insufUser,
        idle,
        comm
    }

    Pattern pData = Pattern.compile("data.");
    Pattern pDataPart = Pattern.compile("default-color.");

    private double lostTime = 0;
    private double lostCompareTime = 0;
//    private double GPUCompareTime = 0;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    public static ArrayList<Stat> compareList = new ArrayList<>();
    public CompareType curCompareType = CompareType.lostTime;
    public Integer curProc = -1;

    private static final String
            numProcSort = "Кол-во процессоров",
            lostTimeSort = "Потерянное время",
            execTimeSort = "Время выполнения",
            coefSort = "Коэф. эффективности";

    private static final String
            lostTimeChartType = "Потерянное время",
            GPUChartType = "Сравнение GPU";

    private void selectTab(int tabIndex){
        tabPane.getSelectionModel().select(tabIndex);
    }

    //-----  INIT SECTION  -----//

    public void initController(){
        initStatTable();
        resetLoadedStat();
        resetCompareStat();
        initSortMenu();
        initCompareTypeChoiceBox();
        initStatAnalysisTable();
    }

    private void initCompareTypeChoiceBox(){
        compareTypeChoiceBox.getItems().clear();
        compareTypeChoiceBox.getItems().addAll(lostTimeChartType, GPUChartType);
        compareTypeChoiceBox.getSelectionModel().select(0);
        compareTypeChoiceBox.setOnAction(event -> {
            switch (compareTypeChoiceBox.getSelectionModel().getSelectedItem()) {
                case lostTimeChartType:
                    switchCompareType(CompareType.lostTime);
                    break;
                case GPUChartType:
                    switchCompareType(CompareType.GPU);
            }
        });
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

    private void initStatAnalysisTable(){
        statAnalysisView.setShowRoot(false);

        TreeItem<CharacteristicPane> root = new TreeItem<>(new CharacteristicPane("root", "root"));
        statAnalysisView.setRoot(root);
    }

    //-----  INIT SECTION END  -----//

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

    //-----  Recursive function to add blink for expanded items  -----//
    private void addBlink(TreeItem treeItem) {
        new Timeline(
                new KeyFrame(Duration.seconds(0),
                        new KeyValue(((Node)treeItem.getValue()).opacityProperty(), 1.0)),
                new KeyFrame(Duration.seconds(0.3),
                        new KeyValue(((Node)treeItem.getValue()).opacityProperty(), 0.3, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.seconds(0.7),
                        new KeyValue(((Node)treeItem.getValue()).opacityProperty(), 1.0, Interpolator.EASE_OUT))
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

    //-----  Displays Labels for Data in Chart  -----//
    private void displayLabelForData(XYChart.Data<String, Double> data) {
        StackPane bar = (StackPane) data.getNode();
        Text dataText = new Text(String.format("%.2f", data.getYValue()));
        dataText.setMouseTransparent(true);
        dataText.setPickOnBounds(false);
        dataText.setStyle("-fx-font-size: 10px;");
        bar.getChildren().add(dataText);
    }

    private void addGPUCards(List<GPUTimesJson> GPUs) {
        for (int i = 0; i < GPUs.size(); i++) {
            GPUPane gpuCard = new GPUPane();
            gpuCard.Init(GPUs.get(i), i + 1);
            GPUVBox.getChildren().add(gpuCard);
        }
    }

    //-----  Find data number from css list  -----//
    private Integer findDataNum(List<String> styleList){
        // TODO: find also for axes
        for(String s: styleList){
            if (pData.matcher(s).matches()) {
                return Integer.decode(s.replaceAll("[^0-9]", ""));
            }
        }
        return null;
    }

    private Integer findDataPartNum(List<String> styleList){
        for(String s: styleList){
            if (pDataPart.matcher(s).matches()) {
                return Integer.decode(s.replaceAll("[^0-9]", ""));
            }
        }
        return null;
    }

    //-----  Select Processor from Stat  -----//
    private void selectProc(int procNum){
        Interval interval = getSelectedIntervalStat();
        if (procNum < 0 || interval == null
                || interval.info.proc_times.get(procNum).gpu_times == null
                || interval.info.proc_times.get(procNum).gpu_times.size() == 0) {
            resetProc(false, false);
            return;
        }

        resetProc(false, true);

        //-----  Set Divider Position  -----//
        if (GPUSplitPane.getDividerPositions()[0] > 0.98)
            GPUSplitPane.setDividerPosition(0, 0.66);

        //-----  Adjust chart style  -----//
        HashSet<Node> data = new HashSet<>(statChart.lookupAll(".chart-bar"));
        String dataName = ".data" + procNum;
        data.removeAll(statChart.lookupAll(dataName));
        for(Node n: data) {
            if (n.getStyleClass().contains("default-color0")) {
                n.setStyle("-fx-background-color: #f8e7e8;");
            }
            if (n.getStyleClass().contains("default-color1")) {
                n.setStyle("-fx-background-color: #d9a1d8;");
            }
            if (n.getStyleClass().contains("default-color2")) {
                n.setStyle("-fx-background-color: #c1dff6;");
            }
            if (n.getStyleClass().contains("default-color3")) {
                n.setStyle("-fx-background-color: #d5f5aa;");
            }
        }

        //-----  Adjust Scroll  -----//
//        GPUScrollPane.setVvalue(0);

        //-----  Add GPU cards  -----//
        addGPUCards(interval.info.proc_times.get(procNum).gpu_times);

        curProc = procNum;
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

        statChart.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                if(mouseEvent.getClickCount() == 2){
                    Node node = mouseEvent.getPickResult().getIntersectedNode();
                    Integer dataNum = findDataNum(node.getStyleClass());
                    if (dataNum == null) {
                        resetProc(true, true);
                        return;
                    }
                    selectProc(dataNum);
                }
            }
        });
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

        statCompareChart.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                if (mouseEvent.getClickCount() == 1) {
                    Node node = mouseEvent.getPickResult().getIntersectedNode();
                    Integer dataNum = findDataNum(node.getStyleClass());
                    Integer dataPartNum = findDataPartNum(node.getStyleClass());
                    if (dataNum == null || getSelectedIntervalsCompare() == null)
                        return;
                    Interval inter = getSelectedIntervalsCompare().get(dataNum);
                    PopOver popOver = new PopOver(new InsufPromt(inter));
                    popOver.show(node);
                    System.out.println(dataNum + " " + dataPartNum);
                }
            }
        });
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

    //-----  Analysis  -----//

    @FXML public void procAnalysis() {
        if (procAnalysisButton.isSelected()) {
            enableProcAnalysis();
        } else {
            enableAnalysis();
        }
    }

    private void enableProcAnalysis() {
        Interval inter = getSelectedIntervalStat();
        hideAnalysis();
        showProcAnalysis();
        if (inter == null) {
            return;
        }
        initStatChart(inter);
        selectProc(curProc);
    }

    private void showAnalysis(){
        characteristicLabel.setVisible(true);
        statAnalysisView.setVisible(true);
    }

    private void hideAnalysis(){
        characteristicLabel.setVisible(false);
        statAnalysisView.setVisible(false);
    }

    private void showProcAnalysis(){
        GPUSplitPane.setVisible(true);
    }

    private void hideProcAnalysis(){
        GPUSplitPane.setVisible(false);
    }

    private void enableAnalysis(){
        Interval inter = getSelectedIntervalStat();
        hideProcAnalysis();
        showAnalysis();
        if (inter == null) {
            return;
        }
        initAnalysis(inter);
    }

    private double getMaxWidth(List<TreeItem<CharacteristicPane>> list, int level, double max){
        for (TreeItem<CharacteristicPane> item: list) {
            double width = item.getValue().getBoundsInParent().getWidth();
            if (18 * level + width > max) {
                max = 18 * level + width;
            }
            double newMax = getMaxWidth(item.getChildren(), level + 1, max);
            if (newMax > max)
                max = newMax;
        }
        return max;
    }

    private void initAnalysis(Interval inter){
        CharacteristicPane efficiency = new CharacteristicPane("Parallelization efficiency", inter.info.times.efficiency),
            execTime = new CharacteristicPane("Execution time", inter.info.times.exec_time),
            processors = new CharacteristicPane("Processors", inter.info.times.nproc),
            threads = new CharacteristicPane("Threads amount", inter.info.times.threadsOfAllProcs),
            totalTime = new CharacteristicPane("Total time", inter.info.times.sys_time),
            prodTime = new CharacteristicPane("Productive time", inter.info.times.prod),
            lostTime = new CharacteristicPane("Lost time", inter.info.times.lost_time),
                insufParallelism = new CharacteristicPane("Insufficient parallelism", inter.info.times.insuf),
                    insufParallelismSys = new CharacteristicPane("Sys", inter.info.times.insuf_sys),
                    insufParallelismUser = new CharacteristicPane("User", inter.info.times.insuf_user),
                comm = new CharacteristicPane("Communication", inter.info.times.comm),
                idleTime = new CharacteristicPane("Idle time", inter.info.times.idle),
            loadImbalance = new CharacteristicPane("Load imbalance", inter.info.times.load_imb);

        TreeItem<CharacteristicPane> efficiencyItem = new TreeItem<>(efficiency),
                execTimeItem = new TreeItem<>(execTime),
                processorsItem = new TreeItem<>(processors),
                threadsItem = new TreeItem<>(threads),
                totalTimeItem = new TreeItem<>(totalTime),
                prodTimeItem = new TreeItem<>(prodTime),
                lostTimeItem = new TreeItem<>(lostTime),
                    insufParallelismItem = new TreeItem<>(insufParallelism),
                        insufParallelismSysItem = new TreeItem<>(insufParallelismSys),
                        insufParallelismUserItem = new TreeItem<>(insufParallelismUser),
                    commItem = new TreeItem<>(comm),
                    idleTimeItem = new TreeItem<>(idleTime),
                loadImbalanceItem = new TreeItem<>(loadImbalance);

        commItem.getValue().getStyleClass().add("comm");
        insufParallelismItem.getValue().getStyleClass().add("insuf");
        insufParallelismSysItem.getValue().getStyleClass().add("insuf-sys");
        insufParallelismUserItem.getValue().getStyleClass().add("insuf-user");
        idleTimeItem.getValue().getStyleClass().add("idle");

        insufParallelismItem.getChildren().addAll(insufParallelismSysItem, insufParallelismUserItem);
        lostTimeItem.getChildren().addAll(insufParallelismItem, commItem, idleTimeItem);

        statAnalysisView.getRoot().getChildren().clear();
        statAnalysisView.getRoot().getChildren().addAll(efficiencyItem, execTimeItem, processorsItem,
                threadsItem, totalTimeItem, prodTimeItem, lostTimeItem, loadImbalanceItem);

//        System.out.println();


        insufParallelismItem.expandedProperty().addListener((observable, oldValue, newValue) -> addBlink(insufParallelismItem));
        lostTimeItem.expandedProperty().addListener((observable, oldValue, newValue) -> addBlink(lostTimeItem));
    }

    //-----  Analysis END  -----//

    private void initStat(@org.jetbrains.annotations.NotNull Stat stat) throws Exception{
        resetProc(true, true);

        statLabel.setText(stat.getHeader());

        //-----  Init tree  -----//
        lostTime = stat.interval.info.times.lost_time;
        TreeItem<IntervalPane> root = getRootWithChildren(stat.interval);
        statTreeView.setRoot(root);

        statTreeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        statTreeView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        if (procAnalysisButton.isSelected()) {
                            initStatChart(newValue.getValue().getInterval());
                            selectProc(curProc);
                        } else {
                            initAnalysis(newValue.getValue().getInterval());
                        }
                    }
        });

        //-----  Adjust style  -----//
        statTreeView.getSelectionModel().select(0);
        statSplitPane.setDividerPositions(statChart.getWidth() / statSplitPane.getWidth(), 1);
        SplitPane.setResizableWithParent(statSplitPane.getItems().get(0), true);
        SplitPane.setResizableWithParent(statSplitPane.getItems().get(1), true);
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
        p.setStyle(Interval.getCompareGradient(intervals, lostCompareTime));
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
//        GPUCompareTime = Collections.max(compareList.stream()
//                .map(elt -> Math.max(Math.max(elt.interval.info.times.gpu_time_prod, elt.interval.info.times.gpu_time_lost),
//                        elt.interval.info.times.exec_time)
//        ).collect(Collectors.toList()));
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

    //-----  Get current selected Interval for loaded Stat  -----//
    public Interval getSelectedIntervalStat(){
        List<TreeItem<IntervalPane>> selectedItems =
                statTreeView.getSelectionModel().getSelectedItems();
        if (selectedItems.size() == 1)
                return selectedItems.get(0).getValue().getInterval();
        return null;
    }

    //-----  Get current selected Interval for compare stats  -----//
    public List<Interval> getSelectedIntervalsCompare(){
        List<TreeItem<IntervalComparePane>> selectedItems =
                statCompareTreeView.getSelectionModel().getSelectedItems();
        if (selectedItems.size() == 1)
            return selectedItems.get(0).getValue().getIntervals();
        return null;
    }

    //-----  Analyze chosen stat  -----//
    @FXML public void loadStat() throws Exception{
        int size = statTableView.getSelectionModel().getSelectedItems().size();
        if (size != 1){
            ErrorDialog errorDialog = new ErrorDialog("Пожалуйста, выберите одну статистику для загрузки.");
            errorDialog.showDialog();
            return;
        }
        initStat(statTableView.getSelectionModel().getSelectedItem().getStat());
        setDisableLoadedStat(false);
        selectTab(1);
    }

    //-----  Add Stat to StatTableView  -----//
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

    //------------  RESET SECTION  ------------//

    private void resetCompareTypeChoiceBox() {
        compareTypeChoiceBox.getSelectionModel().select(0);
    }

    //-----  Reset selection of Processor  -----//
    private void resetProc(boolean resetDivider, boolean resetCurProc){
        if (resetCurProc)
            curProc = -1;
        for(Node n: statChart.lookupAll(".chart-bar"))
            n.setStyle(null);
        GPUVBox.getChildren().clear();
        if (resetDivider)
            GPUSplitPane.setDividerPositions(1);
    }

    @FXML public void resetLoadedStat() {
        SplitPane.setResizableWithParent(statSplitPane.getItems().get(0), false);
        SplitPane.setResizableWithParent(statSplitPane.getItems().get(1), true);
        SplitPane.setResizableWithParent(statSplitPane.getItems().get(2), false);
        SplitPane.setResizableWithParent(GPUSplitPane.getItems().get(1), false);
        resetProc(true, true);
        statLabel.setText("");
        statIntervalText.setText("");
        statChart.getData().clear();
        statTreeView.setRoot(null);
        statSplitPane.setDividerPositions(0, 1);
        setDisableLoadedStat(true);
        selectTab(0);
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

    //------------   RESET SECTION END   ------------//

    //------------    DISABLE SECTION    ------------//

    private void setDisableLoadedStat(boolean disable) {
        resetStatButton.setDisable(disable);
        statSplitPane.setDisable(disable);
    }

    private void setDisableCompare(boolean disable) {
        sortMenu.setDisable(disable);
        showCompareTreeButton.setDisable(disable);
        compareSplitPane.setDisable(disable);
        resetCompareStatButton.setDisable(disable);
        compareTypeChoiceBox.setDisable(disable);
    }

    //------------  DISABLE SECTION END  ------------//

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
        String tmpFileLocDir = new File(loadPath.getText()).getParentFile().getAbsolutePath();
        stat = new Stat(res, tmpFileLocDir, false);

        //---  Save files  ---//
        String tmpDirPath = Main.StatDirPath + stat.hashCode();
        stat.dir = tmpDirPath;
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
