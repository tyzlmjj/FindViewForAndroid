package helper

import bean.SelectedInfo
import bean.ViewInfo
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import extensions.*
import org.apache.http.util.TextUtils

/**
 * Java文件写入帮助类
 */
class JavaFileWriteHelper<T>(project: Project,
                             private val psiFile: PsiFile,
                             private val selectedInfo: SelectedInfo,
                             private val viewInfos: List<ViewInfo>,
                             private val addM: Boolean,
                             private val isPrivate: Boolean,
                             private val rootView: String,
                             private val isTarget26: Boolean,
                             private val isLocalVariable: Boolean
) : WriteCommandAction.Simple<T>(project, psiFile) {

    override fun run() {

        val psiClass = psiFile.getJavaClass(selectedInfo.selectionStart) ?: throw RuntimeException("Java class not found")

        val psiElementFactory = JavaPsiFacade.getElementFactory(project)

        if (!isLocalVariable) {// 非局部变量时添加成员变量
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
        }

        if (psiClass.isExtendsActivity()) {// Activity
            // 搜索Activity 的onCreate方法
            val activityOnCreate = psiClass.findMethodBySignature(psiElementFactory.createMethodFromText(ACTIVITY_ONCREATE_METHOD, psiFile), false)
            if (activityOnCreate != null) {
                addJavaStatementToMethod(activityOnCreate, psiElementFactory)
            } else {// 不存在onCreate()方法!
                addJavaStatementToCodeBlock(psiClass, psiElementFactory)
            }
        } else if (psiClass.isExtendsDialogFragment()) {// DialogFragment
            addJavaStatementToCodeBlock(psiClass, psiElementFactory)
        } else if (psiClass.isExtendsFragment()) {// Fragment
            // 搜索Fragment的OnViewCreated方法
            val fragmentOnViewCreated = psiClass.findMethodBySignature(psiElementFactory.createMethodFromText(FRAGMENT_ONVIEWCREATED_METHOD, psiFile), false)
            if (fragmentOnViewCreated != null) {// 存在 OnViewCreated
                addJavaStatementToFragmentMethod(fragmentOnViewCreated, psiElementFactory)
            } else {//不存在 OnViewCreated,新建一个
                val newMethod = psiElementFactory.createMethodFromText(FRAGMENT_ONVIEWCREATED_METHOD, psiFile)
                addJavaStatementToFragmentMethod(newMethod, psiElementFactory)
                psiClass.add(newMethod)
            }
        } else {// 无法判断类型
            addJavaStatementToCodeBlock(psiClass, psiElementFactory)
        }
    }

    /**
     * 在Fragment.OnViewCreated()方法中添加内容（这里强制修改rootView修改为方法第一个参数名）
     */
    private fun addJavaStatementToFragmentMethod(fragmentOnViewCreated: PsiMethod, psiElementFactory: PsiElementFactory) {
        getJavaStatement(psiElementFactory, fragmentOnViewCreated.parameters[0].name!!).forEach {
            fragmentOnViewCreated.body?.add(it)
        }
    }

    /**
     * 将代码添加到选中内容的代码块内。假如不存在代码块就创建initView()方法
     */
    private fun addJavaStatementToCodeBlock(psiClass: PsiClass, psiElementFactory: PsiElementFactory) {
        val body = psiFile.getMethod(selectedInfo.selectionStart)?.body
        if (body != null) {
            var beforeElement = body.children.firstOrNull { it.text.matches(Regex(".*R\\..+${selectedInfo.text}.*")) }
            if (beforeElement == null) {
                getJavaStatement(psiElementFactory, rootView).forEach {
                    body.add(it)
                }
            } else {
                getJavaStatement(psiElementFactory, rootView).forEach {
                    beforeElement = body.addAfter(it,beforeElement)
                }
            }
        } else {
            addInitViewMehod(psiClass, psiElementFactory)
        }
    }

    /**
     * 添加initView()方法
     */
    private fun addInitViewMehod(psiClass: PsiClass, psiElementFactory: PsiElementFactory) {
        val initMethod = psiElementFactory.createMethodFromText(
                if (TextUtils.isEmpty(rootView)) INITVIEW_METHOD else String.format(INITVIEW_METHOD_HAS_PARAMETERS, rootView), psiFile)

        // 搜索是否已经存在initView方法
        val oldInitMethod = psiClass.findMethodBySignature(initMethod, false)
        if (oldInitMethod != null) {
            addJavaStatementToMethod(oldInitMethod, psiElementFactory)
        } else {
            addJavaStatementToMethod(initMethod, psiElementFactory)
            psiClass.add(initMethod)
        }
    }

    /**
     * 在方法中添加内容
     */
    private fun addJavaStatementToMethod(psiMethod: PsiMethod, psiElementFactory: PsiElementFactory) {
        getJavaStatement(psiElementFactory, rootView).forEach {
            psiMethod.body?.add(it)
        }
    }

    /**
     * 获取成Java代码变量
     */
    private fun getJavaField(psiElementFactory: PsiElementFactory): List<PsiField> {
        return viewInfos.filter { it.isChecked }
                .map { it.getJavaFieldString(addM, isPrivate) }
                .map { psiElementFactory.createFieldFromText(it, psiFile) }
    }

    /**
     * 获取Java参数声明
     */
    private fun getJavaStatement(psiElementFactory: PsiElementFactory, rootView: String): List<PsiStatement> {
        return viewInfos.filter { it.isChecked }
                .map { if (isLocalVariable) it.getJavaLocalVariableString(addM, isTarget26, rootView) else it.getJavaFindViewString(addM, isTarget26, rootView) }
                .map { psiElementFactory.createStatementFromText(it, psiFile) }
    }

    companion object {

        private const val FRAGMENT_ONVIEWCREATED_METHOD = "@Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {}"

        private const val ACTIVITY_ONCREATE_METHOD = "protected void onCreate(Bundle savedInstanceState){}"

        private const val INITVIEW_METHOD = "private void initView(){}"

        private const val INITVIEW_METHOD_HAS_PARAMETERS = "private void initView(View %s){}"
    }
}
