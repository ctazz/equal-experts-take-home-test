package org.cart

import org.cart.Models.{Cart, CartHandler, CartItem, InvalidPrice, InvalidQuantity, Product, ProductError}
import org.scalatest.funsuite.AnyFunSuite

import java.util.UUID

class CartHandlerTest extends AnyFunSuite{

  val doveIdentifier = UUID.randomUUID()
  val axeIdentifier = UUID.randomUUID()
  val doveSoap = Product(doveIdentifier, "Dove Soap", BigDecimal("39.99"))
  val axeDeos = Product(axeIdentifier, "Axe Deos", BigDecimal("99.99"))

  val cartId1 = UUID.randomUUID()
  val cart1 = Cart(Map(doveSoap -> 2))
  val cartMap = Map(cartId1 -> cart1)

  test("carts are unchanged and no errors when empty cartItems ") {
    val expected: Tuple2[Vector[ProductError], Map[UUID, Cart]] = (Vector.empty, cartMap)
    assert(CartHandler.update(cartMap, Vector.empty) == expected)
  }

  test("carts are updated correctly when there are no errors in cartItems") {
    val cartItems = Vector(
       CartItem(cartId1, axeDeos.id, axeDeos.name, axeDeos.price, 2)
    )

    val expectedCart = Cart(cart1.items ++ Map(axeDeos -> 2))
    val expectedReturn: Tuple2[Vector[ProductError], Map[UUID, Cart]] = (Vector.empty, Map(cartId1 -> expectedCart))
    assert(CartHandler.update(cartMap, cartItems) == expectedReturn)
  }

  test("carts are updated and errors are returned") {
    val idForQuantityErrorCartItem = UUID.randomUUID()
    val idForPriceErrorCartItem =  UUID.randomUUID()
    val cartItems = Vector(
      CartItem(cartId1, idForQuantityErrorCartItem, axeDeos.name, axeDeos.price, -2),
      CartItem(cartId1, axeDeos.id, axeDeos.name, axeDeos.price, 2),
        CartItem(cartId1, idForPriceErrorCartItem, axeDeos.name, BigDecimal("-1"), 1),
    )

    val expectedErrors = Vector(InvalidQuantity(cartId1, idForQuantityErrorCartItem), InvalidPrice(cartId1, idForPriceErrorCartItem ) )
    val expectedCart = Cart(cart1.items ++ Map(axeDeos -> 2))
    val expectedCarts = Map(cartId1 -> expectedCart)
    val (errors, carts) = CartHandler.update(cartMap, cartItems)

    assert(carts == expectedCarts, "carts failed")
    assert(errors == expectedErrors, "errors failed")

  }

}
