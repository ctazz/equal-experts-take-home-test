package org.cart

import cats.data.Validated

import java.util.UUID
import scala.math.BigDecimal.RoundingMode
import cats.implicits._

object Models {
  final case class SaleTotal(subtotal: BigDecimal, tax: BigDecimal, total: BigDecimal)

  final case class Product(id: UUID, name: String, price: BigDecimal)

  final case class LineItem(product: Product, quantity: Int)

  object PriceConstants {
    val Zero = BigDecimal("0")
  }

  final case class Cart(items: Map[Product, Int]) {
    import PriceConstants._
    def addItem(prod: Product, quant: Int) = {
      addItems(Map(prod -> quant))
    }

    def addItems(moreItems: Map[Product, Int]): Cart = {
      Cart(items |+| moreItems)
    }

    private def round(bd: BigDecimal): BigDecimal = {
      bd.setScale(2, RoundingMode.HALF_UP)
    }

    def total(salesTax: BigDecimal): SaleTotal = {
      val subtotal = items.foldLeft(Zero) { case (sumSoFar, (product, quantiy)) => sumSoFar + (product.price * quantiy) }
      val tax = if (subtotal.compare(Zero) > 0) round(subtotal * salesTax) else Zero
      SaleTotal(subtotal, tax, subtotal + (tax))
    }
  }

  //cartId, itemId, itemName, itemPrice, quantity.
  case class CartItem(cartId: UUID, productId: UUID, productName: String, price: BigDecimal, quantity: Int)

  sealed trait ProductError {
    val cartId: UUID
    val productId: UUID
  }
  final case class InvalidPrice(cartId: UUID, productId: UUID) extends ProductError
  final case class InvalidQuantity(cartId: UUID, productId: UUID) extends ProductError

  object CartHandler {
    import PriceConstants._

    def validPrice(cartItem: CartItem) =
      Validated.cond(cartItem.price.compare(Zero) > 0, cartItem.price, Vector(InvalidPrice(cartItem.cartId, cartItem.productId)))

    def validQuantity(ci: CartItem) = Validated.cond(ci.quantity > 0, ci.quantity, Vector(InvalidQuantity(ci.cartId, ci.productId)))

    def valid(ci: CartItem): Validated[Vector[ProductError], (UUID, Product, Int)] = {
      (validPrice(ci), validQuantity(ci)).mapN {
        case (price, quant) =>
          (ci.cartId, Product(ci.productId, ci.productName, price), quant)
      }
    }

    def update(cartMap: Map[UUID, Cart], cartItems: Vector[CartItem]): (Vector[ProductError], Map[UUID, Cart]) = {

      val validAndInvalid: Vector[Validated[Vector[ProductError], (UUID, Product, Int)]] = cartItems.map(valid)
      //turn Vector[Validated] into Validated[Vector], but only for the invalid ones, then use fold to pull out the Vector[ProductError]
      val temp: Validated[Vector[ProductError], Vector[(UUID, Product, Int)]] = validAndInvalid.filter(_.isInvalid).sequence
      val invalids: Vector[ProductError] = temp.fold[Vector[ProductError]](identity, _ => Vector.empty[ProductError])
      //same trick but now getting the valid cartId/Product/Int
      val valids: Vector[(UUID, Product, Int)] = validAndInvalid.filter(_.isValid).sequence.fold(_ => Vector.empty[(UUID, Product, Int)], identity)

      //Could also decide to give an error when cartId doesn't match existing carts!
      val updatedCart = valids.foldLeft(cartMap) {
        case (acc, (cartId, product, quant)) =>
          acc + (cartId ->
            acc.get(cartId).map(cartSoFar =>
              cartSoFar.addItem(product, quant)).getOrElse(
              Cart(Map(product -> quant))))
      }

      (invalids, updatedCart)
    }
  }

}
