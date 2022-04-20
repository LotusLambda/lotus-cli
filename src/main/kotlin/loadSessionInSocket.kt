import com.github.ajalt.clikt.output.TermUi.echo
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal
import com.lotuslambda.flowmachine.engine.*
import com.lotuslambda.flowmachine.engine.dependencies.ProjectDependencyResolver
import com.lotuslambda.flowmachine.engine.state.StateSnapshot
import com.lotuslambda.flowmachine.engine.state.toJsonElement
import io.ktor.http.ContentType.Application.Json
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import parser.models.ComponentSpecification
import parser.utils.componentToDSL
import java.io.File

suspend fun DefaultWebSocketServerSession.loadSessionInSocket(
    files: LoadAppFiles, watchComponents: KWatchChannel?, t: Terminal
) {

    println("Connecting to session...")
    val app = buildEngine {
        appConfig {
            files.appFile.readText()
        }
        files {
            files.appFolder.flatten()
                .map { EngineFileData(it.name, it.path, it.readText()) }
                .toTypedArray()
        }
        resolver {
            object : ProjectDependencyResolver() {
                override fun fileFromPath(uri: String): String {
                    return File(uri).readText()
                }
            }
        }
        renderer {
            object : Render {
                val json = Json {  }
                override fun render(componentSpecification: ComponentSpecification, stateSnapshot: StateSnapshot) {
                    launch {
                        println("LotusLog: Sent a frame  at ${System.currentTimeMillis()}")
                        send(Frame.Text("render:${componentToDSL(componentSpecification)}"))
                    }
                }

                override fun close() {
                }

                override fun render(specification: String) {
                    launch {
                        println("LotusLog: Sent a frame  ${System.currentTimeMillis()}")
                        send(Frame.Text("render:${specification}"))
                    }
                }

                override fun stateUpdate(state: StateSnapshot) {
                    launch {
                        println("LotusLog: Sent a frame of state ${System.currentTimeMillis()}")
                        val txt = "state:${json.encodeToString(state.state.toJsonElement())}"
                        send(Frame.Text(txt))
                    }
                }

            }
        }
        actionHandler {
            object : ActionHandler {
                override fun close() {
                    TODO("Not yet implemented")
                }

                override fun execute(action: Action.Invoked, state: StateSnapshot) {
                    TODO("Not yet implemented")
                }
            }
        }

    }

    launch {
        watchComponents?.listen()
            ?.filter { it.file.isFile }
            ?.collect {
                t.println(TextColors.green("Reloading ${it.file.path} to $app"))
                println(it.file.readText())
                app?.execute(Action.Render(it.file.readText(), emptyMap()))
            }

    }
    incoming.receiveAsFlow()
        .onCompletion {
            println("Socket session flow completed ${it?.stackTraceToString()}")
        }
        .catch { println("Socket session flow error ${it.stackTraceToString()}") }
        .collect { frame ->
            when (frame) {
                is Frame.Text -> {
                    val text = frame.readText()
                    val (cmd, afterCmd) = text.splitByDelimiter()
                    println("msg")
                    when (cmd) {
                        "received" -> {
                            println("Received received at ${System.currentTimeMillis()}")
                        }
                        "connect" -> {
                            // connect:appId:sessionId:
                            val (appId, afterAppId) = afterCmd.splitByDelimiter()
                            val (sessionId, rest) = afterAppId.splitByDelimiter()
                        }
                        "resume" -> {
                            // resume:appId:sessionId:
                            val (appId, afterAppId) = afterCmd.splitByDelimiter()
                            val (sessionId, rest) = afterAppId.splitByDelimiter()
                        }

                        "close" -> {
                            // close:appId:sessionId:
                            val (appId, afterAppId) = afterCmd.splitByDelimiter()
                            val (sessionId, rest) = afterAppId.splitByDelimiter()

                        }
                        "route", "push", "replace", "pop" -> {
                            /* route:/foo/bar/ */
                            val route = afterCmd
                            app.execute(Action.RouteOperation(cmd, route, route, ""))
                        }
                        "set" -> {
                            // set:data:

                            fun JsonElement.toMapOfAny(): Any {
                                return when (this) {
                                    is JsonObject -> entries.map {
                                        it.key to it.value.toMapOfAny()
                                    }
                                    is JsonArray -> jsonArray.map {
                                        it.toMapOfAny()
                                    }
                                    is JsonPrimitive -> content
                                }
                            }

                            val (data, rest) = afterCmd.splitByDelimiter()
                            val map = kotlinx.serialization.json.Json.parseToJsonElement(data).jsonObject
                                .map {
                                    it.key to it.value.toMapOfAny()
                                }
                            app.execute(Action.SetState(data, map.toMap()))

                        }
                        "action" -> {
                            // action:actionName:route:{data}
                            val (actionName, after) = afterCmd.splitByDelimiter()
                            println("Got action $actionName:$after $actionName with")
                            when (actionName) {
                                "route" -> {
                                    val (route, data) = after.splitByDelimiter()
                                    println("Got data $data")
                                    println(kotlinx.serialization.json.Json.parseToJsonElement(data).jsonObject.toMap())
                                    app.execute(Action.Route(route, data))
                                }
                                "router" -> {
                                    val (op, args) = after.splitByDelimiter()
                                    app.execute(Action.RouteOperation(op, args))
                                }
                                else -> {
                                    app.execute(
                                        Action.Invoked(
                                            actionName, "",
                                            kotlinx.serialization.json.Json.parseToJsonElement(after).jsonObject.toMap()
                                        )
                                    )

                                }
                            }
                        }
                    }

                }
                else -> {
                    println("Received: $frame")
                }
            }
    }
}

fun String.splitByDelimiter(): Pair<String, String> {
    val cmdIndex = indexOfFirst { it == ':' }
    val left = take(cmdIndex)
    val right = drop(cmdIndex + 1)
    return left to right
}

suspend fun <A, B> Iterable<A>.pmap(f: suspend (A) -> B): List<B> = coroutineScope {
    map { async { f(it) } }.awaitAll()
}
