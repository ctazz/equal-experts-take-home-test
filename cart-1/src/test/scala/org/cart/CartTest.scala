package org.cart
import org.cart.Models.{Cart, LineItem, Product}
import org.scalatest.funsuite.AnyFunSuite

import java.util.UUID


class CartTest extends AnyFunSuite {

  def emptyCart = Cart(Vector.empty)
  val doveSoap = Product(UUID.randomUUID(), "Dove Soap", BigDecimal("39.99"))
  val item = LineItem(product = doveSoap, quantity = 5)
  val anotherProduct = Product(UUID.randomUUID(), "another product", BigDecimal("10.00"))
  val anotherLineItem = LineItem(anotherProduct, 2)

  test("empty cart total is 0") {
    assert(emptyCart.total == BigDecimal("0"))
  }

  //only included because this reproduces requirements scenario. Redundant with multiple items test
  test("cart holds the item you have put into it") {
    val cartWithItem = emptyCart.addItem(item)
    assert(cartWithItem.items == Vector(item))
  }

  test("cart can hold multiple items") {
    val cartWithItem = emptyCart.addItem(item).addItem(anotherLineItem)
    assert(cartWithItem.items == Vector(item, anotherLineItem))
  }

  //only included because this reproduces requirements scenario. Redundant with multiple items test
  test("cart gives correct total")  {
    assert(emptyCart.addItem(item).total == BigDecimal("199.95"))
  }

  test("cart gives correct total when holding multiple items") {
    assert(emptyCart.addItem(item).addItem(anotherLineItem).total == BigDecimal("219.95"))
  }



}
