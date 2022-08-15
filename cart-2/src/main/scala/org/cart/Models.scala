package org.cart

import java.util.UUID

object Models {
  final case class Product(id: UUID, name: String, price: BigDecimal)

  final case class LineItem(product: Product, quantity: Int )

  final case class Cart(items: Map[Product, LineItem]) {
    def addItem(item: LineItem): Cart = {
      val itemToAd = items.get(item.product).map(existingItemWithSameProduct =>
          LineItem(item.product, existingItemWithSameProduct.quantity + item.quantity)
      ).getOrElse(item)

      Cart(
     items + (item.product -> itemToAd)
      )
    }

    def total: BigDecimal = {
      items.values.foldLeft(BigDecimal("0"))((acc, next) =>
        acc.+(next.product.price.*(next.quantity))
      )
    }
  }

}
