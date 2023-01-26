package commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.terminal.Terminal
import flatten
import parser.parseComponentListFromText
import parser.utils.componentListToDSL
import java.io.File

class Format : CliktCommand() {


    val t = Terminal()
    val path: String? by option()

    override fun run() {

        // Find app folder
        val dir = File(path ?: ".")
        if (!dir.list().any { it.contains("app") }) {
            t.println(red("Are you sure this is a lotus folder?") + white("No folder named 'app' found in $dir"))
            return;
        }

        // Check components for bugs
        val flatFiles = File(dir, "app").flatten().filter { it.extension == "lts" }
        val filesWithErrors = mutableListOf<String>()

        flatFiles.forEach {
            t.println(white("Parsing ${it.path}"))
            try {
                val components = parseComponentListFromText(it.readText())
                t.println(white("Parsing ${it.path}"))
                val formatted = componentListToDSL(components)
                t.println(white("Formatting ${it.path}"))
                it.writeText(formatted)
            } catch (e: Throwable) {
                t.println(yellow("Failed to parse ${yellow(it.path)}"))
                filesWithErrors.add(it.path)
            }
        }

        if (filesWithErrors.isNotEmpty()) {
            t.println(red("\nFound files with errors:\n"))
            filesWithErrors.forEach {
                t.println(red("${it}"))
            }
        }

        t.println(
            white(
                "\nFormatted " + green("${flatFiles.size - filesWithErrors.size}") + white(" files with ")
                        + red(filesWithErrors.size.toString()) + white(" errors")
            )
        )
        return;

    }
}