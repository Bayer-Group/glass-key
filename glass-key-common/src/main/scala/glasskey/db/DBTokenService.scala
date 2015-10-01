package glasskey.db

import glasskey.model.OAuthAccessToken

import scala.slick.jdbc.JdbcBackend.Database

class DBTokenService(db: Database) extends DAOService[OAuthAccessToken] {

  import Tables._

  import scala.concurrent.{ExecutionContext, Future}
  import scala.reflect.ClassTag
  import scala.slick.driver.H2Driver.simple._

  override def set(key: String, value: OAuthAccessToken, ttlInSeconds:  Option[Long])(implicit ec: ExecutionContext): Future[Unit] =
    Future.successful {
      db withSession { implicit session =>
        import java.sql.Timestamp
        import java.util.Calendar

        tokenTable.insert((
          key,
          value.access_token,
          value.id_token,
          value.token_type,
          value.expires_in,
          value.refresh_token,
          new Timestamp(Calendar.getInstance().getTime.getTime)))
      }
    }

  override def getAs(key: String)(implicit ct: ClassTag[OAuthAccessToken], ec: ExecutionContext): Future[Option[OAuthAccessToken]] =
    Future.successful {
      db withSession { implicit session =>
        tokenTable.filter(_.sessionId === key).firstOption match {
          case Some(tokenTuple) =>
            Some(new OAuthAccessToken(
              tokenTuple._6,
              tokenTuple._4,
              tokenTuple._5,
              tokenTuple._2,
              tokenTuple._3))
          case None => None
        }
      }
    }

  override def remove(key: String): Future[Unit] =
    Future.successful {
      db withSession { implicit session =>
        tokenTable.filter(_.sessionId === key).delete
      }
    }
}

object DBTokenService {

  private def initializeDB(db: Database) {
    import Tables._

    import scala.slick.driver.H2Driver.simple._
    import scala.slick.jdbc.meta.MTable

    db.withSession { implicit session =>
      if (MTable.getTables("OAUTH_TOKENS").list(session).isEmpty) {
        tokenTable.ddl.create
      }
    }
  }

  def apply(dbName: String): DBTokenService = apply(Database.forConfig(dbName))

  def apply(db: Database): DBTokenService = {
    initializeDB(db)
    new DBTokenService(db)
  }
}
