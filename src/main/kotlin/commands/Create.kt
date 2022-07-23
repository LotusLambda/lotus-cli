package commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import java.io.File

class Create : CliktCommand("Create a lotus app folder") {

    val name: String? by option().prompt("Name of the project?")

    override fun run() {
        val root = File(".", "$name/")
        root.mkdir()

        val appDir = File(root, "app/")
        appDir.mkdir()
        val actionDir = File(root, "actions/")
        actionDir.mkdir()
        val appFile = File(appDir, "App.lts")
        appFile.createNewFile()
        appFile.writeText(
            """
                App {
                     Route("home"){
                       Text("asdsf")
                     }
                }
            """.trimIndent()
        )
    }
}
