package extensions

import bean.Element
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.XmlRecursiveElementVisitor
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlTag
import utils.AndroidLayoutUtils
import java.util.ArrayList

/**
 * Psi文件处理扩展
 */


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
                    // TODO log
                }

            }
        }
    })

    return elements
}

