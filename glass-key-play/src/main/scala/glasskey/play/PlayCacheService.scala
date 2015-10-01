package glasskey.play

import glasskey.db.DAOService

class PlayCacheService[T] extends DAOService[T] {
  import play.api.Play.current
  import play.api.cache.Cache

  import scala.concurrent.{ExecutionContext, Future}
  import scala.reflect.ClassTag

  override def set(key: String, value: T, ttlInSeconds: Option[Long])(implicit ec: ExecutionContext): Future[Unit] =
    Future.successful(Cache.set(key, value, if (ttlInSeconds.isDefined) ttlInSeconds.get.toInt else 4))

  override def getAs(key: String)(implicit ct: ClassTag[T], ec: ExecutionContext): Future[Option[T]] =
    Future.successful(Cache.getAs[T](key))

  override def remove(key: String): Future[Unit] =
    Future.successful(Cache.remove(key))
}
