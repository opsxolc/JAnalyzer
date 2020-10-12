public class StatRow {
    private String statInfo;
    private String creationTime;
    private Stat stat;

    public StatRow(String statInfo, String creationTime, Stat stat) {
        this.creationTime = creationTime;
        this.statInfo = statInfo;
        this.stat = stat;
    }

    public String getStatInfo() {
        return statInfo;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public Stat getStat() {
        return stat;
    }
}
