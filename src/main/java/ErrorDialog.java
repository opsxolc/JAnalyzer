import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ErrorDialog {

    private ErrorController errorController;
    private Stage stage;

    public ErrorDialog(String message){
        FXMLLoader fxmlLoader = new FXMLLoader();
        Pane p;
        try {
            p = fxmlLoader.load(getClass().getResource("errorForm.fxml").openStream());
        } catch (Exception e){
            System.out.println("Error opening ErrorDialog: " + e);
            return;
        }
        errorController = fxmlLoader.getController();
        errorController.setMessage(message);
        Scene scene = new Scene(p);
        stage = new Stage();
        scene.getStylesheets().add("bootstrap3.css");
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
