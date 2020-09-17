import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ErrorDialog {

    private ErrorController errorController;
    private Stage stage;

    public ErrorDialog(String message) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader();
        Pane p = fxmlLoader.load(getClass().getResource("errorForm.fxml").openStream());
        errorController = fxmlLoader.getController();
        errorController.setMessage(message);
        Scene scene = new Scene(p);
        stage = new Stage();
        stage.setScene(scene);
        stage.getIcons().add(new Image("/icon.png"));
        stage.setTitle("Ошибка");
        stage.initModality(Modality.APPLICATION_MODAL);
    }

    public void showDialog(){
        if (stage != null)
            stage.showAndWait();
    }

}
