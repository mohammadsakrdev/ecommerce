package shop

import java.util.UUID

import shop.domain.auth._
import shop.domain.cart._
import shop.domain.checkout._
import shop.domain.item._
import shop.domain.order._
import shop.domain.payment.Payment
import shop.http.auth.users._

import eu.timepit.refined.api.Refined
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import squants.market._

object generators {

  val nonEmptyStringGen: Gen[String] =
    Gen
      .chooseNum(21, 40)
      .flatMap { n =>
        Gen.buildableOfN[String, Char](n, Gen.alphaChar)
      }

  def nesGen[A](f: String => A): Gen[A] =
    nonEmptyStringGen.map(f)

  def idGen[A](f: UUID => A): Gen[A] =
    Gen.uuid.map(f)

  val itemIdGen: Gen[ItemId] =
    idGen(ItemId.apply)

  val itemNameGen: Gen[ItemName] =
    nesGen(ItemName.apply)

  val itemIsAvailable: Gen[ItemIsAvailable] =
    Gen.oneOf(List(ItemIsAvailable(true)))

  val userIdGen: Gen[UserId] =
    idGen(UserId.apply)

  val orderIdGen: Gen[OrderId] =
    idGen(OrderId.apply)

  val paymentIdGen: Gen[PaymentId] =
    idGen(PaymentId.apply)

  val userNameGen: Gen[UserName] =
    nesGen(UserName.apply)

  val passwordGen: Gen[Password] =
    nesGen(Password.apply)

  val encryptedPasswordGen: Gen[EncryptedPassword] =
    nesGen(EncryptedPassword.apply)

  val quantityGen: Gen[Quantity] =
    Gen.posNum[Int].map(Quantity.apply)

  val moneyGen: Gen[Money] =
    Gen.posNum[Long].map(n => USD(BigDecimal(n)))

  val itemGen: Gen[Item] =
    for {
      i <- itemIdGen
      n <- itemNameGen
      p <- moneyGen
      ia <- itemIsAvailable
    } yield Item(i, n, p, ia)

  val cartItemGen: Gen[CartItem] =
    for {
      i <- itemGen
      q <- quantityGen
    } yield CartItem(i, q)

  val cartTotalGen: Gen[CartTotal] =
    for {
      i <- Gen.nonEmptyListOf(cartItemGen)
      t <- moneyGen
    } yield CartTotal(i, t)

  val itemMapGen: Gen[(ItemId, Quantity)] =
    for {
      i <- itemIdGen
      q <- quantityGen
    } yield i -> q

  val cartGen: Gen[Cart] =
    Gen.nonEmptyMap(itemMapGen).map(Cart.apply)

  val cardNameGen: Gen[CardName] =
    Gen.stringOf(Gen.oneOf(('a' to 'z') ++ ('A' to 'Z'))).map { x =>
      CardName(Refined.unsafeApply(x))
    }

  private def sized(size: Int): Gen[Long] = {
    def go(s: Int, acc: String): Gen[Long] =
      Gen.oneOf(1 to 9).flatMap { n =>
        if (s == size) acc.toLong
        else go(s + 1, acc + n.toString)
      }

    go(0, "")
  }

  val cardGen: Gen[Card] =
    for {
      n <- cardNameGen
      u <- sized(16).map(x => CardNumber(Refined.unsafeApply(x)))
      x <- sized(4).map(x => CardExpiration(Refined.unsafeApply(x.toString)))
      c <- sized(3).map(x => CardCVV(Refined.unsafeApply(x.toInt)))
    } yield Card(n, u, x, c)

  // http routes generators

  val userGen: Gen[User] =
    for {
      i <- userIdGen
      n <- userNameGen
    } yield User(i, n)

  val adminUserGen: Gen[AdminUser] =
    userGen.map(AdminUser(_))

  val commonUserGen: Gen[CommonUser] =
    userGen.map(CommonUser(_))

  val paymentGen: Gen[Payment] =
    for {
      i <- userIdGen
      m <- moneyGen
      c <- cardGen
    } yield Payment(i, m, c)

  val createItemParamGen: Gen[CreateItemParam] =
    for {
      n <- arbitrary[String].map(ItemNameParam(_))
      p <- arbitrary[Int].map(PriceParam(_))
      ia <- arbitrary[Boolean].map(ItemIsAvailableParam(_))
    } yield CreateItemParam(n, p,ia)

}
