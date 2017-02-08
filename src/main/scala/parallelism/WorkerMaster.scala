package parallelism

import akka.actor._
import akka.routing.RoundRobinPool


/**
  * Created by Rachel on 2017. 1. 28..
  */

object WorkerMaster {
  sealed trait Command
  case object StartProcessing extends Command
  case object DoWorkerWork extends Command
  final case class IterationCount(count: Long) extends Command
  def props(workerCount: Int) = Props(classOf[WorkerMaster], workerCount)
}

class WorkerMaster(workerCount: Int) extends Actor {
  import WorkerMaster._

  val workers = context.actorOf(
    Worker.props.withRouter(
      RoundRobinPool(workerCount)
    ), "workerRouter"
  )

  def receive = waitingForRequest

  def waitingForRequest: Receive = {
    case StartProcessing =>
      val requestCount = 50000
      for(i <- 1 to requestCount) {
        workers ! DoWorkerWork
      }

      context.become(collectingResults(requestCount, sender()))
  }

  def collectingResults(remaining: Int, caller: ActorRef, iterations: Long = 0): Receive = {
    case IterationCount(count) =>
      val newRemaining = remaining - 1
      val newIterations = count + iterations

      if(newRemaining == 0) {
        caller ! IterationCount(newIterations)
        context.stop(self)
        context.system.terminate
      } else {
        context.become(collectingResults(newRemaining, caller, newIterations))
      }
  }

}
