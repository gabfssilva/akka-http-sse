import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.{HttpApp, Route}
import akka.stream.scaladsl.Source

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random

import scala.util.{Success => ScalaSuccess}

object EventsApp extends HttpApp {
  val resources: mutable.Set[Int] = mutable.Set()

  implicit val theSystem = ActorSystem(Logging.simpleName(this).replaceAll("\\$", ""))
  theSystem.scheduler.schedule(0.seconds, 10.seconds, () => resources.add(Random.nextInt(1000)))

  override def routes: Route =
    pathPrefix("api") {
      path("events" / Segment) { event =>
        get {
          complete {
            Source
              .fromGraph(new RandomSource)
              .map(number => ServerSentEvent(number.toString, event))
              .keepAlive(2.second, () => ServerSentEvent.heartbeat)
          }
        }
      }

      path("resource") {
        get {
          def result: Future[Option[String]] = Future {
            if (resources.isEmpty) {
              None
            } else {
              val head = resources.head
              resources.remove(head)
              Some(head.toString)
            }
          }

          onComplete(result) {
            case ScalaSuccess(Some(x)) => complete(x)
            case ScalaSuccess(None) => complete(NotFound)
          }
        }
      }
    }
}

object Main extends App {
  EventsApp.startServer("localhost", 8080)
}

