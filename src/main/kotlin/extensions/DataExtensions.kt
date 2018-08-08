package extensions

import bean.Element
import bean.ViewInfo
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiField
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPsiFactory
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.util.*

/**
 * 复制字符串到剪贴板
 */
fun String.toClipboard() {
    val clip = Toolkit.getDefaultToolkit().systemClipboard
    val tText = StringSelection(this)
    clip.setContents(tText, null)
}

/**
 * 首字母大写
 */
fun String.firstToUpperCase(): String {
    return substring(0, 1).toUpperCase(Locale.getDefault()) + substring(1)
}

/**
 * Element to ViewInfo
 */
fun List<Element>.toViewInfoList() = this.map { ViewInfo(true, it) }

/**
 * 生成Kotlin代码
 */
fun List<ViewInfo>.gengrateKTCode(addM: Boolean, isPrivate: Boolean, rootView: String): String {
    return this.filter { it.isChecked }.joinToString("\n") { it.getKTString(addM, isPrivate, rootView) }
}

/**
 * 生成Kotlin ViewHolder代码
 */
fun List<ViewInfo>.gengrateKTViewHolderCode(layoutName: String, viewHolderName: String, addM: Boolean, isPrivate: Boolean): String {

    val fieldStr = this.filter { it.isChecked }.joinToString("\n") { it.getKTString(addM, isPrivate, "itemView") }

    return """
internal class ${viewHolderName}ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

$fieldStr

    companion object {

        fun newInstance(parent: ViewGroup): ${viewHolderName}ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.$layoutName, parent, false)
            return ${viewHolderName}ViewHolder(view)
        }
    }

}
    """.trimIndent()
}

/**
 * 生成Java代码
 */
fun List<ViewInfo>.gengrateJavaCode(addM: Boolean, rootView: String, isPrivate: Boolean, isTarget26: Boolean): String {

    val infos = this.filter { it.isChecked }

    val fieldStr = infos.joinToString("\n") { it.getJavaFieldString(addM, isPrivate) }
    val findViewStr = infos.joinToString("\n") { it.getJavaFindViewString(addM, isTarget26, rootView) }

    return "$fieldStr\n\n$findViewStr"
}

/**
 * 生成Java viewHolder代码
 */
fun List<ViewInfo>.gengrateJavaViewHolderCode(layoutName: String, viewHolderName: String, addM: Boolean, isPrivate: Boolean, isTarget26: Boolean): String {

    val infos = this.filter { it.isChecked }

    val fieldStr = infos.joinToString("\n") { it.getJavaFieldString(addM, isPrivate) }
    val findViewStr = infos.joinToString("\n") { it.getJavaFindViewString(addM, isTarget26, "itemView") }

    return """
static class ${viewHolderName}ViewHolder extends RecyclerView.ViewHolder {

    public static ${viewHolderName}ViewHolder newInstance(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.$layoutName, parent, false);
        return new ${viewHolderName}ViewHolder(view);
    }

    $fieldStr

    private ${viewHolderName}ViewHolder(View itemView) {
        super(itemView);
        $findViewStr
    }

}
    """.trimIndent()
}