package analyzer.autoanalysis;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class AutoAnalysisController {

    @FXML TreeView<Node> resultTree;
    @FXML SplitPane splitPane;

    public void init() {
        resultTree.setShowRoot(false);
        resultTree.setRoot(new TreeItem<>());
        resultTree.getRoot().setExpanded(true);
        splitPane.setDividerPositions(1);
    }

    public void addResult(TreeItem<Node> result) {
        resultTree.getRoot().getChildren().add(result);
    }

}
