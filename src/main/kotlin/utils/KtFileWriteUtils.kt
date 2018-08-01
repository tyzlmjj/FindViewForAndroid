package utils

import bean.ViewInfo
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.util.PsiClassUtil
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.uast.getContainingClass

/**
 * kotlin 文件写入工具
 */
object KtFileWriteUtils{

    fun addPropertyToKtClass(project:Project,ktClass:KtClass,list:List<KtProperty>){

        WriteCommandAction.runWriteCommandAction(project) {
//            val ktPsiFactory = KtPsiFactory(project)
//            // 创建变量
//            val ktProperty = ktPsiFactory.createProperty("private", "test", "View", false, "lazy {}")
            list.forEach {
                ktClass.getContainingClass()!!.add(it)
//                ktClass.addAfter(it,ktClass.getClassOrInterfaceKeyword())
            }
        }
    }
}