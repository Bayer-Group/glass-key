function OIDCUtils()
{
}

OIDCUtils.getKeyId = function(token) {
    var encodedHdr = token.substring(0, token.indexOf('.'));
    var decodedHdr = JSON.parse(b64utoutf8(encodedHdr));
    return decodedHdr.kid;
};

OIDCUtils.listJwksKids = function(jwksUrl) {
    // Return a new promise.
    return new Promise(function(resolve, reject) {
        // Do the usual XHR stuff
        var req = new XMLHttpRequest();
        req.open('GET', jwksUrl);

        req.onload = function() {
            // This is called even on 404 etc
            // so check the status
            if (req.status == 200) {
                // Resolve the promise with the response text
                var keys = JSON.parse(req.response);
                resolve(_.pluck(keys.keys, 'kid'));
            }
            else {
                // Otherwise reject with the status text
                // which will hopefully be a meaningful error
                reject(Error(req.statusText));
            }
        };

        // Handle network errors
        req.onerror = function() {
            reject(Error("Network Error"));
        };

        // Make the request
        req.send();
    });
};

OIDCUtils.getKey = function(jwksUrl, kid) {
    // Return a new promise.
    return new Promise(function(resolve, reject) {
        // Do the usual XHR stuff
        var req = new XMLHttpRequest();
        req.open('GET', jwksUrl);

        req.onload = function() {
            // This is called even on 404 etc
            // so check the status
            if (req.status == 200) {
                // Resolve the promise with the response text
                var keys = JSON.parse(req.response);
                resolve(OIDCUtils.findKey(keys, kid));
            }
            else {
                // Otherwise reject with the status text
                // which will hopefully be a meaningful error
                reject(Error(req.statusText));
            }
        };

        // Handle network errors
        req.onerror = function() {
            reject(Error("Network Error"));
        };

        // Make the request
        req.send();
    });
};

OIDCUtils.findKey = function(arr, kid) {
    return _.find(arr.keys, function(key) {return key.kid == kid;});
};

OIDCUtils.verifyToken = function(token, key) {
    var result = false;
    var rsa = new RSAKey();
    rsa.setPublic(parseBigInt(b64utohex(key.n), 16), parseInt("0x" + b64utohex(key.e)));
    try {
        result = KJUR.jws.JWS.verify(token, rsa);
    } catch (ex) {
        alert("Error: " + ex);
        result = false;
    }
    return result;
};