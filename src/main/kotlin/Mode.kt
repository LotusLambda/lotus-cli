sealed class Mode {
    data class Watch(val watcher: KWatchChannel) : Mode()
    object Run : Mode()
}
