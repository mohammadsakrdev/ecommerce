package shop.http.routes

import shop.services.Items

import cats.Monad
import org.http4s._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final case class ItemRoutes[F[_] : Monad](
                                           items: Items[F]
                                         ) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/items"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {

    case GET -> Root =>
      Ok(items.findAll)

  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
