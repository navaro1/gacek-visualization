import java.net.InetSocketAddress

import scala.concurrent.duration._
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.petlew.gacek.visualisation.Store.{Events, GetEvents}
import com.petlew.gacek.visualisation.{Store, UdpEchoService}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future

object Main extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  private val store: ActorRef = system.actorOf(Props[Store])

  implicit lazy val timeout = Timeout(5.seconds)

  private val service = system.actorOf(
    UdpEchoService.props(new InetSocketAddress("localhost", 2321), store)
  )

  val serverSource = Http().bind(interface = "localhost", port = 8080)

  val requestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
      (store ? GetEvents()).mapTo[Events]
      HttpResponse(
        entity =
          HttpEntity(ContentTypes.`application/json`, """{"test":"pretty"}""")
      )

    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) =>
      HttpResponse(entity = "PONG!")

    case r: HttpRequest =>
      r.discardEntityBytes() // important to drain incoming HTTP Entity stream
      HttpResponse(404, entity = "Unknown resource!")
  }

  val bindingFuture: Future[Http.ServerBinding] =
    serverSource
      .to(Sink.foreach { connection =>
        println("Accepted new connection from " + connection.remoteAddress)

        connection handleWithSyncHandler requestHandler
      // this is equivalent to
      // connection handleWith { Flow[HttpRequest] map requestHandler }
      })
      .run()
}
