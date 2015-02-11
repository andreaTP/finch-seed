import com.twitter.util.Await
import java.net.InetSocketAddress
import io.finch._
import io.finch.request._
import io.finch.response._
import com.twitter.finagle._
import com.twitter.finagle.Httpx
import com.twitter.finagle.httpx._
import com.twitter.finagle.httpx.path._

object WebServer extends App {

  Await.ready(
    Httpx.serve(
      new InetSocketAddress("localhost", 9090),
      MyEndpoint))

}

object MyEndpoint extends Endpoint[HttpRequest, HttpResponse] {

  def route = {
    case Method.Get -> Root / "hello" / name =>
      //curl 'http://localhost:9090/hello/Andrea'
      Service.mk(req =>
        Ok(s"Hello ${name}").toFuture
      )
    case Method.Post -> Root / "user" =>
      //curl -X POST 'http://localhost:9090/user?name=Andrea&age=65'
      Service.mk(req => {
        for {
          name <- RequiredParam("name")(req)
          age <- RequiredParam("age")(req).map(_.toInt)
        } yield {
          val user = User(name, age)
          Ok(s"Hello ${user.greet}")
        }
      })
    case _ -> path =>
      Service.mk(req =>
        BadRequest(s"Service not found for path: ${path.toString.replace(Root.toString, "")}").toFuture
      )
  }

  case class User(name: String, age: Int) {
    val yo =
      if (age < 20) "young"
      else "old"

    val greet = s"Hey ${name} you are ${yo}"
  }

}