import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main extends Application {

    public static String StatDirPath = System.getProperty("user.dir") + "/statistics/";

    public static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
        Parent p = loader.load();
        primaryStage.setTitle("Анализатор статистик");
        Scene scene = new Scene(p, 700, 600);
        scene.getStylesheets().add("series.css");
        scene.getStylesheets().add("bootstrap3.css");
        scene.getStylesheets().add("progressBar.css");
        primaryStage.setScene(scene);
//        primaryStage.getIcons().add(new Image("/icon.png"));
        ((Controller)loader.getController()).initController(primaryStage);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
