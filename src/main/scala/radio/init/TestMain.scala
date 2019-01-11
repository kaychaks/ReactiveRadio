package radio.init

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Flow, Framing, Keep, Sink, Source}
import akka.util.ByteString

import scala.concurrent.ExecutionContext
import scala.io.StdIn

object TestMain extends App {
  implicit val actorSystem = ActorSystem()
  implicit val ec: ExecutionContext = actorSystem.dispatcher
  implicit val log = Logging(actorSystem.eventStream, "reactive-radio")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val source = Source
    .single(ByteString("Line 1"))
    .concat(Source.single(ByteString(System.lineSeparator())))
    .concat(FileIO.fromPath(Paths.get("/Users/kaushik", "Music", "episode-71.mp3")))
    .take(10)


  val flow = Flow[ByteString]
    .via(Framing.delimiter(ByteString(System.lineSeparator()), maximumFrameLength = 10, allowTruncation = true))
    .take(2)

  val flow1 = Flow[ByteString]
    .dropWhile(_.utf8String != System.lineSeparator())
    .drop(1)


  val sink = Sink.foreach[ByteString](x => println(x))

//  source.via(flow).toMat(sink)(Keep.right).run().onComplete {
//    case ex => println(ex)
//  }

  source.via(flow1).toMat(sink)(Keep.right).run().onComplete {
    case ex => println(ex)
  }

  StdIn.readLine()

  actorSystem.terminate()

}
