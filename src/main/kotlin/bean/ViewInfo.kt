package bean


class ViewInfo(@JvmField var isChecked: Boolean, var element: Element) {

    fun getKTString(addM: Boolean, isPrivate: Boolean, rootView: String): String {
        return String.format(CODE_T_ACTIVITY, if (isPrivate) "private " else "", element.getFieldName(addM), element.name, rootView, element.name, element.id)
    }

    companion object {

        private val CODE_T_ACTIVITY = "%sval %s: %s by lazy { %sfindViewById<%s>(R.id.%s) }\n"
    }
}
