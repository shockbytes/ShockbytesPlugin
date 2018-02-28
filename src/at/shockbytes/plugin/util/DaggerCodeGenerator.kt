package at.shockbytes.plugin.util

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager

/**
 * Author:  Martin Macheiner
 * Date:    21.02.2017
 */
object DaggerCodeGenerator {

    internal fun modifyAppModule(file: VirtualFile, project: Project) {

        val document = FileDocumentManager.getInstance().getDocument(file) ?: return
        PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document)

        val index = StringUtil.indexOf(document.charsSequence, "class")
        if (index > -1) {

            val lineNumber = document.getLineNumber(index)
            var offset = document.getLineEndOffset(lineNumber - 1)

            val statement = "\n@Module"
            document.insertString(offset, statement)

            val classPrefix = "class AppModule"
            offset = StringUtil.indexOf(document.charsSequence, classPrefix) + classPrefix.length
            document.insertString(offset, "(private val app: Application) ")

            offset = document.getLineEndOffset(lineNumber + 1)
            val statementBuilder = StringBuilder()
            statementBuilder.append("\n\n\t@Provides\n\t@Singleton")
            statementBuilder.append("\n\tfun provideSharedPreferences(): SharedPreferences {")
            statementBuilder.append("\n\t\treturn PreferenceManager.getDefaultSharedPreferences(app)\n\t}")
            document.insertString(offset, statementBuilder.toString())
        }
    }

    internal fun modifyApplicationFile(file: VirtualFile, project: Project, appName: String) {

        val document = FileDocumentManager.getInstance().getDocument(file) ?: return
        PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document)

        var index = StringUtil.indexOf(document.charsSequence, "class " + appName)
        if (index > -1) {

            var lineNumber = document.getLineNumber(index)
            var offset = document.getLineEndOffset(lineNumber) - 1

            val statement = " : Application() "
            document.insertString(offset, statement)

            index = StringUtil.lastIndexOfAny(document.charsSequence, "}")
            lineNumber = document.getLineNumber(index)
            offset = document.getLineEndOffset(lineNumber - 1)
            val stb = StringBuilder()
            stb.append("\n\n\tlateinit var appComponent: AppComponent\n")
            stb.append("\n\t\tprivate set")
            stb.append("\n\toverride fun onCreate() {")
            stb.append("\n\t\tsuper.onCreate()")
            stb.append("\n\n\t\tappComponent = DaggerAppComponent.builder()")
            stb.append("\n\t\t\t.appModule(new AppModule(this))")
            stb.append("\n\t\t\t.build()\n\t}")

            document.insertString(offset, stb.toString())
        }
    }

    internal fun modifyManifest(file: VirtualFile, project: Project, appName: String) {

        val document = FileDocumentManager.getInstance().getDocument(file) ?: return
        PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document)

        val index = StringUtil.indexOf(document.charsSequence, "<application")
        if (index > -1) {
            val lineNumber = document.getLineNumber(index)
            val offset = document.getLineEndOffset(lineNumber + 1)
            val statement = "\n\t\tandroid:name=\".core.$appName\""

            document.insertString(offset, statement)
        }
    }

    internal fun modifyAppComponent(file: VirtualFile, project: Project) {

        val document = FileDocumentManager.getInstance().getDocument(file) ?: return
        PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document)

        val index = StringUtil.indexOf(document.charsSequence, "interface")
        if (index > -1) {
            val lineNumber = document.getLineNumber(index)
            var offset = document.getLineEndOffset(lineNumber - 1)

            var statement = "\n@Singleton\n@Component(modules = [(AppModule::class)])"
            document.insertString(offset, statement)

            offset = document.getLineEndOffset(lineNumber + 2)
            statement = "\n\n\tfun inject(activity: MainActivity)\n"
            document.insertString(offset, statement)
        }
    }

}
