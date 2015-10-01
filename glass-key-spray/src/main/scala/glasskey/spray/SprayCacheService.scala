package glasskey.spray

import glasskey.db.DAOService

class SprayCacheService[T] extends DAOService[T] {

  import spray.caching.{Cache, LruCache}

  import scala.concurrent.duration.{FiniteDuration, HOURS}
  import scala.concurrent.{ExecutionContext, Future}
  import scala.reflect.ClassTag

  //this will actually create a ExpiringLruCache and hold data for 48 hours
  val tokenCache: Cache[T] = LruCache(timeToLive = new FiniteDuration(48, HOURS))

  override def set(key: String, value: T, ttlInSeconds: Option[Long])(implicit ec: ExecutionContext): Future[Unit] =
    Future.successful(tokenCache(key, () => Future(value)))

  override def getAs(key: String)(implicit ct: ClassTag[T], ec: ExecutionContext): Future[Option[T]] =
    tokenCache.get(key).map(f => f.map(Option(_))).getOrElse(Future.successful(None))

  override def remove(key: String): Future[Unit] =
    Future.successful(tokenCache.remove(key))
}
