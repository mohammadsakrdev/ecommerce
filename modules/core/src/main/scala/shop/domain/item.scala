package shop.domain

import java.util.UUID

import shop.domain.cart.{CartItem, Quantity}
import shop.optics.uuid
import derevo.cats._
import derevo.circe.magnolia._
import derevo.derive
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.string.{Uuid, ValidBigDecimal}
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import squants.market._

object item {

  @derive(decoder, encoder, keyDecoder, keyEncoder, eqv, show, uuid)
  @newtype case class ItemId(value: UUID)

  @derive(decoder, encoder, eqv, show)
  @newtype case class ItemName(value: String)

  @derive(decoder, encoder, eqv, show)
  @newtype case class ItemIsAvailable(value: Boolean)

  @derive(decoder, encoder, eqv, show)
  case class Item(
                   uuid: ItemId,
                   name: ItemName,
                   price: Money,
                   isAvailable: ItemIsAvailable
                 ) {
    def cart(q: Quantity): CartItem =
      CartItem(this, q)
  }

  // ----- Create item ------

  @derive(decoder, encoder, show)
  @newtype case class ItemNameParam(value: NonEmptyString)

  @derive(decoder, encoder, show)
  @newtype case class ItemIsAvailableParam(value: Boolean)

  @derive(decoder, encoder, show)
  @newtype case class PriceParam(value: String Refined ValidBigDecimal)

  @derive(decoder, encoder, show)
  case class CreateItemParam(
                              name: ItemNameParam,
                              price: PriceParam,
                              isAvailable: ItemIsAvailableParam,
                            ) {
    def toDomain: CreateItem =
      CreateItem(
        ItemName(name.value),
        USD(BigDecimal(price.value)),
        ItemIsAvailable(isAvailable.value),
      )
  }

  case class CreateItem(
                         name: ItemName,
                         price: Money,
                         isAvailable: ItemIsAvailable,
                       )

  // ----- Update item ------

  @derive(decoder, encoder)
  @newtype case class ItemIdParam(value: String Refined Uuid)

  @derive(decoder, encoder) case class UpdateItemParam(
                              id: ItemIdParam,
                              price: PriceParam
                            ) {
    def toDomain: UpdateItem =
      UpdateItem(
        ItemId(UUID.fromString(id.value)),
        USD(BigDecimal(price.value))
      )
  }

  @derive(decoder, encoder)
  case class UpdateItem(
                         id: ItemId,
                         price: Money
                       )

}
