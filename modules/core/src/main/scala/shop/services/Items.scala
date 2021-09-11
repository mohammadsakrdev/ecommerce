package shop.services

import shop.domain.ID
import shop.domain.item._
import shop.effects.GenUUID
import shop.sql.codecs._

import cats.effect._
import cats.syntax.all._
import skunk._
import skunk.implicits._

trait Items[F[_]] {
  def findAll: F[List[Item]]
  def findById(itemId: ItemId): F[Option[Item]]
  def create(item: CreateItem): F[ItemId]
  def update(item: UpdateItem): F[Unit]
}

object Items {
  def make[F[_]: Concurrent: GenUUID](
      postgres: Resource[F, Session[F]]
  ): Items[F] =
    new Items[F] {
      import ItemSQL._

      // In the book we'll see how to retrieve results in chunks using stream or cursor
      def findAll: F[List[Item]] =
        postgres.use(_.execute(selectAll))

      def findById(itemId: ItemId): F[Option[Item]] =
        postgres.use { session =>
          session.prepare(selectById).use { ps =>
            ps.option(itemId)
          }
        }

      def create(item: CreateItem): F[ItemId] =
        postgres.use { session =>
          session.prepare(insertItem).use { cmd =>
            ID.make[F, ItemId].flatMap { id =>
              cmd.execute(id ~ item).as(id)
            }
          }
        }

      def update(item: UpdateItem): F[Unit] =
        postgres.use { session =>
          session.prepare(updateItem).use { cmd =>
            cmd.execute(item).void
          }
        }
    }

}

private object ItemSQL {

  val decoder: Decoder[Item] =
    (itemId ~ itemName ~ money ~ itemIsAvailable).map {
      case i ~ n ~ p ~ ia =>
        Item(i, n, p, ia)
    }

  val selectAll: Query[Void, Item] =
    sql"""
        SELECT i.uuid, i.name, i.price, i.isAvailable
        FROM items AS i
       """.query(decoder)

  val selectById: Query[ItemId, Item] =
    sql"""
        SELECT i.uuid, i.name, i.price, i.isAvailable
        FROM items AS i
        WHERE i.uuid = $itemId
        and i.isAvailable = true
       """.query(decoder)

  val insertItem: Command[ItemId ~ CreateItem] =
    sql"""
        INSERT INTO items
        VALUES ($itemId, $itemName, $money, $itemIsAvailable,)
       """.command.contramap {
      case id ~ i =>
        id ~ i.name ~ i.price ~ i.isAvailable
    }

  val updateItem: Command[UpdateItem] =
    sql"""
        UPDATE items
        SET price = $money
        WHERE uuid = $itemId
       """.command.contramap(i => i.price ~ i.id)

}
