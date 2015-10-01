package glasskey

trait ClientRuntimeEnvironment extends RuntimeEnvironment {

  import glasskey.db.DAOService
  import glasskey.model.OAuthAccessToken

  val daoService: DAOService[OAuthAccessToken]
}
