import javafx.scene.control.Button;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.awt.*;
import java.util.function.Predicate;

public class ChooseProcButton extends MenuButton {
    private final TextField textField;
    private Predicate<Integer> chosenProcsPred = t -> false;

    public ChooseProcButton() {
        super();

        setText("Выбор процесса(-ов)");

        Pane pane = new Pane();
        textField = new TextField();
        textField.setPromptText("1, 4-5, 2k+1");
        Button chooseButton = new Button("Выбрать");

        chooseButton.setOnAction(e -> {
            parseAndSetPred(textField.getText());
            // TODO: update all
        });

        pane.setPrefSize(188, 29);
        textField.setPrefSize(114, 29);
        chooseButton.setPrefSize(70, 29);

        chooseButton.setLayoutX(118);
        chooseButton.setLayoutY(1);

        pane.getChildren().addAll(textField, chooseButton);

        CustomMenuItem chooseProcMenuItem = new CustomMenuItem();
        chooseProcMenuItem.setContent(pane);

        this.getItems().add(chooseProcMenuItem);
    }

    private boolean isNumeric(String str) {
        return str != null && str.matches("[0-9.]+");
    }

    private boolean isRange(String str) {
        return str != null && str.matches("[0-9.]+-[0-9.]+");
    }

    private void parseAndSetPred(String text) {
        chosenProcsPred = t -> false;
        text = text.replaceAll(" ", "");
        String[] strings = text.split(",");
        for (String s : strings) {
            if (isNumeric(s)) {
                chosenProcsPred = t -> chosenProcsPred.test(t) || t == Integer.parseInt(s);
                continue;
            }

            if (isRange(s)) {
                String[] range = s.split("-");
                if (range.length < 2) continue;
                chosenProcsPred = t -> chosenProcsPred.test(t)
                        || Integer.parseInt(range[0]) <= t || t <= Integer.parseInt(range[1]);
                continue;
            }

            // TODO: parse func
            System.out.println("[WARN] invalid string in chooseProcs: " + s);
        }

    }

    public Predicate<Integer> getChosenProcsPred(){
        return chosenProcsPred;
    }

}
