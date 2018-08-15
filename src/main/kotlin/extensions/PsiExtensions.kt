package extensions

import bean.Element
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiClassImpl
import com.intellij.psi.impl.source.PsiMethodImpl
import com.intellij.psi.impl.source.tree.java.PsiCodeBlockImpl
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlTag
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFunction
import utils.AndroidLayoutUtils
import java.util.ArrayList


/**
 * 在指定范围内寻找文件
 */
fun GlobalSearchScope.findFiles(project: Project, fileName: String): Array<PsiFile> {
    return FilenameIndex.getFilesByName(project, fileName, this)
}

/**
 * 获取XML文件中的Android视图ids
 */
fun PsiFile.getAndroidViewIds(): ArrayList<Element> {
    val elements = ArrayList<Element>()

    this.accept(object : XmlRecursiveElementVisitor() {

        override fun visitElement(element: PsiElement) {
            super.visitElement(element)

            if (element is XmlTag) {

                if ("include".equals(element.name, ignoreCase = true)) {
                    val layout = element.getAttribute("layout", null)

                    if (layout != null) {
                        val project = this@getAndroidViewIds.project
                        val layoutName = AndroidLayoutUtils.getLayoutName(layout.value)
                        val include = if (layoutName == null) null else
                            AndroidLayoutUtils.findLayoutResourceFile(this@getAndroidViewIds, project, "$layoutName.xml")

                        if (include != null) {
                            elements.addAll(include.getAndroidViewIds())
                            return
                        }
                    }
                }

                // get element ID
                val id = element.getAttribute("android:id", null)
                        ?: return  // missing android:id attribute
                val value = id.value
                        ?: return  // empty value

                // check if there is defined custom class
                var name: String? = element.name
                val clazz = element.getAttribute("class", null)
                if (clazz != null) {
                    name = clazz.value
                }

                try {
                    val e = Element(name!!, value)
                    elements.add(e)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    })

    return elements
}

/**
 * 判断类是否继承Activity
 */
fun PsiClass.isExtendsActivity(): Boolean {
    return this.isExtendsClass("Activity", kotlin.arrayOf("android.app"))
}

/**
 * 判断类是否继承Fragment
 */
fun PsiClass.isExtendsFragment(): Boolean {
    return this.isExtendsClass("Fragment", kotlin.arrayOf("android.support.v4.app", "android.app"))
}

/**
 * 判断类是否继承DialogFragment
 */
fun PsiClass.isExtendsDialogFragment(): Boolean {
    return this.isExtendsClass("DialogFragment", kotlin.arrayOf("android.support.v4.app", "android.app"))
}

/**
 * 判断类是否继承指定的类
 */
fun PsiClass.isExtendsClass(className: String, packageNames: Array<String>): Boolean {
    val superClass = this.superClass
    return if (superClass == null) {
        false
    } else {
        val psiFile = superClass.containingFile
        (psiFile is PsiJavaFile && superClass.name == className && packageNames.contains(psiFile.packageName))
                || superClass.isExtendsClass(className, packageNames)
    }
}

/**
 * 获取指定位置的所在的方法,没有则返回空
 */
fun PsiFile.getMethod(selectionStart: Int): PsiMethodImpl? {
    var psiElement: PsiElement? = this.findElementAt(selectionStart)
    while (psiElement != null) {
        if (psiElement is PsiMethodImpl) {
            return psiElement
        }
        psiElement = psiElement.parent
    }
    return null
}

/**
 * 获取指定位置的所在的Java类,没有则返回空
 */
fun PsiFile.getJavaClass(selectionStart: Int): PsiClassImpl? {
    var psiElement: PsiElement? = this.findElementAt(selectionStart)
    while (psiElement != null) {
        if (psiElement is PsiClassImpl) {
            return psiElement
        }
        psiElement = psiElement.parent
    }
    return null
}

/**
 * 获取指定位置的所在的Kotlin类,没有则返回空
 */
fun PsiFile.getKotlinClass(selectionStart: Int): KtClass? {
    var psiElement: PsiElement? = this.findElementAt(selectionStart)
    while (psiElement != null) {
        if (psiElement is KtClass) {
            return psiElement
        }
        psiElement = psiElement.parent
    }
    return null
}

/**
 * 获取指定位置的所在的Kotlin方法,没有则返回空
 */
fun PsiFile.getFunction(selectionStart: Int): KtFunction? {
    var psiElement: PsiElement? = this.findElementAt(selectionStart)
    while (psiElement != null) {
        if (psiElement is KtFunction) {
            return psiElement
        }
        psiElement = psiElement.parent
    }
    return null
}

