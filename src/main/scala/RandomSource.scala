import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.{Attributes, Outlet, SourceShape}
import akka.stream.stage.{AbstractOutHandler, GraphStage, GraphStageLogic}

import scala.concurrent.duration._

import scala.util.Random

/**
  * @author Gabriel Francisco <gabfssilva@gmail.com>
  */
class RandomSource extends GraphStage[SourceShape[Int]]{
  val out: Outlet[Int] = Outlet.create("RandomSource.out")
  val sourceShape: SourceShape[Int] = SourceShape.of(out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape){
      setHandler(out, new AbstractOutHandler {
        override def onPull(): Unit = push(out, random)
      })
    }
  }

  def random: Int = {
    Thread.sleep(1000)
    Random.nextInt(100)
  }

  override def shape: SourceShape[Int] = sourceShape
}
