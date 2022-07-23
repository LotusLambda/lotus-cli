package commands

import commands.OS.*
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay
import utils.Credentials
import utils.NetrcParser
import java.net.Socket
import java.time.Duration
import java.util.UUID

class Login : CliktCommand() {

    val t = Terminal()
    val os = userOS()

    override fun run() {
        val user = User()
        println(user.credentials?.user?:"No user")
        val port = findAvailablePort(8000)
        val uuidToAssign = UUID.randomUUID()
        t.println("A link will open in your browser to log you in.")
        t.println("...")
        openBrowser("http://localhost:8081/auth/cli?uuid=$uuidToAssign&port=$port", os)
        var tokenToReceive: String? = null
        var email: String? = null
        t.println("Waiting for the sign...")
        embeddedServer(CIO,port = port) {
            install(WebSockets) {
                pingPeriod = Duration.ofMinutes(60)
                timeout = Duration.ofMinutes(60)
                maxFrameSize = Long.MAX_VALUE
                masking = false
            }

            routing {
                get("/callback") {
                    tokenToReceive = call.request.queryParameters["token"]
                    email = call.request.queryParameters["email"]
                    call.respondText { "You may close the window now." }
                    delay(100)
                    NetrcParser.defaultFile.appendText("machine test-api.lotuslambda.com\nlogin $email\npassword $tokenToReceive")
                    if(tokenToReceive!= null && email!=null)
                        t.println(TextColors.green("Logged in as $email!\n" +
                                TextColors.white(" To logout, type lotus-cli logout, to see your user, type lotus-cli user"))
                        )

                    ShutDownUrl("") { 1 }.doShutdown(call)
                }
            }
        }.start(true)


    }
}


fun findAvailablePort(default: Int): Int {
    return if (portAvailable(default))
        default
    else findAvailablePort(default + 1)
}

fun portAvailable(port: Int) =
    try {
        val ignored = Socket("localhost", port)
        false
    } catch (_ignored: Throwable) {
        true
    }


sealed class OS {
    object Windows : OS()
    object Linux : OS()
    object Mac : OS()
    object Unknown : OS()
}

fun userOS() =
    System.getProperty("os.name").lowercase().let { name ->
        when {
            name.contains("nix") || name.contains("nux") -> Linux
            name.contains("win") -> Windows
            name.contains("mac") -> Mac
            else -> Unknown
        }
    }

fun openBrowser(url: String, os: OS) {
    val runtime = Runtime.getRuntime()
    when (os) {
        Linux -> {
            val browsers = arrayOf(
                "google-chrome", "firefox", "mozilla", "epiphany", "konqueror",
                "netscape", "opera", "links", "lynx"
            )

            val cmd = StringBuffer()
            for (i in browsers.indices) if (i == 0) cmd.append(
                String.format(
                    "%s \"%s\"",
                    browsers[i],
                    url
                )
            ) else cmd.append(
                String.format(
                    " || %s \"%s\"",
                    browsers[i], url
                )
            )
            // If the first didn't work, try the next browser and so on
            runtime.exec(arrayOf("sh", "-c", cmd.toString()))

        }
        Mac -> runtime.exec("open " + url)

        Windows ->
            runtime.exec("rundll32 url.dll,FileProtocolHandler " + url)
        Unknown ->
            println("Unknown OS, visit $url in your browser and login.")
        //unknown, we try linux again
    }
}