import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import json.StatJson;
import json.UseStatJson;

public class Controller {

    @FXML private TabPane tabPane;
    @FXML private Button loadStatButton;
    @FXML private Label label1;
    @FXML private TextField loadPath;
    @FXML private Label statLabel;

    private void selectTab(int tabIndex){
        tabPane.getSelectionModel().select(tabIndex);
    }

    private void initStat(StatJson stat){
        statLabel.setText(stat.getHeader());
    }

    @FXML public void LoadStat() throws Exception{
        String stat = LibraryImport.readStat(loadPath.getText());
        if (stat == null) {
            ErrorDialog errorDialog = new ErrorDialog("Не найден файл \"" + loadPath.getText()
                + "\".");
            errorDialog.showDialog();
            return;
        }
        initStat(UseStatJson.GetStat(stat));
        label1.setText("Loaded!");
        selectTab(1);
    }

}
