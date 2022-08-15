package org.cart

import java.util.UUID

object Models {
  final case class Product(id: UUID, name: String, price: BigDecimal)

  final case class LineItem(product: Product, quantity: Int )

  final case class Cart(items: Vector[LineItem]) {
    def addItem(item: LineItem): Cart = Cart(items :+ item)

    def total: BigDecimal = {
      items.foldLeft(BigDecimal("0"))((acc, next) =>
        acc.+(next.product.price.*(next.quantity))
      )
    }
  }

}
