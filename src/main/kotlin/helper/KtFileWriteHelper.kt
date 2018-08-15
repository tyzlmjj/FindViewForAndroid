package helper

import bean.SelectedInfo
import bean.ViewInfo
import com.android.internal.R.id.list
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import extensions.getFunction
import extensions.getKotlinClass
import org.jetbrains.kotlin.psi.*

/**
 * Kt类文件写入帮助
 */
class KtFileWriteHelper<T>(project: Project,
                           private val psiFile: PsiFile,
                           private val selectedInfo: SelectedInfo,
                           private val viewInfos: List<ViewInfo>,
                           private val addM: Boolean,
                           private val isPrivate: Boolean,
                           private val isLocalVariable: Boolean,
                           private val rootView: String)
    : WriteCommandAction.Simple<T>(project, psiFile) {

    override fun run() {

        val ktBoday = psiFile.getKotlinClass(selectedInfo.selectionStart)?.getBody()
                ?: throw RuntimeException("kotlin class body not found")

//        val ktBoday: KtClassBody = PsiTreeUtil.findChildOfAnyType(psiFile, KtClass::class.java)?.getBody()
//                ?: throw RuntimeException("kotlin class body not found")
        val psiFactory = KtPsiFactory(project)

        if (isLocalVariable) {
            if (!writeLocalVariable()) {
                writeProperty(psiFactory, ktBoday)
            }
        } else {
            writeProperty(psiFactory, ktBoday)
        }
    }

    /**
     * 写成员变量
     */
    private fun writeProperty(ktPsiFactory: KtPsiFactory, ktBoday: KtClassBody) {
        val list = getKtPropertyList()
        val firstProperty: PsiElement? = ktBoday.declarations.firstOrNull { it is KtProperty }
        val toAfter = firstProperty == null

        var index = 0

        // 已经存在的变量
        val oldPropertyNames = ktBoday.declarations.filter { it is KtProperty }.map { it.name }

        var e = firstProperty ?: ktBoday.lBrace!!
        for (i in 0 until list.size) {
            val p = list[i]
            if (!oldPropertyNames.contains(p.name)) {
                if (toAfter) {// 往后添加变量，就添加一个空行
                    e = ktBoday.addAfter(p, e)
                    ktBoday.addBefore(ktPsiFactory.createNewLine(2), e)
                } else {
                    e = ktBoday.addBefore(p, e)
                }
                index = i + 1
                break
            }
        }

        for (i in index until list.size) {
            val p = list[i]
            if (!oldPropertyNames.contains(p.name)) {
                e = ktBoday.addAfter(p, e)
            }
        }
    }

    /**
     * 写局部变量
     */
    private fun writeLocalVariable(): Boolean {
        val functionBody = psiFile.getFunction(selectedInfo.selectionStart)?.children?.firstOrNull { it is KtBlockExpression }
        var beforeElement = functionBody?.children?.firstOrNull {
            it.text.matches(Regex(".*R\\..+${selectedInfo.text}.*"))
        }
        return if (functionBody == null || beforeElement == null) {
            false
        } else {
            getKtPropertyList().forEach {
                beforeElement = functionBody.addAfter(it, beforeElement)
            }
            true
        }
    }

    /**
     * 获取KtProperty
     */
    private fun getKtPropertyList(): List<KtProperty> {
        val ktPsiFactory = KtPsiFactory(project)
        return viewInfos.filter { it.isChecked }.map {
            if (isLocalVariable)
                it.getKTLocalVariableString(addM, rootView)
            else
                it.getKTString(addM, isPrivate, rootView)
        }.map {
            ktPsiFactory.createProperty(it)
        }
    }

}