package commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.websocket.*
import java.time.Duration
import java.util.UUID

class Deploy : CliktCommand() {

    val t = Terminal()
    val os = userOS()

    override fun run() {
        val port = findAvailablePort(6000)
        val uuidToAssign = UUID.randomUUID()
        openBrowser("https://https://lotus-editor-frontend.vercel.app/oauth/cli?uuid=$uuidToAssign&port=$port", os)
        var tokenToReceive: String? = null
        var email: String? = null
        embeddedServer(CIO) {
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

                    ShutDownUrl("") { 1 }.doShutdown(call)
                }
            }
        }.start(true)
        if(tokenToReceive!= null && email!=null)
        t.println(TextColors.green("Logged in as $email"))

    }
}