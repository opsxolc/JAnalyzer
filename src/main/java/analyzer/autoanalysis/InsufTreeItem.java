package analyzer.autoanalysis;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class InsufTreeItem extends AutoAnalysisTreeItem {

    public InsufTreeItem(double timeSeq, double timePar, double percentLost) {
        super("Неэффективный параллелизм", percentLost);

        double percent = timeSeq / (timeSeq + timePar) * 100;

        getChildren().add(new TreeItem<>(getPercentBar(percent)));
        getChildren().add(new TreeItem<>(getSolutionLabel(percent)));
    }

    private AnchorPane getPercentBar(double percent) {
        AnchorPane anchorPane = new AnchorPane();
        String styleString = "-fx-background-color: linear-gradient(to right, #d5efba 0%, #d5efba " +
                percent + "%, #b8e1f6 " + percent + "%, #b8e1f6 100%);";
        anchorPane.setStyle(styleString);

        return anchorPane;
    }

    private Label getSolutionLabel(double percent) {
        Label solutionLabel = new Label();
        solutionLabel.setWrapText(true);

        if (percent >= 80) {
            solutionLabel.setText(
                    "Большая часть времени была потеряна на последовательных участках,\n" +
                            "следует рассмотреть возможность их распараллеливания для повышения\n" +
                            "эффективности выполнения программы."
            );
        } else if (percent >= 20) {
            solutionLabel.setText(
                    "Время было потеряно как на параллельных так и на последовательных участках\n" +
                            "программы. Для уменьшения потерь на параллельных участках стоит попробовать\n" +
                            "выполнить программу на другой процессорной сетке."
            );
        } else {
            solutionLabel.setText(
                    "Большая часть времени была потеряна на параллельных участках,\n" +
                            "причиной потерь может быть неверное задание матрицы процессоров\n" +
                            "при запуске программы или неверное распределение данных и вычислений."
            );
        }

        return solutionLabel;
    }
}
