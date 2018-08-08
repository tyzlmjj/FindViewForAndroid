package helper

import bean.ViewInfo
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.*

/**
 * Kt类文件写入帮助
 */
class KtFileWriteHelper<T>(project: Project,
                           private val psiFile: PsiFile,
                           private val viewInfos: List<ViewInfo>,
                           private val addM: Boolean,
                           private val isPrivate: Boolean,
                           private val rootView: String)
    : WriteCommandAction.Simple<T>(project, psiFile) {

    override fun run() {
        val ktClass: KtClass? = PsiTreeUtil.findChildOfAnyType(psiFile, KtClass::class.java)

        val ktBoday = ktClass?.getBody()
        if (ktBoday == null) {
            throw RuntimeException("kotlin class body not found")
        } else {
            val propertyList = getKtPropertyList()

            val firstProperty: PsiElement? = ktBoday.declarations.firstOrNull { it is KtProperty }
            writeFile(KtPsiFactory(project), ktBoday, propertyList, firstProperty
                    ?: ktBoday.lBrace!!, firstProperty == null)
        }
    }

    /**
     * 写入
     */
    private fun writeFile(ktPsiFactory: KtPsiFactory, ktBoday: KtClassBody, list: List<KtProperty>, psiElement: PsiElement, toAfter: Boolean) {

        var index = 0
        val last = list.size - 1

        // 已经存在的变量
        val oldPropertyNames = ktBoday.declarations.filter { it is KtProperty }.map { it.name }

        var e = psiElement
        for (i in 0..last) {
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

        for (i in index..last) {
            val p = list[i]
            if (!oldPropertyNames.contains(p.name)) {
                e = ktBoday.addAfter(p, e)
            }
        }
    }

    /**
     * 获取KtProperty
     */
    private fun getKtPropertyList(): List<KtProperty> {
        val ktPsiFactory = KtPsiFactory(project)
        return viewInfos.filter { it.isChecked }.map {
            ktPsiFactory.createProperty(it.getKTString(addM, isPrivate, rootView))
        }
    }

}