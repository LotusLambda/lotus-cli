import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.TermUi.echo
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.terminal.Terminal
import com.lotuslambda.flowmachine.engine.*
import io.ktor.network.sockets.*
import io.ktor.server.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import term.TermQRCode
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.URL
import java.time.Duration


fun main(args: Array<String>) = Lotus()
    .subcommands(Create(), Run(), Watch())
    .main(args)

class Lotus : CliktCommand() {

    override fun run() {
    }

}

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
            TextStyles.bold(
                green(
                    "Loading app from ${
                        white(src?.let {
                            System.getProperty("user.dir").plus("/$it")
                        } ?: System.getProperty("user.dir"))
                    } ..."
                )
            )
        )


        val localIp = try {
            val socket = java.net.Socket()
            socket.connect(java.net.InetSocketAddress("google.com", 80))
            socket.getLocalAddress().hostAddress
        } catch (e: Throwable) {
            "0.0.0.0"
        }

        t.println(TextStyles.bold(green("Starting server at ${white("$localIp:$portToRunOn")}")))

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
                    loadSessionInSocket(files, watchComponents,t)
                    t.println(green("Loaded session in socket."))
                }
            }
            println(TermQRCode().generate("connect:::http://$localIp:$portToRunOn/app:::null:::null"))
        }
        server.start(true)

    }


}


class NoAppFolderException : IOException()
class NoActionsFileException : IOException()
class NoAppFileException : IOException()


fun File.flatten(): List<File> {
    if (this.isDirectory) {
        return this.listFiles().flatMap {
            it.flatten()
        }
    } else return listOf(this)
}

//For Linux OS
