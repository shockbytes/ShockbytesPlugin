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

        val document = FileDocumentManager.getInstance().getDocument(file)
        if (document != null) {

            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document)

            val index = StringUtil.indexOf(document.charsSequence, "public class")
            if (index == -1) return
            val lineNumber = document.getLineNumber(index)
            var offset = document.getLineEndOffset(lineNumber - 1)

            val statement = "\n@Module"
            document.insertString(offset, statement)

            offset = document.getLineEndOffset(lineNumber + 1)
            val statementBuilder = StringBuilder()
            statementBuilder.append("\n\n\tprivate Application app;")
            statementBuilder.append("\n\n\tpublic AppModule(Application app) {")
            statementBuilder.append("\n\t\tthis.app = app;\n\t}")
            statementBuilder.append("\n\n\t@Provides\n\t@Singleton")
            statementBuilder.append("\n\tpublic SharedPreferences provideSharedPreferences() {")
            statementBuilder.append("\n\t\treturn PreferenceManager.getDefaultSharedPreferences(app);\n\t}")
            document.insertString(offset, statementBuilder.toString())
        }

    }

    internal fun modifyApplicationFile(file: VirtualFile, project: Project, appName: String) {

        val document = FileDocumentManager.getInstance().getDocument(file)
        if (document != null) {

            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document)

            var index = StringUtil.indexOf(document.charsSequence, "public class " + appName)
            if (index == -1) return
            var lineNumber = document.getLineNumber(index)
            var offset = document.getLineEndOffset(lineNumber) - 1

            val statement = "extends Application "
            document.insertString(offset, statement)

            index = StringUtil.lastIndexOfAny(document.charsSequence, "}")
            lineNumber = document.getLineNumber(index)
            offset = document.getLineEndOffset(lineNumber + -1)
            val stb = StringBuilder()
            stb.append("\n\n\tprivate AppComponent appComponent;\n")
            stb.append("\n\t@Override")
            stb.append("\n\tpublic void onCreate() {")
            stb.append("\n\t\tsuper.onCreate();")
            stb.append("\n\n\t\tappComponent = DaggerAppComponent.builder()")
            stb.append("\n\t\t\t.appModule(new AppModule(this))")
            stb.append("\n\t\t\t.build();\n\t}")
            stb.append("\n\n\tpublic AppComponent getAppComponent() {")
            stb.append("\n\t\treturn appComponent;\n\t}")

            document.insertString(offset, stb.toString())
        }
    }

    internal fun modifyManifest(file: VirtualFile, project: Project, appName: String) {

        val document = FileDocumentManager.getInstance().getDocument(file)
        if (document != null) {

            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document)

            val index = StringUtil.indexOf(document.charsSequence, "<application")
            if (index == -1) return
            val lineNumber = document.getLineNumber(index)
            val offset = document.getLineEndOffset(lineNumber + 1)

            val statement = "\n\t\tandroid:name=\".core.$appName\""
            document.insertString(offset, statement)
        }
    }

    internal fun modifyAppComponent(file: VirtualFile, project: Project) {

        val document = FileDocumentManager.getInstance().getDocument(file)
        if (document != null) {

            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document)

            val index = StringUtil.indexOf(document.charsSequence, "public interface")
            if (index == -1) return
            val lineNumber = document.getLineNumber(index)
            var offset = document.getLineEndOffset(lineNumber - 1)

            var statement = "\n@Singleton\n@Component(modules = {AppModule.class})"
            document.insertString(offset, statement)

            offset = document.getLineEndOffset(lineNumber + 2)
            statement = "\n\n\tvoid inject(MainActivity activity);\n"
            document.insertString(offset, statement)
        }
    }

}
