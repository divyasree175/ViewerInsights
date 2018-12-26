
import casestudy.Utils.AppDefaults
import org.apache.spark.streaming.StreamingContext
import org.slf4j.LoggerFactory

object LogHolder extends Serializable {
  @transient lazy val LOG = LoggerFactory.getLogger(CricketViewerInsightsDriver.getClass)
}

object CricketViewerInsightsDriver {
  val cricketViewerInsightsUtils: CricketViewerInsightsUtils = new CricketViewerInsightsUtils();

  def createStreamingContext(): StreamingContext = {
    //create spark configuration and Streaming context
    val sparkConf = cricketViewerInsightsUtils.getSparkConf()
    val streamingContext = cricketViewerInsightsUtils.getStreamingContext(sparkConf)

    var parallelPartitions = AppDefaults.app_parallelism_partitions.toInt
    if (parallelPartitions < 0) {
      LogHolder.LOG.info("Using Task scheduler default parallelism")
      parallelPartitions = streamingContext.sparkContext.defaultParallelism
    }

    val dstream = cricketViewerInsightsUtils.createDStream(sparkConf, streamingContext)
    val mappedstream = dstream.map(record => record.value()).cache()
    val dStreamRepartition = mappedstream.repartition(parallelPartitions)

    val dStreamParse = dStreamRepartition.mapPartitions(partition => {
      val listIterator: Iterator[(String, Double)] = cricketViewerInsightsUtils.parseEvents(partition)

      listIterator
    })

    val dstreamInfluxData = dStreamParse.mapPartitions(partition => {
      val listIterator: Iterator[String] = cricketViewerInsightsUtils.generateInfluxData(partition)

      listIterator
    })
    dstreamInfluxData.persist()
    dstreamInfluxData.foreachRDD(rdd => {

      rdd.foreachPartition(str => {
        cricketViewerInsightsUtils.sendToInfluxDB(str, AppDefaults.influxHost, AppDefaults.influxDBName, AppDefaults.influxUserName, AppDefaults.influxPassword)

      })
    })

    streamingContext.checkpoint(AppDefaults.kafka_checkpoint_dir)
    return streamingContext
  }

  def main(args: Array[String]): Unit = {

    val default: AppDefaults = new AppDefaults
    val streamingContext = StreamingContext.getOrCreate(AppDefaults.kafka_checkpoint_dir, ()
    => createStreamingContext())

    streamingContext.start()
    streamingContext.awaitTermination()
  }
}
