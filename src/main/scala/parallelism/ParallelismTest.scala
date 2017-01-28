package parallelism

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._


/**
  * Created by Rachel on 2017. 1. 28..
  */

object ParallelismTest extends App {

  implicit val timeout = Timeout(60.seconds)
  val workerCount = args.headOption.getOrElse("4").toInt
  println(s"Using $workerCount worker instances")

  val system = ActorSystem("parallelism")
  import system.dispatcher
  sys.addShutdownHook(system.terminate)

  val master = system.actorOf(WorkerMaster.props(workerCount), "master")
  val start = System.currentTimeMillis()
  (master ? WorkerMaster.StartProcessing).
    mapTo[WorkerMaster.IterationCount].
    flatMap { iterations =>
      val time = System.currentTimeMillis() - start
      println(s"total time was: $time ms")
      println(s"total iterations was: ${iterations.count}")
      system.terminate()
    }.
    recover {
      case t: Throwable =>
        t.printStackTrace()
        system.terminate()
    }
}
