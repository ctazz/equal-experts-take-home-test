package org.cart

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import org.book.{ Book, BookStore, Warehouse }
import org.scalamock.scalatest.MockFactory

import scala.concurrent.Future

class BookstoreTest extends AnyFunSuite with ScalaFutures with MockFactory {

  val warehouse: Warehouse = mock[Warehouse]

  //IMPORTANT: When doing test first coding, it helps to start by creating the vals you're going to use.
  //and sometimes even the expectations you'll use. THat way you never have very much non-compiling code.
  //In this test suite, that means creating val book, then creating the book object,
  //And I might have created the warehouse expection before writing BookStore, and maybe even before writing
  //Warehoouse.
  val isbn = "z13"
  val book = Book(isbn, "The Book", Vector("Arthur Author"))

  val bookstore = new BookStore(warehouse)

  def warehouseExpectation(isbn: String, result: Future[Option[Book]]) =
    (warehouse.get(_: String)).expects(isbn).returns(result)

  def search(isbn: String) = {
    whenReady(bookstore.find(isbn)) {
      identity
    }
  }

  test("returns no book when book in the warehouse corresponds to the isbn") {
    warehouseExpectation(isbn, Future.successful(None))
    assert(search(isbn) == None)
  }

  test("returns book when book corresponding to isbn is in the warehouse") {
    warehouseExpectation(isbn, Future.successful(Some(book)))
    assert(search(isbn) == Some(book))
  }

}
