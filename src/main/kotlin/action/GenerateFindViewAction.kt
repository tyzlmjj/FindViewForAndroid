package action

import bean.Element
import bean.SelectedInfo
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiFile
import extensions.getAndroidViewIds
import ui.JavaDialog
import ui.KotlinDialog
import ui.XMLDialog
import utils.AndroidLayoutUtils

import java.util.ArrayList

/**
 * 生成事件自动识别文件
 * 目前支持的：XML、JAVA、KOTLIN
 */
class GenerateFindViewAction : AnAction() {

    private var selectedInfo: SelectedInfo? = null

    override fun actionPerformed(e: AnActionEvent) {
        // 获取project
        val project = e.getData(PlatformDataKeys.PROJECT) ?: return

        // 获取焦点文件
        val psiFile = e.getData(LangDataKeys.PSI_FILE)
        if (psiFile == null) {
            Messages.showWarningDialog(project, "No focus file", "ERROE")
            return
        }

        when (psiFile.fileType.name.toUpperCase()) {
            "KOTLIN" -> {
                if (hasSelectedText(e)) {
                    showGenerateKotlinCodeDialog(project, selectedInfo!!, psiFile, searchFileAndGetElementList(project, psiFile))
                } else {
                    Messages.showErrorDialog("Layout file name is not selected", "ERROE")
                }
            }
            "JAVA" -> {
                if (hasSelectedText(e)) {
                    showGenerateJavaCodeDialog(project, selectedInfo!!, psiFile, searchFileAndGetElementList(project, psiFile))
                } else {
                    Messages.showErrorDialog("Layout file name is not selected", "ERROE")
                }
            }
            "XML" -> {
                showGenerateXMLDialog(psiFile, psiFile.getAndroidViewIds())
            }
            else -> Messages.showWarningDialog(project, "This file type (${psiFile.fileType.name}) is not supported", "Warning")
        }
    }

    /**
     * 检查是否选中字符串
     */
    private fun hasSelectedText(e: AnActionEvent): Boolean {
        // 获取选中内容
        val editor = e.getData(PlatformDataKeys.EDITOR) ?: return false

        val model = editor.selectionModel
        val text = model.selectedText
        return if (text == null || text.isEmpty()) {
            false
        } else {
            selectedInfo = SelectedInfo(text, model.selectionStart)
            true
        }
    }

    /**
     * 搜搜布局文件,并且解析内容
     */
    private fun searchFileAndGetElementList(project: Project, psiFile: PsiFile): ArrayList<Element> {

        val file = AndroidLayoutUtils.findLayoutResourceFile(psiFile, project, "${selectedInfo?.text}.xml")

        return file?.getAndroidViewIds() ?: ArrayList()
    }

    /**
     * 显示生成代码的Dialog(Kotlin)
     */
    private fun showGenerateKotlinCodeDialog(project: Project, selectedInfo: SelectedInfo, psiFile: PsiFile, elements: ArrayList<Element>) {
        val dialog = KotlinDialog(project, psiFile, selectedInfo, elements)
        dialog.pack()
        dialog.isVisible = true
    }

    /**
     * 显示生成代码的Dialog(XML)
     */
    private fun showGenerateXMLDialog(psiFile: PsiFile, elements: ArrayList<Element>) {
        val dialog = XMLDialog(psiFile, elements)
        dialog.pack()
        dialog.isVisible = true
    }

    /**
     * 显示生成代码的Dialog(JAVA)
     */
    private fun showGenerateJavaCodeDialog(project: Project, selectedInfo: SelectedInfo, psiFile: PsiFile, elements: ArrayList<Element>) {
        val dialog = JavaDialog(project, selectedInfo, psiFile, elements)
        dialog.pack()
        dialog.isVisible = true
    }
}
