package analyzer.autoanalysis;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class AutoAnalysisController {

    @FXML TreeView<Node> resultTree;

    public void init() {
        resultTree.setShowRoot(false);
        resultTree.setRoot(new TreeItem<>());
        resultTree.getRoot().setExpanded(true);
    }

    public void addResult(TreeItem<Node> result) {
        resultTree.getRoot().getChildren().add(result);
    }

}
