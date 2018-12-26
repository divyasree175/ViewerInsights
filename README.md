# ViewerInsights

##Overview
This Project is a spark streaming application which provides insights on cricket viewers watching from different locations in India.

##Description
The application streams the data from kafka consumer which contains the number of viewers watching cricket in different location (different states in India) and process it as influxDB data, get ingested into influxDB which measurement as location and the viewer count as value in various time periods. The insights collected are visualized in Granfana Dashboard, connecting it to influxDB showing the count of viewers for different areas.

##Components
Java 1.8
Spark 2.2.0
Scala 2.11.8
Zookeeper 3.4.10
Kafka_2.11-2.1
InfluxDB 1.7.2
Grafana 5.4.2

