package Utils;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.util.Duration;

public class Utils {
    //-----  Recursive function to add blink for expanded items  -----//
    public static void addBlink(TreeItem<?> treeItem) {
        new Timeline(
                new KeyFrame(Duration.seconds(0),
                        new KeyValue(((Node) treeItem.getValue()).opacityProperty(), 1.0)),
                new KeyFrame(Duration.seconds(0.3),
                        new KeyValue(((Node) treeItem.getValue()).opacityProperty(), 0.3, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.seconds(0.7),
                        new KeyValue(((Node) treeItem.getValue()).opacityProperty(), 1.0, Interpolator.EASE_OUT))
        ).play();
        for (Object item : treeItem.getChildren()) {
            addBlink((TreeItem<?>) item);
        }
    }
}
