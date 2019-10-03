import java.net.InetSocketAddress

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.petlew.gacek.visualisation.Store.{Events, GetEvents}
import com.petlew.gacek.visualisation.{EnqueueEvent, GraphBuilder, GraphRepresentation, Node, Store, UdpServer, VisualizationConfig, VisualizationServerConfig}
import spray.json._

import scala.concurrent.duration._

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val eventFormat = jsonFormat5(EnqueueEvent)
  implicit val nodeFormat = jsonFormat3(Node)
  implicit val linkFormat = jsonFormat3(com.petlew.gacek.visualisation.Link)
  implicit val graphFormat = jsonFormat2(GraphRepresentation)
}

object Main extends Directives with App with JsonSupport {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  private val store: ActorRef = system.actorOf(Props[Store])

  implicit lazy val timeout = Timeout(5.seconds)

  val config = VisualizationConfig.fromConfig(system.settings.config)
  val serviceConfig = VisualizationServerConfig.fromConfig(system.settings.config)

  private val service = system.actorOf(UdpServer.props(config.address, store))

  private val corsResponseHeaders = List(
    `Access-Control-Allow-Origin`.*,
    `Access-Control-Allow-Credentials`(true),
    `Access-Control-Allow-Headers`("Authorization",
      "Content-Type", "X-Requested-With"),
    `Access-Control-Max-Age`(1.day.toMillis) //Tell browser to cache OPTIONS requests
  )

  val routes =
    get {
      concat(
        pathSingleSlash {
          val eventualEvents = (store ? GetEvents)
            .mapTo[Events]
            .map(_.events)
            .map(_.map(_.asInstanceOf[EnqueueEvent]))
            .map(GraphBuilder.buildGraph)

          respondWithHeaders(corsResponseHeaders) {
            complete(eventualEvents)
          }
        }
      )
    }

  val serverSource =
    Http().bindAndHandle(routes, interface = serviceConfig.interface, port = serviceConfig.port)
}
