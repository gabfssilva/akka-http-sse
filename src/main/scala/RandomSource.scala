import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.{Attributes, Outlet, SourceShape}
import akka.stream.stage.{AbstractOutHandler, GraphStage, GraphStageLogic}

import scala.concurrent.duration._
import scala.util.Random

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * @author Gabriel Francisco <gabfssilva@gmail.com>
  */
class RandomSource(from: Int) extends GraphStage[SourceShape[Int]]{
  var list = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
  var last = from - 1

  implicit val theSystem = ActorSystem(Logging.simpleName(this).replaceAll("\\$", ""))
  theSystem.scheduler.schedule(0.seconds, 2.seconds, () => list = random :: list)

  val out: Outlet[Int] = Outlet.create("RandomSource.out")
  val sourceShape: SourceShape[Int] = SourceShape.of(out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape){
      setHandler(out, new AbstractOutHandler {
        override def onPull(): Unit = if (last < list.size) {
          push(out, list(last))
          last = last + 1
        } else {
          setKeepGoing(true)
        }
      })
    }
  }

  def random: Int = {
    Random.nextInt(100)
  }

  override def shape: SourceShape[Int] = sourceShape
}
