import javafx.fxml.FXML;
import javafx.scene.control.Label;


public class StatTreeItemController {
    @FXML private Label execLabel;
    @FXML private Label coefLabel;

    public void init(double exec, double coef){
        execLabel.setText(String.format("%.3f", exec));
        coefLabel.setText(String.format("%.3f", coef));
    }
}
