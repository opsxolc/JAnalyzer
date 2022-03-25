package Characteristics;

import javafx.scene.control.TreeItem;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.List;

public class CharacteristicsTreeItem<T> extends TreeItem<Pane> {
    public CharacteristicsTreeItem(String name, T val, Color color) {
        super();
        this.setValue(new CharacteristicPane<T>(name, val, color));
    }
    public CharacteristicsTreeItem(String name, T val) {
        super();
        this.setValue(new CharacteristicPane<T>(name, val));
    }
    public CharacteristicsTreeItem(){
        super();
    }
}
