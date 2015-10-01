Glass Key Spray Client
======================

This project serves as an example of how to use the [Glass Key](https://github.com/MonsantoCo/glass-key) library in a spray client

- Supports Authorization Code, Client Credentials, and Resource Owner grant types (you should do implicit via a browser client, although the resource code in this library will handle the token)
- Intended to be used with the [Glass Key Spray Resource](../glass-key-spray-resource)

Follow these steps to get started:

1. Launch SBT:

        $ sbt ~re-start