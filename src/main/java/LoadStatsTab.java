import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class LoadStatsTab {
    @FXML private Button loadStatButton;
    @FXML private Label label1;

    @FXML public void LoadStat(){
//        loadStatButton.setText("Loaded!");
        label1.setText("Loaded!");
    }
}
