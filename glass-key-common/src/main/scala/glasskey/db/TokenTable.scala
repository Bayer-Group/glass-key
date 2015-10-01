package glasskey.db

object Tables {

  import java.sql.Timestamp

  import scala.slick.driver.H2Driver.simple._

  class TokenTable(tag: Tag) extends Table[(String, String, Option[String], String, Option[Long], Option[String], Timestamp)](tag, "OAUTH_TOKENS") {

    def sessionId: Column[String] = column[String]("SESSION_ID", O.PrimaryKey)

    def tokenHash: Column[String] = column[String]("TOKEN_HASH")

    def idToken: Column[Option[String]] = column[Option[String]]("ID_TOKEN")

    def tokenType: Column[String] = column[String]("TOKEN_TYPE")

    def expiresIn: Column[Option[Long]] = column[Option[Long]]("EXPIRES_IN")

    // The field being put in here needs to be added to current time
    def refreshToken: Column[Option[String]] = column[Option[String]]("REFRESH_TOKEN")

    def insertionTime: Column[Timestamp] = column[Timestamp]("INSERTION_TIME")

    def * = (sessionId, tokenHash, idToken, tokenType, expiresIn, refreshToken, insertionTime)
  }

  val tokenTable = TableQuery[TokenTable]
}
