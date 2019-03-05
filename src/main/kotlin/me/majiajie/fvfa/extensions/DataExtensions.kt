package me.majiajie.fvfa.extensions

import me.majiajie.fvfa.bean.Element
import me.majiajie.fvfa.bean.ViewInfo
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

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
    val ch = this.toCharArray()
    if (ch[0] in 'a'..'z') {
        ch[0] = (ch[0].toInt() - 32).toChar()
    }
    return String(ch)
}

/**
 * Element to ViewInfo
 */
fun List<Element>.toViewInfoList() = this.map { ViewInfo(true, it) }

/**
 * 生成Kotlin代码
 */
fun List<ViewInfo>.generateKTCode(addM: Boolean, isPrivate: Boolean, isLocalVariable: Boolean, rootView: String): String {
    return this.filter { it.isChecked }.joinToString("\n") {
        if (isLocalVariable)
            it.getKTLocalVariableString(addM, rootView)
        else
            it.getKTString(addM, isPrivate, rootView)
    }
}

/**
 * 生成Kotlin ViewHolder代码
 */
fun List<ViewInfo>.generateKTViewHolderCode(layoutName: String, viewHolderName: String, addM: Boolean, isPrivate: Boolean): String {

    val fieldStr = this.filter { it.isChecked }.joinToString("\n    ") { it.getKTString(addM, isPrivate, "itemView") }

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
 * 生成Kotlin Epoxy model代码
 */
fun List<ViewInfo>.generateKTEpoxyModelCode(layoutName: String, modelName: String, addM: Boolean): String {

    val fieldStr = this.filter { it.isChecked }.joinToString("\n\t") { it.getKTString(addM, false, "itemView") }

    return """
@EpoxyModelClass(layout = R.layout.$layoutName)
abstract class ${modelName}Model : EpoxyModelWithHolder<${modelName}Model.${modelName}ViewHolder>() {

    override fun bind(holder: ${modelName}ViewHolder) {

    }

    class ${modelName}ViewHolder : EpoxyHolder() {

        lateinit var itemView: View
        $fieldStr

        override fun bindView(itemView: View) {
            this.itemView = itemView
        }
    }

}
    """.trimIndent()
}

/**
 * 生成Java代码
 */
fun List<ViewInfo>.generateJavaCode(addM: Boolean, rootView: String, isPrivate: Boolean, isTarget26: Boolean, isLocalVariable: Boolean = false): String {
    val infos = this.filter { it.isChecked }
    return if (isLocalVariable) {
        infos.joinToString("\n") { it.getJavaLocalVariableString(addM, isTarget26, rootView) }
    } else {
        val fieldStr = infos.joinToString("\n") { it.getJavaFieldString(addM, isPrivate) }
        val findViewStr = infos.joinToString("\n") { it.getJavaFindViewString(addM, isTarget26, rootView) }
        "$fieldStr\n\n$findViewStr"
    }
}

/**
 * 生成Java ViewHolder代码
 */
fun List<ViewInfo>.generateJavaViewHolderCode(layoutName: String, viewHolderName: String, addM: Boolean, isPrivate: Boolean, isTarget26: Boolean): String {

    val infos = this.filter { it.isChecked }

    val fieldStr = infos.joinToString("\n    ") { it.getJavaFieldString(addM, isPrivate) }
    val findViewStr = infos.joinToString("\n\t") { it.getJavaFindViewString(addM, isTarget26, "itemView") }

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

/**
 * 生成Java Epoxy model代码
 */
fun List<ViewInfo>.generateJavaEpoxyModelCode(layoutName: String, modelName: String, addM: Boolean, isPrivate: Boolean, isTarget26: Boolean): String {

    val infos = this.filter { it.isChecked }

    val fieldStr = infos.joinToString("\n\t") { it.getJavaFieldString(addM, isPrivate) }
    val findViewStr = infos.joinToString("\n\t    ") { it.getJavaFindViewString(addM, isTarget26, "itemView") }

    return """
@EpoxyModelClass(layout = R.layout.$layoutName)
public abstract class ${modelName}Model extends EpoxyModelWithHolder<${modelName}Model.${modelName}ViewHolder> {

    @Override
    public void bind(@NonNull ${modelName}ViewHolder holder) {

    }

    static class ${modelName}ViewHolder extends EpoxyHolder {

        $fieldStr

        @Override
        protected void bindView(@NonNull View itemView) {
            $findViewStr
        }
    }

}
    """.trimIndent()
}