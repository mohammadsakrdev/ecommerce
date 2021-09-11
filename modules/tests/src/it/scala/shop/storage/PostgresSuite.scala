package shop.storage

import shop.domain._
import shop.domain.item._
import shop.generators._
import shop.services._

import cats.data.NonEmptyList
import cats.effect._
import cats.implicits._
import natchez.Trace.Implicits.noop
import org.scalacheck.Gen
import skunk._
import skunk.implicits._
import suite.ResourceSuite

object PostgresSuite extends ResourceSuite {

  val flushTables: List[Command[Void]] =
    List("items", "orders", "users").map { table =>
      sql"DELETE FROM #$table".command
    }

  type Res = Resource[IO, Session[IO]]

  override def sharedResource: Resource[IO, Res] =
    Session
      .pooled[IO](
        host = "localhost",
        port = 5432,
        user = "postgres",
        password = Some("my-password"),
        database = "store",
        max = 10
      )
      .beforeAll {
        _.use { s =>
          flushTables.traverse_(s.execute)
        }
      }


  test("Items") { postgres =>
    forall(itemGen) { item =>
      def newItem(
      ) = CreateItem(
        name = item.name,
        isAvailable = item.isAvailable,
        price = item.price
      )

      val i = Items.make[IO](postgres)

      for {
        x <- i.findAll
        _ <- i.create(newItem())
        y <- i.findAll
      } yield expect.all(x.isEmpty, y.count(_.name === item.name) === 1)
    }
  }

  test("Users") { postgres =>
    val gen = for {
      u <- userNameGen
      p <- encryptedPasswordGen
    } yield u -> p

    forall(gen) {
      case (username, password) =>
        val u = Users.make[IO](postgres)
        for {
          d <- u.create(username, password)
          x <- u.find(username)
          z <- u.create(username, password).attempt
        } yield expect.all(x.count(_.id === d) === 1, z.isLeft)
    }
  }

  test("Orders") { postgres =>
    val gen = for {
      oid <- orderIdGen
      pid <- paymentIdGen
      un  <- userNameGen
      pw  <- encryptedPasswordGen
      it  <- Gen.nonEmptyListOf(cartItemGen).map(NonEmptyList.fromListUnsafe)
      pr  <- moneyGen
    } yield (oid, pid, un, pw, it, pr)

    forall(gen) {
      case (oid, pid, un, pw, items, price) =>
        val o = Orders.make[IO](postgres)
        val u = Users.make[IO](postgres)
        for {
          d <- u.create(un, pw)
          x <- o.findBy(d)
          y <- o.get(d, oid)
          i <- o.create(d, pid, items, price)
        } yield expect.all(x.isEmpty, y.isEmpty, i.value.version === 4)
    }
  }

}
