package commands

import utils.Credentials
import utils.NetrcParser

class User {
    val credentials: Credentials?
    init {
        credentials = NetrcParser.instance.getCredentials("test-api.lotuslambda.com")
    }
}