package glasskey.model.fetchers

trait AccessTokenFetcher[T] {
  import glasskey.model.ProtectedResourceRequest

  def matches(request: ProtectedResourceRequest): Boolean

  def fetch(request: ProtectedResourceRequest): T
}
