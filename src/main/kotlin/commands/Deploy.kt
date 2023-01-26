package commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.animation.progressAnimation
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.terminal.Terminal
import flatten
import org.rauschig.jarchivelib.ArchiveFormat
import org.rauschig.jarchivelib.Archiver
import org.rauschig.jarchivelib.ArchiverFactory
import org.rauschig.jarchivelib.CompressionType
import parser.parseComponentListFromText
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.nio.file.Path


class Deploy : CliktCommand() {


    val t = Terminal()
    val os = userOS()
    val project: String? by option()
    val path: String? by option()
    val user = User()
    override fun run() {
        val projectFile = File("project_$project.tar.gz")

        //check if logged in
        if (user.credentials == null) {
            t.println(red("You are not logged in. To use 'deploy' command you have to be logged in."))
            t.println(white("Try using " + yellow("'lotus-cli login'") + white("to login")))
        }
        // Find app folder
        val dir = File(path ?: ".")
        if (!dir.list().any { it.contains("app") }) {
            t.println(red("Are you sure this is a lotus folder?") + white("No folder named 'app' found in $dir"))
            return;
        }

        t.println(green("Found folder for project $project") + "\nChecking for errors deployment...")

        // Check components for bugs
        val flatFiles = File(dir, "app").flatten().filter { it.extension == "lts" }
        val filesWithErrors = flatFiles.map {
            try {
                parseComponentListFromText(
                    it.readText()
                )
                it.path to false
            } catch (e: Throwable) {
                it.path to true
            }
        }.filter { it.second }
        if (filesWithErrors.isNotEmpty()) {
            t.println(red("Found files with errors:"))
            filesWithErrors.forEach {
                t.println(gray("${it.first}"))
            }
            return;
        }

        // tar-gz project
        val archiver: Archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP)
        t.println(red(dir.absolutePath))
        archiver.create("${project}_archive",File("archive"),dir)

        // upload project to server
        t.progressAnimation { this.text("Uploading ...") }
        val response = HttpClient.newBuilder().build().send(
            HttpRequest.newBuilder(URI.create("https://test-api.lotuslambda.com/user/deploy"))
                .POST(BodyPublishers.ofFile(Path.of("archive/${project}_archive.tar.gz")))
                .header("Authorization", "Bearer ${user.credentials?.password}")
                .build(), HttpResponse.BodyHandlers.ofString()
        )
        if (response.statusCode() == 200)
            t.println(brightGreen("Deployment complete!"))
        else t.println("Oh no, an error ${response.statusCode()}")
        // receive response
        File("archive/").deleteRecursively()
        return
    }
}