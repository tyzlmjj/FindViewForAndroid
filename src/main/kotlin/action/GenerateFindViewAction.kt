package action

import bean.Element
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import org.apache.http.util.TextUtils
import ui.JavaDialog
import ui.KotlinDialog
import ui.XMLDialog
import utils.LayoutUtils

import java.util.ArrayList

/**
 * 生成事件自动识别文件
 * 目前支持的：XML、JAVA、KOTLIN
 */
class GenerateFindViewAction : AnAction() {

    private var mSelectedText: String? = null

    override fun actionPerformed(e: AnActionEvent) {

        // 获取project
        val project = e.getData(PlatformDataKeys.PROJECT) ?: return

        // 获取焦点文件
        val psiFile = e.getData(LangDataKeys.PSI_FILE)
        if (psiFile == null) {
            Messages.showWarningDialog(project, "文件无法识别", "异常")
            return
        }

        when (psiFile.fileType.name.toUpperCase()) {
            "KOTLIN" -> if (checkSelectedText(e, project)) {
                showGenerateKotlinCodeDialog(searchFileAndGetElementList(project))
            }
            "JAVA" -> if (checkSelectedText(e, project)) {
                showGenerateJavaCodeDialog(searchFileAndGetElementList(project))
            }
            "XML" -> showGenerateXMLDialog(psiFile, LayoutUtils.getIDsFromLayout(psiFile))
            else -> Messages.showWarningDialog(project, "不支持的文件类型: " + psiFile.fileType.name, "警告")
        }
    }

    /**
     * 检查是否选中字符串
     */
    private fun checkSelectedText(e: AnActionEvent, project: Project): Boolean {
        // 获取选中内容
        val editor = e.getData(PlatformDataKeys.EDITOR) ?: return false

        val model = editor.selectionModel
        mSelectedText = model.selectedText

        if (TextUtils.isEmpty(mSelectedText)) {
            Messages.showWarningDialog(project, "未选择layout文件名", "异常")
            return false
        }

        return true
    }

    /**
     * 搜搜布局文件，并且解析内容
     */
    private fun searchFileAndGetElementList(project: Project): ArrayList<Element> {
        // todo 搜索整个项目有可能搜索到错误的文件
        // 获取布局文件，通过FilenameIndex.getFilesByName获取
        // GlobalSearchScope.allScope(project)搜索整个项目
        val psiFiles = FilenameIndex.getFilesByName(project, mSelectedText!! + ".xml", GlobalSearchScope.allScope(project))
        if (psiFiles.isEmpty()) {
            Messages.showWarningDialog(project, "未找到选中的布局文件", "异常")
            return ArrayList()
        }
        // 解析布局中所有View的ID
        return LayoutUtils.getIDsFromLayout(psiFiles[0])
    }

    /**
     * 显示生成Kotlin代码的Dialog
     * @param elements  view数据
     */
    private fun showGenerateKotlinCodeDialog(elements: ArrayList<Element>) {
        val dialog = KotlinDialog(elements)
        dialog.pack()
        dialog.isVisible = true
    }

    /**
     * 显示生成代码的Dialog(XML)
     * @param psiFile   文件
     * @param elements  view数据
     */
    private fun showGenerateXMLDialog(psiFile: PsiFile, elements: ArrayList<Element>) {
        val dialog = XMLDialog(psiFile,elements)
        dialog.pack()
        dialog.isVisible = true
    }

    /**
     * 显示生成代码的Dialog(JAVA)
     * @param psiFile   文件
     * @param elements  view数据
     */
    private fun showGenerateJavaCodeDialog(elements: ArrayList<Element>) {
        val dialog = JavaDialog(elements)
        dialog.pack()
        dialog.isVisible = true
    }
}
