package me.majiajie.fvfa.ui

import me.majiajie.fvfa.bean.ViewInfo

import javax.swing.table.AbstractTableModel

/**
 * 从文件解析出的有ID的View的数据表格适配器
 */
class ViewTableModel internal constructor(private val mViewInfoList: List<ViewInfo>, private var mAddM: Boolean, private val mListener: TableEditListener) : AbstractTableModel() {

    interface TableEditListener {

        fun onEdited()
    }

    fun setAddM(addM: Boolean) {
        mAddM = addM
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return columnIndex == 0
    }

    override fun getColumnName(column: Int): String {
        return when (column) {
            0 -> "select"
            1 -> "type"
            2 -> "id"
            3 -> "name"
            else -> "unknow"
        }
    }

    override fun getRowCount(): Int {
        return mViewInfoList.size
    }

    override fun getColumnCount(): Int {
        return 4
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val viewInfo = mViewInfoList[rowIndex]
        when (columnIndex) {
            0 -> return viewInfo.isChecked
            1 -> return viewInfo.element.viewName!!
            2 -> return viewInfo.element.id!!
            3 -> return viewInfo.element.getFieldName(mAddM)
        }
        return ""
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        return if (columnIndex == 0) Boolean::class.javaObjectType else String::class.java
    }

    override fun setValueAt(value: Any?, rowIndex: Int, columnIndex: Int) {
        if (columnIndex == 0 && value is Boolean) {
            mViewInfoList[rowIndex].isChecked = value
            mListener.onEdited()
        }
    }
}
