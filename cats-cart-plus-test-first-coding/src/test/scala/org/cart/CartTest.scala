package org.cart
import cats.data.Validated
import org.cart.Models.{ Cart, LineItem, Product, SaleTotal }
import org.scalatest.funsuite.AnyFunSuite

import java.util.UUID

import cats.implicits._

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

  val cartFilledAccordingToRequirementDocs = emptyCart.addItem(doveSoap, 2).addItem(axeDeos, 2)

  def validQuantity(quant: Int): Validated[Vector[String], Int] = Validated.cond(quant > 0, quant, Vector("Quantity must be >= 0"))
  def validPrice(bd: BigDecimal): Validated[Vector[String], BigDecimal] = Validated.cond(bd.compare(Zero) > 0, bd, Vector("price  is <= 0"))
  def validItemName(itemName: String) = Validated.cond(Set("soap", "water").contains(itemName), itemName, Vector(s"$itemName not one of the items we selll"))

  def validProductAndQuantity(prod: Product, quantity: Int): Validated[Vector[String], (Product, Int)] = {
    (validItemName(prod.name), validPrice(prod.price), validQuantity(quantity)).mapN {
      case (name, pr, quant) => Product(prod.id, name, pr) -> quant
    }
  }

  println("validated as a semiGroup. Not very useful. The success values have to be a SemiGroup")
  println(validQuantity(2) |+| validQuantity(2))

  //This fails if any are invalid, and returns all invalid
  def validNum(n: Int) = Validated.cond(n > 0, n, Vector((n, "Quantity must be >= 0")))
  //Using traverse with Validated to return a Vector of (badData, commentAboutBadData)
  println(Vector(-3, -2, -1, 4).traverse(validNum))

  //TODO Can I do anything with a Vector of invalids? I guess I could turn it into a Validated[Vector"
  //I'd like to have a list of all my failures, but all get to keep my successes.
  //Only problem right now is that I still have the Vectors inside their Valid/InValid wrappers
  val listOfValidated = Vector(-3, -2, -1, 4, 5).map(validNum)
  println("Vector showing all the errors")
  println(listOfValidated.filter(_.isInvalid).sequence.fold(identity, _ => Vector.empty))
  println("Vector showing all the successes")
  println(listOfValidated.filter(_.isValid).sequence.fold(_ => Vector.empty, identity))
  //We'll end up having Validated's that return a Vector of BaseError, and BaseError has an identifier field. PriceError, NoPriceFoundForItem, etc.
  //HOW ABOUT a map of Carts, keyed by uuid, some of them filled, and text that we interpret as cartId, itemId, itemName, itemPrice, quantity.
  //We return a Vector of BaseError and a Map of new carts

  test("gives error for price, quantity < 0") {
    assert(
      validProductAndQuantity(Product(UUID.randomUUID(), "soap", BigDecimal(-2.0)), -1).toEither == Left(Vector("price  is <= 0", "Quantity must be >= 0")))
  }

  test("gives correct") {
    val prod = Product(UUID.randomUUID(), "soap", BigDecimal(2.0))
    assert(
      validProductAndQuantity(prod, 1).toEither == Right(
        prod -> 1))
  }

  test("empty cart total is 0") {
    assert(emptyCart.total(salesTax = Zero) == SaleTotal(Zero, Zero, Zero))
  }

  test("empty cart total is 0 even when sales tax is non-zero") {
    assert(emptyCart.total(salesTax = standardSalesTax) == SaleTotal(Zero, Zero, Zero))
  }

  test("cart holds the item you have put into it") {

    val cartWithItem = emptyCart.addItem(doveSoap, 2)
    assert(cartWithItem.items.get(doveSoap) == Some(2))
  }

  test("cart holds correct quantities and prices for multiple products") {
    assert(
      cartFilledAccordingToRequirementDocs.items.get(doveSoap) == Some(2) &&
        cartFilledAccordingToRequirementDocs.items.get(axeDeos) == Some(2))
  }

  test("cart calculates correct subtotal, tax, total") {
    assert(
      cartFilledAccordingToRequirementDocs.total(standardSalesTax) == SaleTotal(BigDecimal("279.96"), BigDecimal("35.00"), BigDecimal("314.96")))
  }

  test("cart combines quantities when two line items referencing the same product are added, and can do so for multiple products," +
    " and calculates correct totals") {
    val cartWithItems = emptyCart.addItems(Map(doveSoap -> 2)).addItems(Map(doveSoap -> 3)).addItems(Map(axeDeos -> 4)).addItems(Map(axeDeos -> 2))
    assert(cartWithItems.items.get(doveSoap) == Some(5)
      && cartWithItems.items.get(axeDeos) == Some(6), "line items not as expected")
    assert(
      cartWithItems.total(standardSalesTax) == SaleTotal(BigDecimal("799.89"), BigDecimal("99.99"), BigDecimal("899.88")),
      "sales total not as expected")
  }
  /*
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

*/

}
