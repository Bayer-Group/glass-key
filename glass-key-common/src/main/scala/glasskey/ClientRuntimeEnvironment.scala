package glasskey

trait ClientRuntimeEnvironment {

  import glasskey.db.DAOService
  import glasskey.model.OAuthAccessToken

  val daoService: DAOService[OAuthAccessToken]
}
