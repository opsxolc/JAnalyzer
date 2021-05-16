import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Characteristic {
    private String stringVal;
    private double doubleVal;
    private int intVal;

    private String name;

    private static final NumberFormat f4 = new DecimalFormat("#0.####");

    public Characteristic(String name, String s){
        this.name = name;
        stringVal = s;
    }
    public Characteristic(String name, double d){
        this.name = name;
        setDoubleVal(d);
    }
    public Characteristic(String name, int i){
        this.name = name;
        setIntVal(i);
    }

    public String getStringVal(){return stringVal;}
    public void setStringVal(String s){stringVal = s;}

    public double getDoubleVal(){return doubleVal;}
    public void setDoubleVal(double d){
        doubleVal = d;
        stringVal = f4.format(d);
    }

    public int getIntVal(){return intVal;}
    public void setIntVal(int i) {
        intVal = i;
        stringVal = String.valueOf(intVal);
    }

    public String getName(){return name;}
    public void setName(String name){this.name = name;}

}
