package org.book

import scala.concurrent.Future

trait Warehouse {

  def get(isbn: String): Future[Option[Book]]

}
