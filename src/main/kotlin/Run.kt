import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.mordant.terminal.Terminal
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import java.io.File
import java.time.Duration
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import term.TermQRCode

class Run : CliktCommand() {
    val src: String? by option()
    val port: Int? by option(help = "Port to start the app server at").int()
    val t = Terminal()

    override fun run() {

        val portToRunOn = port?:8080
        t.println(bold(green("Loading app from ${white(src?.let {
            System.getProperty("user.dir").plus("/$it")
        }?:System.getProperty("user.dir"))} ...")))

        val files = LoadAppFiles(File(src ?: "."))

        val localIp = try {
            val socket = java.net.Socket()
            socket.connect(java.net.InetSocketAddress("google.com", 80))
            socket.getLocalAddress().hostAddress
        } catch (e: Throwable){
            "0.0.0.0"
        }

        t.println(bold(green("Starting server at ${white("$localIp:$portToRunOn")}")))

        //parse app file
        val server = embeddedServer(CIO, port = portToRunOn) {
            install(WebSockets) {
                pingPeriod = Duration.ofMinutes(60)
                timeout = Duration.ofMinutes(60)
                maxFrameSize = Long.MAX_VALUE
                masking = false
            }

            routing {
                webSocket("/app") {
                    val app = loadSessionInSocket(
                        files,null,t
                    )
                }
            }
        }
        server.start(true)
        println(TermQRCode().generate("app:::$localIp:$portToRunOn"))
    }

}
