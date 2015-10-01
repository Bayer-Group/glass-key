glass-key
=========

This project serves as a library to support [OAuth](https://tools.ietf.org/html/rfc6749) call wrapping for Scala (initially Spray/Play)

- Supports Authorization Code, Client Credentials, and Resource Owner grant types (you should do implicit via a browser client, although the resource code in this library will handle the token)
- Validates a OAuth token against ping (configurable Validators via Dependency Injection)
- Supports OAuth access token present in client http header or query parameter
- Supports [Open ID](http://openid.net/specs/openid-connect-core-1_0.html) token validation/usage if header/param named correctly
- Client will retry 3 times to get a token
- Transforms errors to JSON

For Spray - see [Glass Key Spray Client](samples/glass-key-spray-client) and [Glass Key Spray Resource](samples/glass-key-spray-resource)

For Play - see [Glass Key Play Client](samples/glass-key-play-client) and [Glass Key Play Resource](samples/glass-key-play-resource)


## The Glass Key

Inspiration for the naming of glass-key comes from Star Wars lore. The [Glass Key](http://starwars.wikia.com/wiki/Glass_key) 
was used to activate and control the [Ardana Shadex](http://starwars.wikia.com/wiki/Ardana_Shadex), a huge stone giant capable of mass destruction. The project could 
have been named Ardana Shadex, but that just seemed like too long of a name/too many syllables.

![The Glass Key](http://img4.wikia.nocookie.net/__cb20100113031501/starwars/images/1/17/Glass_key.jpg)