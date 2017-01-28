package parallelism

import akka.actor._


/**
  * Created by Rachel on 2017. 1. 28..
  */

object Worker {
  def props() = Props[Worker]
}

class Worker extends Actor {
  import WorkerMaster._

  def receive = {
    case DoWorkerWork =>
      var totalIterations = 0L
      var count = 10000000
      while (count > 0) {
        totalIterations += 1
        count -= 1
      }

      sender() ! IterationCount(totalIterations)
  }
}
