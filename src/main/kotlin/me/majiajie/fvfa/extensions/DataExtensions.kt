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
 * id转换变量名
 * @param addM 是否添加前缀 'm'
 */
fun String.id2FieldName(addM: Boolean): String {
    val names = this.split("_".toRegex()).dropLastWhile { it.isEmpty }.toTypedArray()
    val sb = StringBuilder()
    if (addM) {
        // mAaBbCc
        for (i in names.indices) {
            if (i == 0) {
                sb.append("m")
            }
            sb.append(names[i].firstToUpperCase())
        }
    } else {
        // aaBbCc
        for (i in names.indices) {
            if (i == 0) {
                sb.append(names[i])
            } else {
                sb.append(names[i].firstToUpperCase())
            }
        }

    }
    return sb.toString()
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

    val fieldStr = this.filter { it.isChecked }.joinToString("\n\t") { it.getKTValFieldString(addM, isPrivate) }
    val findViewStr = this.filter { it.isChecked }.joinToString("\n\t") { it.getKTFindViewString(addM, "itemView") }

    return """
class ${viewHolderName}ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

    $fieldStr
    
    init{
        $findViewStr
    }

    companion object {

        fun newInstance(parent: ViewGroup): ${viewHolderName}ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.$layoutName, parent, false)
            return ${viewHolderName}ViewHolder(view)
        }
    }

}""".trimIndent()
}

/**
 * 从布局名生成Kotlin ViewHolder代码(ViewBinding)
 */
fun String.generateKTViewHolderViewBindingCode(viewHolderName: String): String {

    val bindingClassName = this.id2FieldName(false).firstToUpperCase() + "Binding"

    return """
class ${viewHolderName}ViewHolder private constructor(val binding: $bindingClassName) : RecyclerView.ViewHolder(binding.root) {

    companion object {

        fun newInstance(parent: ViewGroup): ${viewHolderName}ViewHolder {
            val binding = LayoutItemTextBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ${viewHolderName}ViewHolder(binding)
        }
    }

}""".trimIndent()
}

/**
 * 生成Kotlin Epoxy model代码
 */
fun List<ViewInfo>.generateKTEpoxyModelCode(layoutName: String, modelName: String, addM: Boolean): String {

    val fieldStr = this.filter { it.isChecked }.joinToString("\n\t") { it.getKTLateinitFieldString(addM, false) }
    val findViewStr = this.filter { it.isChecked }.joinToString("\n\t") { it.getKTFindViewString(addM, "itemView") }

    return """
@EpoxyModelClass
abstract class ${modelName}Model : EpoxyModelWithHolder<${modelName}Model.${modelName}ViewHolder>() {

    override fun bind(holder: ${modelName}ViewHolder) {
    
    }
    
    override fun getDefaultLayout(): Int {
        return R.layout.$layoutName
    }

    class ${modelName}ViewHolder : EpoxyHolder() {

        lateinit var itemView: View
        $fieldStr

        override fun bindView(itemView: View) {
            this.itemView = itemView
            $findViewStr
        }
    }

}""".trimIndent()
}

/**
 * 从布局名生成Kotlin Epoxy model代码(ViewBinding)
 */
fun String.generateKTEpoxyModelViewBindingCode(modelName: String): String {

    val bindingClassName = this.id2FieldName(false).firstToUpperCase() + "Binding"

    return """
@EpoxyModelClass
abstract class ${modelName}Model : EpoxyModelWithHolder<${modelName}Model.${modelName}ViewHolder>() {

    override fun bind(holder: ${modelName}ViewHolder) {

    }
    
    override fun getDefaultLayout(): Int {
        return R.layout.$this
    }

    class ${modelName}ViewHolder : EpoxyHolder() {

        lateinit var itemView: View
        lateinit var binding: $bindingClassName

        override fun bindView(itemView: View) {
            this.itemView = itemView
            this.binding = $bindingClassName.bind(itemView)
        }
    }

}""".trimIndent()
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
 * 从布局名生成Java ViewHolder代码(ViewBinding)
 */
fun String.generateJavaViewHolderViewBindingCode(viewHolderName: String, isPrivate: Boolean): String {

    val bindingClassName = this.id2FieldName(false).firstToUpperCase() + "Binding"

    return """
static class ${viewHolderName}ViewHolder extends RecyclerView.ViewHolder {

    public static ${viewHolderName}ViewHolder newInstance(ViewGroup parent) {
        $bindingClassName binding = $bindingClassName.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ${viewHolderName}ViewHolder(binding);
    }

    ${if (isPrivate) "private " else ""}$bindingClassName binding;

    private ${viewHolderName}ViewHolder($bindingClassName binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

}""".trimIndent()
}

/**
 * 生成Java Epoxy model代码
 */
fun List<ViewInfo>.generateJavaEpoxyModelCode(layoutName: String, modelName: String, addM: Boolean, isPrivate: Boolean, isTarget26: Boolean): String {

    val infos = this.filter { it.isChecked }

    val fieldStr = infos.joinToString("\n\t") { it.getJavaFieldString(addM, isPrivate) }
    val findViewStr = infos.joinToString("\n\t    ") { it.getJavaFindViewString(addM, isTarget26, "itemView") }

    return """
@EpoxyModelClass
public abstract class ${modelName}Model extends EpoxyModelWithHolder<${modelName}Model.${modelName}ViewHolder> {

    @Override
    public void bind(@NonNull ${modelName}ViewHolder holder) {

    }
    
    @Override
    public int getDefaultLayout() {
        return R.layout.$layoutName;
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

/**
 * 从布局名生成Java Epoxy model代码(ViewBinding)
 */
fun String.generateJavaEpoxyModelViewBindingCode(modelName: String, isPrivate: Boolean): String {

    val bindingClassName = this.id2FieldName(false).firstToUpperCase() + "Binding"

    return """
@EpoxyModelClass
public abstract class ${modelName}Model extends EpoxyModelWithHolder<${modelName}Model.${modelName}ViewHolder> {

    @Override
    public void bind(@NonNull ${modelName}ViewHolder holder) {

    }
    
    @Override
    public int getDefaultLayout() {
        return R.layout.$this;
    }

    static class ${modelName}ViewHolder extends EpoxyHolder {

        ${if (isPrivate) "private " else ""}$bindingClassName binding;

        @Override
        protected void bindView(@NonNull View itemView) {
            binding = $bindingClassName.bind(itemView);
        }
    }

}""".trimIndent()
}