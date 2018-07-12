package bean

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

/**
 * 复制字符串到剪贴板
 */
fun String.toClipboard(){
    val clip = Toolkit.getDefaultToolkit().systemClipboard
    val tText = StringSelection(this)
    clip.setContents(tText, null)
}

/**
 * Element to ViewInfo
 */
fun List<Element>.toViewInfoList() = this.map { ViewInfo(true, it) }

/**
 * 生成Kotlin代码
 */
fun List<ViewInfo>.gengrateKTCode(addM: Boolean, isPrivate: Boolean, rootView: String):String {
    return this.filter { it.isChecked }.joinToString("") { it.getKTString(addM,isPrivate,rootView) }
}