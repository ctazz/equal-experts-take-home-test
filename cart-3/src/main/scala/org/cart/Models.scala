package org.cart

import java.util.UUID
import scala.math.BigDecimal.RoundingMode

object Models {
  object Constants {
    val NotFound = "Product not found"
    val InvalidQuantity = "Invalid quantity"
  }

  final case class SaleTotal(subtotal: BigDecimal, tax: BigDecimal, total: BigDecimal)

  final case class Product(id: UUID, name: String, price: BigDecimal)

  final case class LineItem(product: Product, quantity: Int)

  final case class Cart(items: Map[Product, LineItem]) {
    import Constants._
    //My suggesting for further improvement: If quantity is 0, remove product from Map
    def removeProduct(p: Product, quantity: Int): Either[String, Cart] = {

      items.get(p) match {
        case None => Left(NotFound)
        case Some(LineItem(_, q)) if q < quantity => Left(InvalidQuantity)
        case Some(lineItem) =>
          val modifiedLineItem = lineItem.copy(quantity = lineItem.quantity - quantity)
          val newItems = items + (p -> modifiedLineItem)
          Right(Cart(newItems))
      }
    }

    def addItem(item: LineItem): Cart = {
      val itemToAd = items.get(item.product).map(existingItemWithSameProduct =>
        LineItem(item.product, existingItemWithSameProduct.quantity + item.quantity)).
        getOrElse(item)

      if (itemToAd.quantity <= 0) Cart(items - item.product)
      else Cart(items + (item.product -> itemToAd))
    }

    private def round(bd: BigDecimal): BigDecimal = {
      bd.setScale(2, RoundingMode.HALF_UP)
    }

    private val Zero = BigDecimal("0")

    def total(salesTax: BigDecimal): SaleTotal = {
      val subtotal = items.values.foldLeft(Zero)((acc, next) =>
        acc.+(next.product.price.*(next.quantity)))
      val tax = if (subtotal.compare(Zero) > 0) round(subtotal * salesTax) else Zero
      SaleTotal(subtotal, tax, subtotal + (tax))
    }
  }

}
