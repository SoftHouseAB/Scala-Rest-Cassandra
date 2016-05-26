package com.roblayton.example

/**
  * Created by jaswath on 25-05-2016.
  */
import com.datastax.driver.core.{Cluster, Session}

import scala.collection.JavaConversions
import org.json4s.native.{Json, Serialization}
import org.json4s.native.Serialization._
import org.json4s.ShortTypeHints
import spray.json._
import DefaultJsonProtocol._

import scala.collection.mutable.HashMap

case class Movies(id:Int, name:String, status:String) extends ConnectToCassandra
case class Metrics(USERNAME:String, CPU_USAGE:String, VALUE:String, DATE_AND_TIME:String) extends ConnectToCassandra

/*object MyClassJsonProtocol extends DefaultJsonProtocol {
  implicit object MyClassJsonFormat extends JsonFormat[Movies] {
    override def write(obj: List[Movies]) =
      JsObject(
        (obj1 : Movies) = JsObject(Js
          "id" -> JsNumber(obj1.id),
          "name" -> JsString(obj1.name),
          "status" -> JsString(obj1.status)
        )
      )
    override def read(json: JsValue)= {
      json.asJsObject.getFields("id", "name", "status") match {
        case Seq(JsNumber(id), JsString(name), JsString(status)) =>
          Movies(id.toInt, name, status)
        case _ => throw new DeserializationException("Color expected")
      }
    }
  }
}*/

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

  def demo(): List[Movies] = {
    var movies = List[Movies]()
    try {
      val keyspace = "jaibalayya"
      val (cluster, session) = setup(keyspace, "localhost", 9042)
      println(session)
      val cql = "SELECT * FROM movies"
      val resultSet = session.execute( cql )
      val itr = JavaConversions.asScalaIterator(resultSet.iterator)
      itr.foreach( row => {
        val idv = row.getInt("id")
        val firstName = row.getString("name")
        val lastName = row.getString("status")
        movies = movies ::: List(Movies(idv, firstName, lastName))
        //println(s"$idv $firstName $lastName")
      })
    }
    finally {
      close()
      println("Done! demo")
      //println(session)
    }
    return movies
  }

  def addMovie(idd:Int, movieName:String, movieStatus:String) = {
    try {
      val keyspace = "jaibalayya"
      val (cluster, session) = setup(keyspace, "localhost", 9042)

      val cql = "INSERT INTO movies (id, name, status) VALUES ("+idd+",'"+movieName+"','"+movieStatus+"')"
      val resultSet = session.execute( cql )
    }
    finally {
      close()
      println("Done!")
    }
  }

  def addMetric(metric:Metrics) = {
    try {
      val keyspace = "jaibalayya"
      val (cluster, session) = setup(keyspace, "localhost", 9042)
      val intCpu = metric.CPU_USAGE.toInt
      val cql = "INSERT INTO metrics (cpu, username, value, date) VALUES ("+intCpu+",'"+metric.USERNAME+"','"+metric.VALUE+"','"+metric.DATE_AND_TIME+"')"
      val resultSet = session.execute( cql )
    }
    finally {
      close()
      println("Done!")
    }
  }

  private implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[ConnectToCassandra])))
  def toJSON(movie: List[Movies]) = Serialization.writePretty(movie)
}

trait ConnectToCassandra
