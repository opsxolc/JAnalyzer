import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class CharacteristicPane extends AnchorPane {
    private Characteristic characteristic;

    public Characteristic getCharacteristic(){return characteristic;}
    public void setCharacteristic(Characteristic characteristic){
        this.characteristic = characteristic;
    }


    public void CharacteristicPane(Characteristic characteristic){
        this.characteristic = characteristic;
        Label nameLabel = new Label(characteristic.getName());
        Label valueLabel = new Label(characteristic.getStringVal());
        nameLabel.setAlignment(Pos.CENTER_LEFT);
        valueLabel.setAlignment(Pos.CENTER_RIGHT);
        this.getChildren().addAll(nameLabel, valueLabel);
    }

}
