import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class CharacteristicPane extends GridPane {
    private Characteristic characteristic;

    public Characteristic getCharacteristic(){return characteristic;}
    public void setCharacteristic(Characteristic characteristic){
        this.characteristic = characteristic;
    }

    private void initPane(){
        Label nameLabel = new Label(characteristic.getName());
        Label valueLabel = new Label(characteristic.getStringVal());
        nameLabel.setAlignment(Pos.CENTER_LEFT);
        valueLabel.setAlignment(Pos.CENTER_RIGHT);
        addRow(0, nameLabel, valueLabel);
        setHalignment(nameLabel, HPos.LEFT);
        setValignment(nameLabel, VPos.CENTER);
        setHalignment(valueLabel, HPos.RIGHT);
        setValignment(valueLabel, VPos.CENTER);

        setVgap(3);
        setPadding(new Insets(2, 5, 5, 5));

        ColumnConstraints column1 = new ColumnConstraints(80,100,Double.MAX_VALUE);
        ColumnConstraints column2 = new ColumnConstraints(0,100,Double.MAX_VALUE);
        column2.setHgrow(Priority.SOMETIMES);
        column1.setHgrow(Priority.SOMETIMES);
        getColumnConstraints().addAll(column1, column2);

    }


    public CharacteristicPane(Characteristic characteristic){
        super();
        this.characteristic = characteristic;
        initPane();
    }

    public CharacteristicPane(String name, String value){
        super();
        this.characteristic = new Characteristic(name, value);
        initPane();
    }

    public CharacteristicPane(String name, double value){
        super();
        this.characteristic = new Characteristic(name, value);
        initPane();
    }

    public CharacteristicPane(String name, int value){
        super();
        this.characteristic = new Characteristic(name, value);
        initPane();
    }

}
