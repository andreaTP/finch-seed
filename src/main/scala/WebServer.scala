
import io.finch._
import com.twitter.finagle.Http
import com.twitter.finagle.http.{Response, Request}

import com.twitter.util.Await
import com.twitter.finagle.http.Status

import io.circe._
import io.circe.generic.auto._

import io.circe.syntax._

object WebServer extends App {

  //curl 'http://localhost:9090/hello/pippo'
  val hello = get("hello" :: string) { name : String =>
    Ok(s"Hello, $name")
  }

  //curl -X POST 'http://localhost:9090/user?name=Andrea&age=65'
  val user = post("user" :: param("name") :: param("age").as[Int]) { (name: String, age: Int) =>
    val user = User(name, age)
    Ok(s"Hello ${user.greet}")
  }

  case class User(name: String, age: Int) {
    val yo =
      if (age < 20) "young"
      else "old"

    val greet = s"Hey ${name} you are ${yo}"
  }

  val notFound: Endpoint[String] = * {
    Output.payload("Service not found", Status.NotFound) 
  }

  val api = (hello :+: user :+: notFound).handle {
    case e: Exception => BadRequest(e)
  }

  Await.ready(
    Http.serve(
      "localhost:9090",
      api.toService))
}
