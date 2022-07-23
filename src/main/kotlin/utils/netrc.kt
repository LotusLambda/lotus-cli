package utils

import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class Credentials(
    var user: String? = null,
    var password: String? = null,
    var host: String? = null)

class NetrcParser private constructor(netrc: File) {
    private enum class ParseState {
        START, REQ_KEY, REQ_VALUE, MACHINE, LOGIN, PASSWORD, MACDEF, END
    }

    private val netrc: File
    private var lastModified: Long = 0
    private val hosts: MutableMap<String, Credentials> = HashMap()

    // Pattern for detecting comments
    private val commentPattern: Pattern = Pattern.compile("(^|\\s)#\\s")

    fun getCredentials(host: String): Credentials? {
        if (!netrc.exists()) return null
        if (lastModified != netrc.lastModified()) parse()
        return hosts[host]
    }

    init {
        this.netrc = netrc
    }

    private fun parse(): NetrcParser {
        if (!netrc.exists()) return this
        hosts.clear()
        lastModified = netrc.lastModified()
        try {
            BufferedReader(InputStreamReader(Files.newInputStream(netrc.toPath()), Charset.defaultCharset())).use { r ->
                var line: String? = null
                var machine: String? = null
                var login: String? = null
                var password: String? = null
                var state = ParseState.START
                val commentMatcher: Matcher =
                    commentPattern.matcher("") // Matcher to remove comments on each line before parsing
                val matcher: Matcher = NETRC_TOKEN.matcher("")
                while (r.readLine().also {
                        line = it } != null) {
                    line = line?.trim { it <= ' ' }
                    if (line?.isEmpty()==true) {
                        if (state == ParseState.MACDEF) {
                            state = ParseState.REQ_KEY
                        }
                        continue
                    }

                    // Remove comments before paring
                    commentMatcher.reset(line)
                    if (commentMatcher.find()) {
                        // We found a comment, so truncate the string from that point on and clean it up
                        line = line?.substring(0, commentMatcher.start())?.trim { it <= ' ' }
                    }
                    matcher.reset(line)
                    while (matcher.find()) {
                        val match: String = matcher.group()
                        when (state) {
                            ParseState.START -> if ("machine" == match) {
                                state = ParseState.MACHINE
                            }
                            ParseState.REQ_KEY -> state = if ("login" == match) {
                                ParseState.LOGIN
                            } else if ("password" == match) {
                                ParseState.PASSWORD
                            } else if ("macdef" == match) {
                                ParseState.MACDEF
                            } else if ("machine" == match) {
                                ParseState.MACHINE
                            } else {
                                ParseState.REQ_VALUE
                            }
                            ParseState.REQ_VALUE -> state = ParseState.REQ_KEY
                            ParseState.MACHINE -> {
                                if (machine != null && login != null && password != null) {
                                    hosts[machine] = Credentials(login,password,machine)
                                }
                                machine = match
                                login = null
                                password = null
                                state = ParseState.REQ_KEY
                            }
                            ParseState.LOGIN -> {
                                login = match
                                state = ParseState.REQ_KEY
                            }
                            ParseState.PASSWORD -> {
                                password = match
                                state = ParseState.REQ_KEY
                            }
                            ParseState.MACDEF -> {}
                        }
                    }
                }
                if (machine != null) {
                    if (login != null && password != null) {
                        hosts[machine] = Credentials(
                            login,password,machine)
                    }
                }
            }
        } catch (e: IOException) {
            throw InvalidPropertiesFormatException("Invalid netrc file: '" + netrc.getAbsolutePath() + "'")
        }
        return this
    }

    companion object {
        private val NETRC_TOKEN: Pattern = Pattern.compile("(\\S+)")

        /**
         * getInstance.
         *
         * @return a [NetrcParser] object.
         */
        val instance: NetrcParser
            get() {
                val netrc: File = defaultFile
                return getInstance(netrc)
            }

        /**
         * getInstance.
         *
         * @param netrcPath a [java.lang.String] object.
         * @return a [NetrcParser] object.
         */
        fun getInstance(netrcPath: String?): NetrcParser? {
            val netrc = File(netrcPath)
            return if (netrc.exists()) getInstance(File(netrcPath)) else null
        }

        /**
         * getInstance.
         *
         * @param netrc a [java.io.File] object.
         * @return a [NetrcParser] object.
         */
        fun getInstance(netrc: File): NetrcParser {
            return NetrcParser(netrc).parse()
        }

        // windows variant
        val defaultFile: File
            get() {
                val home = File(System.getProperty("user.home"))
                var netrc = File(home, ".netrc")
                println("$netrc file")
                println("${netrc.absolutePath}")
                if (!netrc.exists()) netrc = File(home, "_netrc") // windows variant
                return netrc
            }
    }
}
