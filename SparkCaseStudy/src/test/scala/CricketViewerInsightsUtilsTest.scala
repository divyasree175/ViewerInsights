import casestudy.InfluxRestAPI.WriteToInfluxDB
import casestudy.Pojo.Viewer
import casestudy.Utils.AppDefaults
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{StreamingContext, StreamingContextState}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite, PrivateMethodTester}

@RunWith(classOf[JUnitRunner])
class CricketViewerInsightsUtilsTest extends FunSuite with BeforeAndAfter with PrivateMethodTester {

  private var cricketViewerInsights: CricketViewerInsightsUtils = new CricketViewerInsightsUtils
  val StrRecord: String = " {\"id\" : 1,\"value\" : 23.000,\"location\" : \"Tamilnadu\"}"

  test("test get spark conf") {

    val getSparkConf = PrivateMethod[SparkConf]('getSparkConf)
    val sparkConf = cricketViewerInsights invokePrivate getSparkConf()
    assert(sparkConf.get("spark.app.name") === (AppDefaults.app_name))
  }


  test("test get streaming context") {

    val getSparkConf = PrivateMethod[SparkConf]('getSparkConf)
    val sparkConf = cricketViewerInsights invokePrivate getSparkConf()
    sparkConf.setMaster("local[*]")
    val getStreamingContext = PrivateMethod[StreamingContext]('getStreamingContext)
    val ssc = cricketViewerInsights invokePrivate getStreamingContext(sparkConf)
    try {
      assert(ssc.getState() == StreamingContextState.INITIALIZED)
    }
    finally {
      if (ssc != null) {
        ssc.stop()
      }
    }
  }

  test("test create dstream") {
    val getSparkConf = PrivateMethod[SparkConf]('getSparkConf)
    val sparkConf = cricketViewerInsights invokePrivate getSparkConf()
    sparkConf.setMaster("local[*]")
    val getStreamingContext = PrivateMethod[StreamingContext]('getStreamingContext)
    val ssc = cricketViewerInsights invokePrivate getStreamingContext(sparkConf)
    try {
      val createDstream = PrivateMethod[DStream[scala.Array[scala.Byte]]]('createDStream)
      val dstream = cricketViewerInsights invokePrivate createDstream(sparkConf, ssc)
      assert(dstream != null)
    }
    finally {
      if (ssc != null) {
        ssc.stop()
      }
    }
  }

  test("test for parseEvents") {
    val lstEventData: List[String] = List(StrRecord)
    val parsedEventsList: List[(String, Double)] =
      cricketViewerInsights.parseEvents(lstEventData.iterator).toList
    assert(parsedEventsList != null)
    assert(parsedEventsList.size === 1)
    assert(parsedEventsList(0)._1.equals("Tamilnadu"))
    assert(parsedEventsList(0)._2.equals(23.000))
  }

  test("write to influxDB") {
    val influxData: String = "samplemeasure value=98.444 1545659632000000000"
    val sb: java.lang.StringBuilder = new java.lang.StringBuilder
    sb.append(influxData)

    val writerInfluxDB: WriteToInfluxDB = new WriteToInfluxDB
    val response = writerInfluxDB.write(sb, AppDefaults.influxHost, AppDefaults.influxDBName, AppDefaults.influxUserName, AppDefaults.influxPassword)

  }


}
