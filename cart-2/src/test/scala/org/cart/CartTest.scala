package org.cart
import org.cart.Models.{Cart, LineItem, Product}
import org.scalatest.funsuite.AnyFunSuite

import java.util.UUID


class CartTest extends AnyFunSuite {

  def emptyCart = Cart(Map.empty)
  val doveIdentifier = UUID.randomUUID()
  val doveSoap = Product(doveIdentifier, "Dove Soap", BigDecimal("39.99"))
  val fiveDoveBars = LineItem(product = doveSoap, quantity = 5)
  val threeDoveBars = LineItem(product = doveSoap, quantity = 3)
  val anotherProduct = Product(UUID.randomUUID(), "another product", BigDecimal("10.00"))
  val anotherLineItem = LineItem(anotherProduct, 2)

  test("empty cart total is 0") {
    assert(emptyCart.total == BigDecimal("0"))
  }

  test("cart holds the item you have put into it") {
    val cartWithItem = emptyCart.addItem(fiveDoveBars)
    assert(cartWithItem.items.get(doveSoap) == Some(fiveDoveBars)   )
  }

  test("cart can hold multiple items") {
    val cart = emptyCart.addItem(fiveDoveBars).addItem(anotherLineItem)
    assert(cart.items.get(doveSoap) == Some(fiveDoveBars)  && cart.items.get(anotherProduct) == Some(anotherLineItem))
  }

  //Test using previous step-1 implementation fails
/*  test("cart combines quantities when two line items referencing the same product are added") {
    val cartWithItems = emptyCart.addItem(fiveDoveBars).addItem(LineItem(product = doveSoap, quantity = 3))
    assert(cartWithItems.items == Vector(LineItem(doveSoap, fiveDoveBars.quantity + threeDoveBars.quantity)))
  }*/

  test("cart combines quantities when two line items referencing the same product are added") {
    val cartWithItems = emptyCart.addItem(fiveDoveBars).addItem(threeDoveBars)
    assert(cartWithItems.items.get(doveSoap) == Some(LineItem(doveSoap, fiveDoveBars.quantity + threeDoveBars.quantity)))
  }

  test("cart gives correct total")  {
    val cart = emptyCart.addItem(fiveDoveBars)
    assert(cart.total == BigDecimal("199.95"))
    assert(cart.addItem(threeDoveBars).total == BigDecimal("319.92"))
  }

  test("cart gives correct total when holding multiple items") {
    assert(emptyCart.addItem(fiveDoveBars).addItem(anotherLineItem).total == BigDecimal("219.95"))
  }



}
