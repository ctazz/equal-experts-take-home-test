package org.book

import scala.concurrent.Future

class BookStore(warehouse: Warehouse) {

  def find(isbn: String): Future[Option[Book]] = {
    warehouse.get(isbn)
  }

}
