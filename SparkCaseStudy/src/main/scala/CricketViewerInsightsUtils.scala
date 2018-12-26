import java.util
import casestudy.InfluxRestAPI.WriteToInfluxDB
import casestudy.Utils.AppDefaults
import com.google.gson.Gson
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.{Milliseconds, StreamingContext}
import java.util.Calendar
import java.lang.StringBuilder
import casestudy.Pojo.Viewer

@SerialVersionUID(100L)
class CricketViewerInsightsUtils extends Serializable {

  def getSparkConf() = {
    val sparkConf = new SparkConf().setAppName(AppDefaults.app_name).setMaster("local[2]") //.setMaster("yarn-client")
    sparkConf
  }

  def getStreamingContext(sparkConf: SparkConf) = {
    val ssc = new StreamingContext(sparkConf, Milliseconds(AppDefaults.app_streaming_batch_interval_milli_sec.toInt))

    ssc
  }

  def createDStream(sparkConf: SparkConf, streamingContext: StreamingContext) = {

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> AppDefaults.kafka_host,
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "viewer_id",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Array(AppDefaults.kafka_topic_name)
    val stream = KafkaUtils.createDirectStream[String, String](
      streamingContext,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams))
    stream
  }

  def parseEvents(streamdata: Iterator[String]): Iterator[(String, Double)] = {
    val listResult: util.ArrayList[(String, Double)] = new util.ArrayList[(String, Double)]()
    var data: String = new String
    val gson: Gson = new Gson()
    streamdata.foreach(data => {
      try {
        val recordValue: String = new String(data)
        val viewerObject: Viewer = gson.fromJson(recordValue, classOf[Viewer])
        val influxData = generateVaildEvents(viewerObject, recordValue)
        influxData match {
          case Some(points) => listResult.add(points)
          case None => LogHolder.LOG.warn(recordValue + "is not in correct format")
        }
      }
      catch {
        case exe: Exception => {
          LogHolder.LOG.warn("Exception in ParseEvent :" + exe.getLocalizedMessage)
        }
      }
    })

    import scala.collection.JavaConversions._
    listResult.iterator()
  }

  def generateVaildEvents(viewer: Viewer, str: String): Option[(String, Double)] = {
    if (viewer == null) {
      LogHolder.LOG.warn(str + "record object is null");
      return None
    }

    val id = viewer.getId()
    if (id.equals(null)) {
      LogHolder.LOG.warn(str + "id is null")
      return None
    }

    val value: Double = viewer.getValue
    if (value.equals(null)) {
      LogHolder.LOG.warn(str + "value is null")
      return None
    }

    val location: String = viewer.getLocation
    if (location == null) {
      LogHolder.LOG.warn(str + "location is null")
      return None
    }
    var validLocation: String = new String()
    var validValue: Double = 0
    val calendar = Calendar.getInstance
    //Returns current time in millis
    var timeMilli2 = calendar.getTimeInMillis
    var timeinnanoseconds = timeMilli2 * 1000000
    if (viewer != null && location != null) {
      validLocation = location
      validValue = value
    }
    return Some(validLocation, validValue)
  }

  def generateInfluxData(item: Iterator[(String, Double)]): Iterator[String] = {
    val listResult: util.ArrayList[String] = new util.ArrayList[String]()
    var itemString: String = new String()
    val calendar = Calendar.getInstance
    //Returns current time in millis
    var timeMilli2 = calendar.getTimeInMillis
    var timeinnanoseconds = timeMilli2 * 1000000
    item.foreach(data => {

      itemString = "%s value=%f %d\n".format(data._1.replaceAll(" ", "_"), data._2, timeinnanoseconds)
      listResult.add(itemString)
    })

    import scala.collection.JavaConversions._
    listResult.iterator()
  }

  def sendToInfluxDB(itermIterator: Iterator[String], host: String, databaseName: String, username: String, password: String) = {
    val writeToInfluxDB: WriteToInfluxDB = new WriteToInfluxDB()
    var response: String = null
    var res: String = null
    val itemBuilder: StringBuilder = new StringBuilder

    itermIterator.foreach(item => {
      itemBuilder.append(item)
    })
    try {
      writeToInfluxDB.write(itemBuilder, host, databaseName, username, password)

      response = writeToInfluxDB.write(itemBuilder.asInstanceOf, host, databaseName, username, password)
      LogHolder.LOG.info("Response from InfluxDB :" + response)
      res = response.iterator.asInstanceOf[String]
      LogHolder.LOG.info("Response Iterator in String :" + res)
    }
    catch {
      case ex: Exception => {
        LogHolder.LOG.error(ex.getLocalizedMessage)
      }
      case nullPointerException: NullPointerException => {
        LogHolder.LOG.error(nullPointerException.getLocalizedMessage)
      }
    }
  }
}
