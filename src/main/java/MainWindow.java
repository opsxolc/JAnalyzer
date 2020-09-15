import json.StatJson;
import json.UseStatJson;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainWindow extends JFrame {
    private JTextField path;
    private JButton saveButton;
    private JButton loadButton;
    private JButton compareButton;
    private JButton deleteButton;
    private JButton closeButton;
    private JPanel contentPane;
    private JTable statListTable;
    private JTabbedPane tabbedPane1;

    public MainWindow() {
        setContentPane(contentPane);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addListeners();

        statListTable.setModel(new StatListDataModel());
        System.out.println("Init done");
    }

    private void addListeners() {
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    public static void main(String[] args) {
//        MainWindow mainWindow = new MainWindow();
//        mainWindow.setSize(new Dimension(800, 600));
//        mainWindow.setMinimumSize(new Dimension(800, 600));
//        mainWindow.pack();
//        mainWindow.setVisible(true);
//        String json = "{\"inter\":[{\"id\":{\"expr\":2000000000,\"nenter\":1.0,\"nlev\":0,\"nline\":20,\"nline_end\":83,\"pname\":\"jac2d.c\",\"t\":21},\"times\":{\"comm\":0.004381895065307617,\"comm_start\":0.0,\"efficiency\":0.4129233981078108,\"exec_time\":0.467540979385376,\"gpu_time_lost\":0.0,\"gpu_time_prod\":0.0,\"idle\":0.1619269847869873,\"insuf_sys\":0.0034399032592773438,\"insuf_user\":0.37921595573425293,\"load_imb\":0.07885050773620605,\"lost_time\":0.5489647388458252,\"nproc\":2,\"overlap\":2.956390380859375e-05,\"prod_cpu\":0.3826773166656494,\"prod_io\":0.0,\"prod_sys\":0.0034399032592773438,\"real_comm\":0.0,\"synch\":0.00027108192443847656,\"sys_time\":0.935081958770752,\"thr_sys_time\":0.0,\"thr_user_time\":0.0,\"threadsOfAllProcs\":2,\"time_var\":8.535385131835938e-05}},{\"id\":{\"expr\":1,\"nenter\":13.0,\"nlev\":1,\"nline\":48,\"nline_end\":61,\"pname\":\"jac2d.c\",\"t\":23},\"times\":{\"comm\":0.003943204879760742,\"comm_start\":0.0,\"efficiency\":0.40773380594398784,\"exec_time\":0.006120920181274414,\"gpu_time_lost\":0.0,\"gpu_time_prod\":0.0,\"idle\":0.0001277923583984375,\"insuf_sys\":0.001538991928100586,\"insuf_user\":0.0016404390335083008,\"load_imb\":0.0018676519393920898,\"lost_time\":0.007250428199768066,\"nproc\":2,\"overlap\":1.8596649169921875e-05,\"prod_cpu\":0.0034524202346801758,\"prod_io\":0.0,\"prod_sys\":0.001538991928100586,\"real_comm\":0.0,\"synch\":5.1021575927734375e-05,\"sys_time\":0.012241840362548828,\"thr_sys_time\":0.0,\"thr_user_time\":0.0,\"threadsOfAllProcs\":2,\"time_var\":5.1975250244140625e-05}},{\"id\":{\"expr\":11,\"nenter\":13.0,\"nlev\":2,\"nline\":49,\"nline_end\":58,\"pname\":\"jac2d.c\",\"t\":23},\"times\":{\"comm\":0.003943204879760742,\"comm_start\":0.0,\"efficiency\":0.4048567672018806,\"exec_time\":0.005984067916870117,\"gpu_time_lost\":0.0,\"gpu_time_prod\":0.0,\"idle\":0.0001461505889892578,\"insuf_sys\":0.0014151334762573242,\"insuf_user\":0.0016182661056518555,\"load_imb\":0.0018584728240966797,\"lost_time\":0.00712275505065918,\"nproc\":2,\"overlap\":1.8596649169921875e-05,\"prod_cpu\":0.0034302473068237305,\"prod_io\":0.0,\"prod_sys\":0.0014151334762573242,\"real_comm\":0.0,\"synch\":5.1021575927734375e-05,\"sys_time\":0.011968135833740234,\"thr_sys_time\":0.0,\"thr_user_time\":0.0,\"threadsOfAllProcs\":2,\"time_var\":5.1975250244140625e-05}},{\"id\":{\"expr\":12,\"nenter\":13.0,\"nlev\":2,\"nline\":59,\"nline_end\":60,\"pname\":\"jac2d.c\",\"t\":23},\"times\":{\"comm\":0.0,\"comm_start\":0.0,\"efficiency\":0.4765258215962441,\"exec_time\":5.078315734863281e-05,\"gpu_time_lost\":0.0,\"gpu_time_prod\":0.0,\"idle\":4.76837158203125e-06,\"insuf_sys\":4.363059997558594e-05,\"insuf_user\":4.76837158203125e-06,\"load_imb\":2.384185791015625e-06,\"lost_time\":5.316734313964844e-05,\"nproc\":2,\"overlap\":0.0,\"prod_cpu\":4.76837158203125e-06,\"prod_io\":0.0,\"prod_sys\":4.363059997558594e-05,\"real_comm\":0.0,\"synch\":0.0,\"sys_time\":0.00010156631469726563,\"thr_sys_time\":0.0,\"thr_user_time\":0.0,\"threadsOfAllProcs\":2,\"time_var\":0.0}},{\"id\":{\"expr\":2,\"nenter\":13.0,\"nlev\":1,\"nline\":65,\"nline_end\":70,\"pname\":\"jac2d.c\",\"t\":23},\"times\":{\"comm\":0.000438690185546875,\"comm_start\":0.0,\"efficiency\":0.5421259842519685,\"exec_time\":0.002573728561401367,\"gpu_time_lost\":0.0,\"gpu_time_prod\":0.0,\"idle\":7.390975952148438e-05,\"insuf_sys\":0.0007108449935913086,\"insuf_user\":0.0011334419250488281,\"load_imb\":3.2067298889160156e-05,\"lost_time\":0.002356886863708496,\"nproc\":2,\"overlap\":1.0967254638671875e-05,\"prod_cpu\":0.0020797252655029297,\"prod_io\":0.0,\"prod_sys\":0.0007108449935913086,\"real_comm\":0.0,\"synch\":0.0002200603485107422,\"sys_time\":0.005147457122802734,\"thr_sys_time\":0.0,\"thr_user_time\":0.0,\"threadsOfAllProcs\":2,\"time_var\":3.337860107421875e-05}}],\"iscomp\":false,\"nproc\":2,\"proc\":[{\"node_name\":\"MacBook-Penek.local\",\"test_time\":2.0685290000000003},{\"node_name\":\"MacBook-Penek.local\",\"test_time\":2.1559090000000003}]}",
//
        String resultJson;
        StatJson statJson;

        String result = LibraryImport.readStat("/Users/penek/Desktop/Specsem/Диплом/NAS/Statistics/withCUDA/bt.C.2x4x1.sts.gz+");
        System.out.println(result);

        statJson = UseStatJson.GetStat.apply(result);
        resultJson = UseStatJson.GetJson.apply(statJson);
//        System.out.println(resultJson);

    }

}
