package commands

import LoadAppFiles
import Mode
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.terminal.Terminal
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.websocket.*
import loadSessionInSocket
import term.TermQRCode
import watch
import java.io.File
import java.time.Duration

class Watch : CliktCommand() {
    val src: String? by option()
    val port: Int? by option(help = "Port to start the app server at").int()
    val t = Terminal()

    override fun run() {

        val files = LoadAppFiles(File(src ?: "."))

        val watchComponents = files.appFolder.watch()

        val watchActions = files.actionsFolder.watch()

        val portToRunOn = port ?: 8080
        t.println(
            TextStyles.bold(green("Loading app from ${
                white(src?.let {
                    System.getProperty("user.dir").plus("/$it")
                } ?: System.getProperty("user.dir"))
            } ...")))


        val localIp = try {
            val socket = java.net.Socket()
            socket.connect(java.net.InetSocketAddress("google.com", 80))
            socket.getLocalAddress().hostAddress
        } catch (e: Throwable) {
            "0.0.0.0"
        }

        t.println(TextStyles.bold(green("Preparing server at ${white("$localIp:$portToRunOn")}")))

        //parse app file
        val server = embeddedServer(CIO, port = portToRunOn) {
            install(WebSockets) {
                pingPeriod = Duration.ofMinutes(60)
                timeout = Duration.ofMinutes(60)
                maxFrameSize = Long.MAX_VALUE
                masking = false
            }

            routing {

                get("/ping") {
                    call.respond(HttpStatusCode.OK)
                }
                webSocket("/app") {
                    loadSessionInSocket(files, Mode.Watch(watchComponents), t)
                }
            }
        }
        t.println(TermQRCode().generate("connect:::http://$localIp:$portToRunOn/app:::null:::null"))
        t.println(TextStyles.bold(green("Starting server at ${white("$localIp:$portToRunOn")}")))
        server.start(true)
        t.println(TextStyles.bold(green("Started server at ${white("$localIp:$portToRunOn")}")))
    }
}

