package glasskey.db

/**
 * An interface for the Cache API
 */
trait DAOService[T] {

  import scala.concurrent.{ExecutionContext, Future}
  import scala.reflect.ClassTag

  def set(key: String, value: T, ttlInSeconds:  Option[Long] = None)(implicit ec: ExecutionContext): Future[Unit]

  def getAs(key: String)(implicit ct: ClassTag[T], ec: ExecutionContext): Future[Option[T]]

  def remove(key: String): Future[Unit]
}
