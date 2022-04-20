import java.io.File

class LoadAppFiles(root: File) {
    //check for app folder
    //check for actions folder
    //check for App.lts file

    val appFolder = File(root, "/app/").let {
        if (it.exists())
            it
        else
            throw NoAppFolderException()
    }
    val actionsFolder = File(root, "/actions/").let {
        if (it.exists())
            it
        else
            throw NoActionsFileException()
    }

    val appFile = File(root, "/app/App.lts").let {
        if (it.exists())
            it
        else
            throw NoAppFileException()
    }

}