package glasskey.model

/**
 * Created by loande on 6/17/15.
 */
case class ValidatedEntitlementData(username: Option[String],
                                    user_id: Option[String],
                                    orgName: Option[String], applicationentitlements: Either[Set[String], String],
                                    client_id: Option[String],
                                    scopes: Option[Set[String]] = None) extends ValidatedData
