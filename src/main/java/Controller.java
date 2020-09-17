import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import json.UseStatJson;

import java.io.IOException;

public class Controller {

    @FXML private TabPane tabPane;
    @FXML private Button loadStatButton;
    @FXML private Label label1;
    @FXML private TextField loadPath;
    @FXML private Label statLabel;
    @FXML private TreeView<Pane> statTreeView;

    private void selectTab(int tabIndex){
        tabPane.getSelectionModel().select(tabIndex);
    }

    private void addBlink(TreeItem<Pane> treeItem){
        new Timeline(
                new KeyFrame(Duration.seconds(0),
                        new KeyValue(treeItem.getValue().opacityProperty(), 1.0)),
                new KeyFrame(Duration.seconds(0.3),
                        new KeyValue(treeItem.getValue().opacityProperty(), 0.3, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.seconds(0.7),
                        new KeyValue(treeItem.getValue().opacityProperty(), 1.0, Interpolator.EASE_OUT))
        ).play();
        for (TreeItem<Pane> item: treeItem.getChildren()) {
            addBlink(item);
        }
    }

    private TreeItem<Pane> getRootWithChildren(Interval interval) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        Pane p = fxmlLoader.load(getClass().getResource("statTreeItem.fxml").openStream());
        StatTreeItemController controller = fxmlLoader.getController();
        controller.init(interval.info.times.exec_time, interval.info.times.efficiency);
        p.setStyle(interval.getGradient());
        TreeItem<Pane> treeItem = new TreeItem<>(p);
        for (Interval inter: interval.intervals) {
            treeItem.getChildren().add(getRootWithChildren(inter));
        }
        treeItem.expandedProperty().addListener((observable, oldValue, newValue) -> addBlink(treeItem));
        return treeItem;
    }

    private void initStat(@org.jetbrains.annotations.NotNull Stat stat) throws Exception{
        statLabel.setText(stat.getHeader());

        //-----  Init tree  -----//
        TreeItem<Pane> root = getRootWithChildren(stat.interval);
        statTreeView.setRoot(root);

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
