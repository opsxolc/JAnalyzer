import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import json.StatJson;
import json.UseStatJson;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
//        String result = LibraryImport.readStat("/Users/penek/Desktop/Specsem/Диплом/NAS/Statistics/withCUDA/bt.C.2x4x1.sts.gz+");
//        System.out.println("OK");
//
//        StatJson statJson = UseStatJson.GetStat.apply(result);
//        String resultJson = UseStatJson.GetJson.apply(statJson);
//        System.out.println(resultJson);
//        SplitPane sp = new SplitPane();
//        Button b = new Button("Left Control");
//        b.setMinSize(0,0);
//        VBox leftControl  = new VBox(b);
//        VBox midControl   = new VBox(new Button("Mid Control"));
//        VBox rightControl = new VBox(new Button("Right Control"));
//        sp.getItems().addAll(leftControl, midControl, rightControl);
//        primaryStage.setScene(new Scene(sp));
    }


    public static void main(String[] args) {
        launch(args);
    }

}
