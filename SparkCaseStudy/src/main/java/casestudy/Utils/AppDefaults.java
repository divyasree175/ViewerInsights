package casestudy.Utils;

import java.io.Serializable;

public class AppDefaults implements Serializable
{
    public static final String app_name = "CricketViewerInsights";
    public static final String app_streaming_batch_interval_milli_sec = "30000";
    public static final String app_error_records_dir = "C:\\Users\\h172170\\Documents\\casestudy\\ProjectLogs\\app-error";
    public static final String app_parallelism_partitions = "-1";
    public static final String kafka_checkpoint_dir = "C:\\Users\\h172170\\Documents\\casestudy\\ProjectLogs\\kafka-chkpoint";
    public static final String influxDBName = "cricketViewer";
    public static final String influxUserName = "divyasree";
    public static final String influxPassword = "divyasree";
    public static final String influxHost = "http://localhost:8086";
    public static final String kafka_topic_name = "cricketViewer";
    public static final String kafka_host = "localhost:9092";

}
