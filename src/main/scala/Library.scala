import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.{HttpApp, Route}
import akka.stream.scaladsl.Source

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Random, Success => ScalaSuccess}

object EventsApp extends HttpApp {
  var list = Seq(1)

  implicit val theSystem = ActorSystem(Logging.simpleName(this).replaceAll("\\$", ""))
  theSystem.scheduler.schedule(0.seconds, 2.seconds, () => list = list :+ Random.nextInt(1000))

  override def routes: Route =
    pathPrefix("api") {
      path("events" / IntNumber) { from =>
        get {
          complete {
            var last = from - 1

            Source
              .tick(0 seconds, 200 millis, NotUsed)
              .filter(_ => last <= list.size)
              .map { _ =>
                val result = list(last)
                last = last + 1
                result
              }
              .map(number => ServerSentEvent(number.toString))
              .keepAlive(10.second, () => ServerSentEvent.heartbeat)
          }
        }
      }
    }
}

object Main extends App {
  EventsApp.startServer("localhost", 8080)
}

