package helper

import bean.ViewInfo
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil

/**
 * Java文件写入帮助类
 */
class JavaFileWriteHelper<T>(project: Project,
                             private val psiFile: PsiFile,
                             private val viewInfos: List<ViewInfo>,
                             private val addM: Boolean,
                             private val isPrivate: Boolean,
                             private val rootView: String,
                             private val isTarget26: Boolean
) : WriteCommandAction.Simple<T>(project, psiFile) {

    override fun run() {
        val psiClass = PsiTreeUtil.findChildOfAnyType(psiFile, PsiClass::class.java)
        if (psiClass == null) {
            throw RuntimeException("Java class not found")
        } else {
            val psiElementFactory = JavaPsiFacade.getElementFactory(project)
            // 已经存在的变量
            val oldFieldNameList = psiClass.allFields.map { it.name }
            // 生成变量
            val fieldList = getJavaField(psiElementFactory)
            // 添加变量
            fieldList.forEach {
                // 已经存在同名变量就不添加
                if (!oldFieldNameList.contains(it.name)) {
                    psiClass.add(it)
                }
            }

            // 检查是否存在'initView()'方法
            val initMethod = psiClass.findMethodsByName("initView", true).firstOrNull { it.parameters.isEmpty() }
            if (initMethod != null) {
                getJavaStatement(psiElementFactory).forEach {
                    initMethod.body?.add(it)
                }
            } else {// 不存在就创建initView方法
                val method = createInitViewMethod(psiElementFactory)
                psiClass.add(method)
            }
        }
    }

    /**
     * 获取成Java代码变量
     */
    private fun getJavaField(psiElementFactory: PsiElementFactory)
            : List<PsiField> {
        return viewInfos.filter { it.isChecked }
                .map { it.getJavaFieldString(addM, isPrivate) }
                .map { psiElementFactory.createFieldFromText(it, psiFile) }
    }

    /**
     * 获取Java参数变量
     */
    private fun getJavaStatement(psiElementFactory: PsiElementFactory)
            : List<PsiStatement> {
        return viewInfos.filter { it.isChecked }
                .map { it.getJavaFindViewString(addM, isTarget26, rootView) }
                .map { psiElementFactory.createStatementFromText(it, psiFile) }
    }

    /**
     * 创建initView方法
     */
    private fun createInitViewMethod(psiElementFactory: PsiElementFactory): PsiMethod {
        return psiElementFactory.createMethodFromText(
                """public void initView(){
                    ${viewInfos.filter { it.isChecked }.map { it.getJavaFindViewString(addM, isTarget26, rootView) }.joinToString("\n") { it }}
                    }
                """.trimIndent(), psiFile)
    }

}
