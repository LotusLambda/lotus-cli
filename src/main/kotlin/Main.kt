import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import commands.*
import java.io.File
import java.io.IOException


fun main(args: Array<String>) = Lotus().subcommands(Create(), Run(), Watch(), Login(),
Format(),Deploy()).main(args)

class Lotus : CliktCommand() {

    override fun run() {
    }

}


class NoAppFolderException : IOException()
class NoActionsFileException : IOException()
class NoAppFileException : IOException()


//Flattens directory into a list of files
fun File.flatten(): List<File> {
    if (this.isDirectory) {
        return this.listFiles().flatMap {
            it.flatten()
        }
    } else return listOf(this)
}

//For Linux OS
