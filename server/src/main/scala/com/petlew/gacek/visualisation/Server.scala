import java.net.InetSocketAddress

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.petlew.gacek.visualisation.Store.{Events, GetEvents}
import com.petlew.gacek.visualisation.{EnqueueEvent, Store, UdpEchoService}
import spray.json._

import scala.concurrent.duration._

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val eventFormat = jsonFormat5(EnqueueEvent)
}

object Main extends Directives with App with JsonSupport {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  private val store: ActorRef = system.actorOf(Props[Store])

  implicit lazy val timeout = Timeout(5.seconds)

  private val service = system.actorOf(
    UdpEchoService.props(new InetSocketAddress("localhost", 2321), store)
  )

  val routes =
    get {
      concat(
        pathSingleSlash {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<html><body>Hello world!</body></html>"))
        },
        path("ping") {
          complete("PONG!")
        },
        path("crash") {
          sys.error("BOOM!")
        }
      )
    }
//    concat(
//      pathPrefix("") {
//        get {
//          pathEnd {
//            val eventualEvents = (store ? GetEvents).mapTo[Events].map(_.events).map(_.map(_.asInstanceOf[EnqueueEvent]))
//            complete(eventualEvents)
//          }
//        }
//      },

//
//  val requestHandler: HttpRequest => HttpResponse = {
//    case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
//      val eventualEvents: Future[Events] = (store ? GetEvents()).mapTo[Events]
//      println(eventualEvents.)
//      complete(HttpResponse(
//        entity =
//          HttpEntity(ContentTypes.`application/json`, """{"test":"pretty"}""")
//      ))
//
//    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) =>
//      HttpResponse(entity = "PONG!")
//
//    case r: HttpRequest =>
//      r.discardEntityBytes() // important to drain incoming HTTP Entity stream
//      HttpResponse(404, entity = "Unknown resource!")
//  }

//  val bindingFuture: Future[Http.ServerBinding] =
//    serverSource
//      .to(Sink.foreach { connection =>
//        println("Accepted new connection from " + connection.remoteAddress)
//
//        connection handleWithSyncHandler requestHandler
//      // this is equivalent to
//      // connection handleWith { Flow[HttpRequest] map requestHandler }
//      })
//      .run()

  val serverSource =
    Http().bindAndHandle(routes, interface = "localhost", port = 8080)
}
