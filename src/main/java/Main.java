import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent p = FXMLLoader.load(getClass().getResource("main.fxml"));
        primaryStage.setTitle("Анализатор статистик");
        Scene scene = new Scene(p, 700, 600);
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image("/icon.png"));
        primaryStage.getScene().getStylesheets().add("series.css");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
