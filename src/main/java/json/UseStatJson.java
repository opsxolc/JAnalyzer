package json;

import com.google.gson.Gson;

public class UseStatJson {
    private static Gson gson = new Gson();

    public static StatJson GetStat(String json){
        return gson.fromJson(json, StatJson.class);
    }

    public static String GetJson(StatJson stat) {
        return gson.toJson(stat);
    }

}