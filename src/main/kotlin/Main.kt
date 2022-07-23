import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import commands.Create
import commands.Login
import commands.Run
import commands.Watch
import java.io.File
import java.io.IOException


fun main(args: Array<String>) = Lotus().subcommands(Create(), Run(), Watch(), Login()).main(args)

class Lotus : CliktCommand() {

    override fun run() {
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
