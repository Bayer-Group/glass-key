package glasskey.play.client

import glasskey.config.OAuthConfig


object ClientCredentialsController extends SampleController(PlayClientRuntimeEnvironment("hello-client_credentials-client", new OAuthConfig.Default()))