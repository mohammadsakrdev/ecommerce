package shop.http.routes

import shop.domain.ID
import shop.domain.item._
import shop.generators._
import shop.services.Items

import cats.effect._
import cats.syntax.all._
import org.http4s.Method._
import org.http4s._
import org.http4s.client.dsl.io._
import org.http4s.syntax.literals._
import org.scalacheck.Gen
import suite.HttpSuite

object ItemRoutesSuite extends HttpSuite {

  def dataItems(items: List[Item]) = new TestItems {
    override def findAll: IO[List[Item]] =
      IO.pure(items)
  }

  def failingItems(items: List[Item]) = new TestItems {
    override def findAll: IO[List[Item]] =
      IO.raiseError(DummyError) *> IO.pure(items)
  }

  test("GET items succeeds") {
    forall(Gen.listOf(itemGen)) { it =>
      val req    = GET(uri"/items")
      val routes = ItemRoutes[IO](dataItems(it)).routes
      expectHttpBodyAndStatus(routes, req)(it, Status.Ok)
    }
  }

  test("GET items fails") {
    forall(Gen.listOf(itemGen)) { it =>
      val req    = GET(uri"/items")
      val routes = ItemRoutes[IO](failingItems(it)).routes
      expectHttpFailure(routes, req)
    }
  }

}

protected class TestItems extends Items[IO] {
  def findAll: IO[List[Item]]                    = IO.pure(List.empty)
  def findById(itemId: ItemId): IO[Option[Item]] = IO.pure(none[Item])
  def create(item: CreateItem): IO[ItemId]       = ID.make[IO, ItemId]
  def update(item: UpdateItem): IO[Unit]         = IO.unit
}
