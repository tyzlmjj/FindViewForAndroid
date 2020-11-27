package me.majiajie.fvfa.bean

import me.majiajie.fvfa.extensions.firstToUpperCase
import me.majiajie.fvfa.extensions.id2FieldName
import java.util.regex.Pattern

class Element(viewClassName: String, viewId: String) {

    var id: String? = null
    var isAndroidNS = false
    var viewNameFull: String? = null // view 类全名
    var viewName: String? = null // view 类名

    init {
        // id
        val matcher = mIdPattern.matcher(viewId)
        if (matcher.find() && matcher.groupCount() > 1) {
            this.id = matcher.group(2)

            val androidNS = matcher.group(1)
            this.isAndroidNS = !(androidNS == null || androidNS.isEmpty())
        }

        // name
        val packages = viewClassName.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (packages.size > 1) {
            viewNameFull = viewClassName
            viewName = packages[packages.size - 1]
        } else {
            viewNameFull = null
            viewName = viewClassName
        }
    }

    /**
     * 获取变量名称
     *
     * @param addM 是否添加前缀 'm'
     */
    fun getFieldName(addM: Boolean): String {
        return id?.id2FieldName(addM) ?: ""
    }

    /**
     * 完整的ID
     */
    fun fullID(): String {
        val fullID = StringBuilder()
        val rPrefix: String = if (isAndroidNS) {
            "android.R.id."
        } else {
            "R.id."
        }

        fullID.append(rPrefix)
        fullID.append(id)

        return fullID.toString()
    }

    /**
     * 验证变量名称有效性
     */
    fun checkValidity(): Boolean {
        val matcher = mValidityPattern.matcher(getFieldName(false))
        return matcher.find()
    }

    companion object {

        private val mIdPattern = Pattern.compile("@\\+?(android:)?id/([^$]+)$", Pattern.CASE_INSENSITIVE)
        private val mValidityPattern = Pattern.compile("^([a-zA-Z_\\$][\\w\\$]*)$", Pattern.CASE_INSENSITIVE)
    }
}