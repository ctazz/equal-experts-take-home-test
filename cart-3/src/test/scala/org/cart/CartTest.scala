package org.cart
import org.cart.Models.{Cart, SaleTotal, LineItem, Product}
import org.scalatest.funsuite.AnyFunSuite

import java.util.UUID


class CartTest extends AnyFunSuite {

  def emptyCart = Cart(Map.empty)
  val doveIdentifier = UUID.randomUUID()
  val axeIdentifier = UUID.randomUUID()
  val doveSoap = Product(doveIdentifier, "Dove Soap", BigDecimal("39.99"))
  val axeDeos = Product(axeIdentifier, "Axe Deos", BigDecimal("99.99"))
  val twoDoveBars = LineItem(product = doveSoap, quantity = 2)
  val threeDoveBars = LineItem(product = doveSoap, quantity = 3)
  val twoAxeDeos = LineItem(product = axeDeos, quantity = 2)
  val fourAxeDeos = LineItem(product = axeDeos, quantity = 4)

  val standardSalesTax = BigDecimal("0.125")
  val Zero = BigDecimal("0")

  val cartFilledAccordingToRequirementDocs = emptyCart.addItem(twoDoveBars).addItem(twoAxeDeos)

  test("empty cart total is 0") {
    assert(emptyCart.total(salesTax = Zero) == SaleTotal(Zero, Zero, Zero))
  }

  test("empty cart total is 0 even when sales tax is non-zero") {
    assert(emptyCart.total(salesTax = standardSalesTax) == SaleTotal(Zero, Zero, Zero))
  }

  test("cart holds the item you have put into it") {
    val cartWithItem = emptyCart.addItem(twoDoveBars)
    assert(cartWithItem.items.get(doveSoap) == Some(twoDoveBars)   )
  }

  test("cart holds correct quantities and prices for multiple products") {
    assert(
    cartFilledAccordingToRequirementDocs.items.get(doveSoap) == Some(twoDoveBars) &&
      cartFilledAccordingToRequirementDocs.items.get(axeDeos) == Some(twoAxeDeos)
    )
  }

  test("cart calculates correct subtotal, tax, total") {
    assert(
    cartFilledAccordingToRequirementDocs.total(standardSalesTax) == SaleTotal(BigDecimal("279.96"), BigDecimal("35.00"), BigDecimal("314.96"))
    )
  }

  test("cart combines quantities when two line items referencing the same product are added, and can do so for multiple products," +
    " and calculates correct totals") {
    val cartWithItems = emptyCart.addItem(twoDoveBars).addItem(threeDoveBars).addItem(fourAxeDeos).addItem(twoAxeDeos)
    assert(cartWithItems.items.get(doveSoap) == Some(LineItem(doveSoap, twoDoveBars.quantity + threeDoveBars.quantity))
      && cartWithItems.items.get(axeDeos) == Some(LineItem(axeDeos, fourAxeDeos.quantity + twoAxeDeos.quantity)), "line items not as expected"
    )
    assert(cartWithItems.total(standardSalesTax) == SaleTotal(BigDecimal("799.89"), BigDecimal("99.99"), BigDecimal("899.88")),
      "sales total not as expected")
  }

  test("cart allows zeroing out of a line item by adding a line item for the same product that negates the original quantity") {
    val cart = emptyCart.addItem(twoDoveBars).
      addItem(twoDoveBars.copy(quantity = twoDoveBars.quantity * -1))
    assert(cart.items.isEmpty)
  }

  test("cart does not hold items with negative quantity") {
    assert(emptyCart.addItem(twoDoveBars.copy(quantity = twoDoveBars.quantity * -1)).items.isEmpty)
  }

  test("cart allows credit to user, and does not apply sales tax on negative sale total") {
    val credit = Product(UUID.randomUUID(), "Credit-- returned radios", BigDecimal("-70.00"))
    val creditLineItem = LineItem(credit, 3)
    val cart = emptyCart.addItem(creditLineItem)

    val expectedTotal = credit.price * creditLineItem.quantity
    assert(
      cart.total(standardSalesTax) == SaleTotal(subtotal = expectedTotal, tax = Zero, total = expectedTotal)
    )
  }

  val cartHoldsAPenny = emptyCart.addItem(LineItem(product = Product(UUID.randomUUID(), "penny-product", BigDecimal("0.01")), quantity = 1))

  test("cart tax should round down for < .5 cents") {
    assert(cartHoldsAPenny.total(BigDecimal(".49")).tax == Zero)
  }

  test("cart tax should round up for >= .5 cents") {
    assert(cartHoldsAPenny.total(BigDecimal("0.5")).tax == BigDecimal("0.01"))
  }



}
