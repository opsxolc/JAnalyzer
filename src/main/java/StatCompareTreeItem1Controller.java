import javafx.fxml.FXML;
import javafx.scene.control.Label;


public class StatCompareTreeItem1Controller {
    @FXML private Label maxLabel;
    @FXML private Label minLabel;
    @FXML private Label maxTimeLabel;
    @FXML private Label minTimeLabel;
    @FXML private Label typeLabel;


    public void init(String max, String min, double maxTime, double minTime, String type){
        maxLabel.setText(String.format("%.2fs", maxTime));
        minLabel.setText(String.format("%.2fs", minTime));
        maxTimeLabel.setText(max);
        minTimeLabel.setText(min);
        typeLabel.setText(type);
    }
}
