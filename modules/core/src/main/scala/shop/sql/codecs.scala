package shop.sql

import shop.domain.auth._
import shop.domain.item._
import shop.domain.order._

import skunk._
import skunk.codec.all._
import squants.market._

object codecs {
  val itemId: Codec[ItemId]            = uuid.imap[ItemId](ItemId(_))(_.value)
  val itemName: Codec[ItemName]        = varchar.imap[ItemName](ItemName(_))(_.value)
  val itemIsAvailable: Codec[ItemIsAvailable] = bool.imap[ItemIsAvailable](ItemIsAvailable(_))(_.value)

  val orderId: Codec[OrderId]     = uuid.imap[OrderId](OrderId(_))(_.value)
  val paymentId: Codec[PaymentId] = uuid.imap[PaymentId](PaymentId(_))(_.value)

  val userId: Codec[UserId]     = uuid.imap[UserId](UserId(_))(_.value)
  val userName: Codec[UserName] = varchar.imap[UserName](UserName(_))(_.value)

  val money: Codec[Money] = numeric.imap[Money](USD(_))(_.amount)

  val encPassword: Codec[EncryptedPassword] = varchar.imap[EncryptedPassword](EncryptedPassword(_))(_.value)
}
