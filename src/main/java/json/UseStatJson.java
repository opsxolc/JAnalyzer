package json;

import com.google.gson.Gson;

import java.util.function.Function;

public class UseStatJson {
    private static Gson gson = new Gson();

    public static Function<String, StatJson> GetStat = (String json) -> gson.fromJson(json, StatJson.class);
    public static Function<StatJson, String> GetJson = (StatJson stat) -> gson.toJson(stat);
}