package analyzer.autoanalysis;

import analyzer.stat.Interval;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;

public class AutoAnalysis {

    AnchorPane mainPane;
    AutoAnalysisController controller;

    protected static boolean valuesAreClose(double x, double y) {
        return Math.abs(x - y)
                <= 0.2 * Math.max(x, y);
    }

    public Node getMainPane() {
        return mainPane.getChildren().get(0);
    }

    public AutoAnalysis(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("auto_analysis.fxml"));

        try {
            mainPane = fxmlLoader.load();
            controller = fxmlLoader.getController();
        } catch (Exception e){
            System.out.println("Error creating analyzer.autoanalysis.AutoAnalysis: " + e);
        }

        controller.init();
    }

    private ArrayList<String> analyzeInsufPar(
            Interval interval, Double[] times, Double max
    ) {
        ArrayList<String> ids = new ArrayList<>();

        if (!interval.hasChildLoopInterval()) {
            switch (interval.info.id.t) {
                case Interval.SEQ:
                    times[0] += interval.info.times.insuf;
                    break;
                case Interval.PAR:
                    times[1] += interval.info.times.insuf;
                    break;
            }

            if (interval.info.times.insuf >= 0.2 * max)
                ids.add(interval.id);
        }

        for (Interval inter : interval.intervals)
            ids.addAll(analyzeInsufPar(inter, times, max));

        return ids;
    }

    public void Analyze(Interval interval) {
        // Clean up
        controller.resultTree.getRoot().getChildren().clear();

        // Неэффективный параллелизм
        Double[] times = {0., 0.};
        // TODO: добавить времена посл и пар
        ArrayList<String> insufIntervals = analyzeInsufPar(interval, times, interval.info.times.insuf);
        controller.addResult(new InsufTreeItem(times[0], times[1],
                (interval.info.times.lost_time > 0) ? interval.info.times.insuf / interval.info.times.lost_time * 100: 0 ));

        // Коммуникации
        controller.addResult(new CommTreeItem(interval.info.times.comm, interval.info.times.synch,
                interval.info.times.load_imb, interval.info.times.time_var,
                (interval.info.times.lost_time > 0) ? interval.info.times.comm / interval.info.times.lost_time * 100 : 0));

        System.out.println(times[0] + " " + times[1]);
        System.out.println(insufIntervals);

        System.out.println("Analysis done");
    }
}
