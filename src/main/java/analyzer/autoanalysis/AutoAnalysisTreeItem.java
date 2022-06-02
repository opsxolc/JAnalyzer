package analyzer.autoanalysis;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class AutoAnalysisTreeItem extends TreeItem<Node> {

    NumberFormat f2 = new DecimalFormat("#0.##");

    public AutoAnalysisTreeItem(String name, double percent) {
        super();

        HBox box = new HBox();
        box.getChildren().add(new Label(name));
        Label percentLabel = new Label(f2.format(percent) + "%");
        percentLabel.setTextFill(getPercentColor(percent));
        box.getChildren().add(percentLabel);

        setValue(box);
        setExpanded(true);
    }

    private Color getPercentColor(double percent) {
        if (percent >= 80)
            return Color.RED;
        if (percent >= 50)
            return Color.ORANGERED;
        if (percent >= 30)
            return Color.ORANGE;
        return Color.GREEN;
    }

}
