import FileEvent.*
import com.sun.nio.file.SensitivityWatchEventModifier
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onSubscription
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.nio.file.WatchKey
import java.nio.file.FileVisitResult
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.SimpleFileVisitor
import java.nio.file.Files
import java.nio.file.StandardWatchEventKinds.*
import kotlin.io.path.isRegularFile

fun File.watch(
    extension: String? = null,
    tag: Any? = null,
    scope: CoroutineScope = GlobalScope
) = KWatchChannel(
    file = this,
    scope = scope,
    tag = tag
)

/**
 * Channel based wrapper for Java's WatchService
 *
 * @param [file] - file or directory that is supposed to be monitored by WatchService
 * @param [scope] - CoroutineScope in within which Channel's sending loop will be running
 * @param [mode] - channel can work in one of the three modes: watching a single file,
 * watching a single directory or watching directory tree recursively
 * @param [tag] - any kind of data that should be associated with this channel, optional
 */
class KWatchChannel(
    val file: File,
    val extension: String? = null,
    val scope: CoroutineScope,
    val tag: Any? = null,
) {

    private val flow = MutableStateFlow<FileEvent>(Initialized(file, tag))

    private val watchService: WatchService = FileSystems.getDefault().newWatchService()
    private val registeredKeys = ArrayList<WatchKey>()
    private val path: Path = if (file.isFile) {
        file.parentFile
    } else {
        file
    }.toPath()

    fun listen() = flow.asSharedFlow().onSubscription {
        println("Subscribed")
    }.onEach {
        println("$it")
    }

    private fun registerPaths() {
        registeredKeys.apply {
            forEach { it.cancel() }
            clear()
        }
        Files.walkFileTree(path, object : SimpleFileVisitor<Path>() {
            override fun preVisitDirectory(subPath: Path, attrs: BasicFileAttributes): FileVisitResult {
                if (extension != null && subPath.isRegularFile())
                    if (!subPath.endsWith(extension))
                        return FileVisitResult.CONTINUE

                registeredKeys += subPath.register(watchService,arrayOf(ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE),
                    SensitivityWatchEventModifier.HIGH)
                return FileVisitResult.CONTINUE
            }
        })
    }

    init {
        // commence emitting events from channel
        scope.launch(Dispatchers.IO) {

            // sending channel initalization event
            var shouldRegisterPath = true

            while (isActive) {

                if (shouldRegisterPath) {
                    registerPaths()
                    shouldRegisterPath = false
                }

                val monitorKey = watchService.take()
                val dirPath = monitorKey.watchable() as? Path ?: break
                monitorKey.pollEvents().forEach {
                    val eventPath = dirPath.resolve(it.context() as Path)
                    println("Happened ${it.kind().name()} - ${it.context().toString()}")

                    val event = when (it.kind()) {
                        ENTRY_CREATE -> Created(
                            file = eventPath.toFile(),
                            tag = tag,
                        )
                        ENTRY_DELETE -> Deleted(
                            file = eventPath.toFile(),
                            tag = tag,
                        )
                        else -> Modified(
                            file = eventPath.toFile(),
                            tag = tag,
                        )
                    }

                    // if any folder is created or deleted... and we are supposed
                    // to watch subtree we re-register the whole tree
                    if ((event is Created || event is Deleted) &&
                        event.file.isDirectory
                    ) {
                        shouldRegisterPath = true
                    }

                    println("Setting value to ${event.toString()}")
                    flow.value = event;
                }

                if (!monitorKey.reset()) {
                    monitorKey.cancel()
                    close(IOException())
                    break
                } else if (!isActive) {
                    break
                }
            }
        }
    }

    fun close(cause: Throwable?): Boolean {
        registeredKeys.apply {
            forEach { it.cancel() }
            clear()
        }
        return true;
    };

}

sealed class FileEvent(
    val file: File,
    val tag: Any?
) {
    class Initialized(file: File, tag: Any?) : FileEvent(file, tag)
    class Created(file: File, tag: Any?) : FileEvent(file, tag)
    class Modified(file: File, tag: Any?) : FileEvent(file, tag)
    class Deleted(file: File, tag: Any?) : FileEvent(file, tag)
}