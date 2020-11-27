package me.majiajie.fvfa.bean


class ViewInfo(var isChecked: Boolean, var element: Element) {

    fun getKTString(addM: Boolean, isPrivate: Boolean, rootView: String): String {
        return String.format(CODE_TEMPLATE_KOTLIN, if (isPrivate) "private " else "", element.getFieldName(addM), element.viewName, if (rootView.isEmpty) "" else "$rootView.", element.viewName, element.id)
    }

    fun getKTLateinitFieldString(addM: Boolean, isPrivate: Boolean): String {
        return String.format(CODE_TEMPLATE_KOTLIN_LATEINIT, if (isPrivate) "private " else "", element.getFieldName(addM), element.viewName)
    }

    fun getKTValFieldString(addM: Boolean, isPrivate: Boolean): String {
        return String.format(CODE_TEMPLATE_KOTLIN_VAL, if (isPrivate) "private " else "", element.getFieldName(addM), element.viewName)
    }

    fun getKTFindViewString(addM: Boolean,rootView: String): String {
        return String.format(CODE_TEMPLATE_KOTLIN_FIND_VIEW, element.getFieldName(addM),if (rootView.isEmpty) "" else "$rootView.", element.id)
    }

    fun getKTLocalVariableString(addM: Boolean, rootView: String): String {
        return String.format(CODE_TEMPLATE_KOTLIN_LOCAL_VARIABLE, element.getFieldName(addM), element.viewName, if (rootView.isEmpty) "" else "$rootView.", element.id)
    }

    fun getJavaFieldString(addM: Boolean, isPrivate: Boolean): String {
        return String.format(CODE_TEMPLATE_JAVA_FIELD, if (isPrivate) "private " else "", element.viewName, element.getFieldName(addM))
    }

    fun getJavaFindViewString(addM: Boolean, isTarget26: Boolean, rootView: String): String {
        return String.format(CODE_TEMPLATE_JAVA_FINDVIEW, element.getFieldName(addM), if (isTarget26) "" else "(${element.viewName}) ", if (rootView.isEmpty) "" else "$rootView.", element.id)
    }

    fun getJavaLocalVariableString(addM: Boolean, isTarget26: Boolean, rootView: String): String {
        return String.format(CODE_TEMPLATE_JAVA_LOCAL_VARIABLE, element.viewName, element.getFieldName(addM), if (isTarget26) "" else "(${element.viewName}) ", if (rootView.isEmpty) "" else "$rootView.", element.id)
    }

    companion object {

        private const val CODE_TEMPLATE_KOTLIN_INITIALIZER = "by lazy { %sfindViewById<%s>(R.id.%s) }"

        private const val CODE_TEMPLATE_KOTLIN = "%sval %s: %s $CODE_TEMPLATE_KOTLIN_INITIALIZER"

        private const val CODE_TEMPLATE_KOTLIN_LATEINIT = "%slateinit var %s: %s"

        private const val CODE_TEMPLATE_KOTLIN_VAL = "%sval %s: %s"

        private const val CODE_TEMPLATE_KOTLIN_FIND_VIEW = "%s = %sfindViewById(R.id.%s)"

        private const val CODE_TEMPLATE_KOTLIN_LOCAL_VARIABLE = "val %s: %s = %sfindViewById(R.id.%s)"

        private const val CODE_TEMPLATE_JAVA_FIELD = "%s%s %s;"

        private const val CODE_TEMPLATE_JAVA_FINDVIEW = "%s = %s%sfindViewById(R.id.%s);"

        private const val CODE_TEMPLATE_JAVA_LOCAL_VARIABLE = "%s $CODE_TEMPLATE_JAVA_FINDVIEW"
    }
}
