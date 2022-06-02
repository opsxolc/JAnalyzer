package analyzer.autoanalysis;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;

public class CommTreeItem extends AutoAnalysisTreeItem {

    public CommTreeItem(
            double timeComm, double timeSynch, double timeLoadImb, double timeVar,
            double percentLost
    ) {
        super("Потери из-за рассинхронизации (коммуникации)", percentLost);

        // Comm близко к Sync
        if (AutoAnalysis.valuesAreClose(timeComm, timeSynch)) {
            // Comm близко к load_imb
            if (AutoAnalysis.valuesAreClose(timeComm, timeLoadImb)) {
                // На данном интервале величина потерь из-за коммуникаций близка
                // к потерям из-за разбалансировки вычислений.
                getChildren().add(new TreeItem<>(new Label(
                        "Величина потерь из-за коммуникаций близка к потерям из-за разбалансировки вычислений\n" +
                            "(из-за неравномерной загруженности процессов они могут затрачивать различное время\n" +
                            "на выполнение своих итераций цикла, что влечет за собой рассинхронизацию в дальнейшем).\n" +
                            "Чтобы повысить эффективность программы следует обратить внимание на распределение\n" +
                            "нагрузки между процессами и постараться сделать так, чтобы процессы выполняли равные\n" +
                            "объемы вычислений."
                )));
            }
            // Comm близко к time_var
            else {
                if (AutoAnalysis.valuesAreClose(timeComm, timeVar)) {
                    // На данном интервале величина потерь из-за коммуникаций близка
                    // к потерям из-за разброса времен выполнения коллективных операций
                    getChildren().add(new TreeItem<>(new Label(
                        "Величина потерь из-за коммуникаций близка к потерям из-за разброса\n" +
                            "времен выполнения коллективных операций (процессы затрачивают разное время\n" +
                            "на операцию, из-за чего возникает рассинхронизация). Стоит обратить внимание\n" +
                            "на используемые коллективные операции, рассмотреть альтернативные варианты\n" +
                            "реализации."
                    )));
                }
            }
        }
    }



}
