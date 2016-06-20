package com.roblayton.example

/**
  * Created by jaswath on 25-05-2016.
  */
import java.text.SimpleDateFormat
import com.datastax.driver.core.{Cluster, Session}
import scala.collection.JavaConversions
import org.json4s.native.{Json, Serialization}
import org.json4s.native.Serialization._
import org.json4s.ShortTypeHints
import spray.json._
import DefaultJsonProtocol._
import scala.collection.mutable.HashMap

case class Metrics(USERNAME:String, CPU_USAGE:String, DATE_AND_TIME:String, IP_AD:String, MEMORY:String, NETWORK_IN:String, NETWORK_OUT:String) extends ConnectToCassandra
case class Devices(IP_AD:String) extends ConnectToCassandra
case class Multi_Metrics(IP_ADD:String, metrics: List[Metrics]) extends ConnectToCassandra

object ConnectToCassandra {

  var cluster:Cluster = null
  var session:Session = null

  def setup(keyspaceName:String, host:String, port:Int) :(Cluster,Session) = {
    cluster = Cluster.builder().addContactPoint(host).withPort(port).build()
    session = cluster.connect(keyspaceName)
    (cluster,session)
  }

  def close() = {
    if( session != null ) {
      session.close()
      session = null
    }
    if( cluster != null ) {
      cluster.close()
      cluster = null
    }
  }

  def getMetrics(): List[Metrics] = {
    var metrics = List[Metrics]()
    try {
      val keyspace = "servermetrics"
      val (cluster, session) = setup(keyspace, "localhost", 9042)
      val cql = "SELECT * FROM metrics"
      val resultSet = session.execute( cql )
      val itr = JavaConversions.asScalaIterator(resultSet.iterator)
      itr.foreach( row => {
        val cpu = row.getInt("cpu").toString
        val newDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm")
        val date = newDateFormat.format(row.getTimestamp("date")).toString
        val name = row.getString("username")
        val ipad = row.getInet("ipad").toString.split("/")
        val memory = row.getInt("memory").toString
        val networkIn = row.getInt("networkin").toString
        val networkOut = row.getInt("networkout").toString
        metrics = metrics ::: List(Metrics(name, cpu, date, ipad(1), memory, networkIn, networkOut))
        //println(s"$idv $firstName $lastName")
      })
    }
    finally {
      close()
      println("Done! demo empty")
      //println(session)
    }
    return metrics
  }

  def getDevices(): List[Devices] = {
    var metrics = List[Devices]()
    try {
      val keyspace = "servermetrics"
      val (cluster, session) = setup(keyspace, "localhost", 9042)
      val cql = "SELECT DISTINCT ipad FROM metrics"
      val resultSet = session.execute( cql )
      val itr = JavaConversions.asScalaIterator(resultSet.iterator)
      itr.foreach( row => {
        val ipad = row.getInet("ipad").toString.split("/");
        metrics = metrics ::: List(Devices(ipad(1)))
        //println(s"$idv $firstName $lastName")
      })
    }
    finally {
      close()
      println("Done! demo devices")
      //println(session)
    }
    return metrics
  }

  def getMetrics(ipaddd:String, startDate:String, endDate:String): List[Multi_Metrics] = {
    var multi_metrics = List[Multi_Metrics]()
    var multiIPS = ipaddd.split(",")
    for (ipadd <- multiIPS) {
      var metrics = List[Metrics]()
    try {
      val keyspace = "servermetrics"
      val (cluster, session) = setup(keyspace, "localhost", 9042)
      val cql = "SELECT * FROM metrics where ipad ='"+ipadd+"' AND date >='"+startDate+"' AND date <='"+endDate+"'"
      val resultSet = session.execute( cql )
      val itr = JavaConversions.asScalaIterator(resultSet.iterator)
      itr.foreach( row => {
        val cpu = row.getInt("cpu").toString
        val newDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm")
        val date = newDateFormat.format(row.getTimestamp("date")).toString
        val name = row.getString("username")
        val ipad = row.getInet("ipad").toString.split("/")
        val memory = row.getInt("memory").toString
        val networkIn = row.getInt("networkin").toString
        val networkOut = row.getInt("networkout").toString
        metrics = metrics ::: List(Metrics(name, cpu, date, ipad(1), memory, networkIn, networkOut))
        //println(s"$idv $firstName $lastName")
      })
    }
      catch {
        case unknown: Throwable => println("Got this unknown exception: " + unknown)
      }
    finally {
      close()
      println("Done! demo metrics")
      println(ipadd)
      //println(session)
    }
      multi_metrics = multi_metrics ::: List(Multi_Metrics(ipadd, metrics));
    }
    return multi_metrics
  }

  def addMetric(metric:Metrics) = {
    try {
      val keyspace = "servermetrics"
      val (cluster, session) = setup(keyspace, "localhost", 9042)
      val intCpu = metric.CPU_USAGE.toInt
      val intMemory = metric.MEMORY.toInt
      val intNetworkIn = metric.NETWORK_IN.toInt
      val intNetworkOut = metric.NETWORK_OUT.toInt
      val cql = "INSERT INTO metrics (ipad, date, cpu, username, memory, networkin, networkout) VALUES ('"+metric.IP_AD+"','"+metric.DATE_AND_TIME+"',"+intCpu+",'"+metric.USERNAME+"',"+intMemory+","+intNetworkIn+","+intNetworkOut+")"
      val resultSet = session.execute( cql )
    }
    finally {
      close()
      println("Done!")
    }
  }

  def delMetric(ip:String, date:String) = {
    try {
      val keyspace = "servermetrics"
      val (cluster, session) = setup(keyspace, "localhost", 9042)
      val cql = "DELETE FROM metrics WHERE ipad='"+ip+"' and date='"+date+"'"
      val resultSet = session.execute( cql )
    }
    finally {
      close()
      println("Done! delete metric")
    }
  }

  def delDevice(ip:String) = {
    try {
      val keyspace = "servermetrics"
      val (cluster, session) = setup(keyspace, "localhost", 9042)
      val cql = "DELETE FROM metrics WHERE ipad='"+ip+"'"
      val resultSet = session.execute( cql )
    }
    finally {
      close()
      println("Done! delete device")
    }
  }

  private implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[ConnectToCassandra])))
  def toJSONM(metric:List[Metrics])=Serialization.writePretty(metric)
  def toJSOND(metric:List[Devices])=Serialization.writePretty(metric)
  def toJSONMM(metric:List[Multi_Metrics])=Serialization.writePretty(metric)
}

trait ConnectToCassandra
