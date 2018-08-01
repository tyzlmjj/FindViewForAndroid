package ui

import bean.*
import extensions.gengrateJavaCode
import extensions.toClipboard
import extensions.toViewInfoList
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class JavaDialog(list: List<Element>) : BaseJDialog() {

    override var contentPane: JPanel? = null
    override var buttonOK: JButton? = null
    override var buttonCancel: JButton? = null
    private var tvCode: JTextArea? = null
    private var viewTable: JTable? = null
    private var selectAllButton: JButton? = null
    private var selectNoneButton: JButton? = null
    private var selectInvert: JButton? = null
    private var isPrivateCheckBox: JCheckBox? = null
    private var addMCheckBox: JCheckBox? = null

    private var addRootViewCheckBox: JCheckBox? = null
    private var edtRootView: JTextField? = null

    private var isTarget26CheckBox: JCheckBox? = null

    private var mViewInfoList = list.toViewInfoList()

    private val mViewTableModel = ViewTableModel(mViewInfoList, addMCheckBox!!.isSelected,
            object : ViewTableModel.TableEditListener {
                override fun onEdited() {
                    generateCode()
                }
            }
    )

    init {
        title = "Generate findViewById code (JAVA)"

        // 设置大小和位置
        layoutSize(700, 400)

        init()

        initEvent()

        bindData()
    }

    /**
     * 初始化事件
     */
    private fun initEvent() {

        // 全选
        selectAllButton!!.addActionListener {
            for (viewInfo in mViewInfoList) {
                viewInfo.isChecked = true
            }
            mViewTableModel.fireTableDataChanged()
            generateCode()
        }

        // 全不选
        selectNoneButton!!.addActionListener {
            for (viewInfo in mViewInfoList) {
                viewInfo.isChecked = false
            }
            mViewTableModel.fireTableDataChanged()
            tvCode!!.text = ""
        }

        // 反选
        selectInvert!!.addActionListener {
            for (viewInfo in mViewInfoList) {
                viewInfo.isChecked = !viewInfo.isChecked
            }
            mViewTableModel.fireTableDataChanged()
            generateCode()
        }

        // 变量添加"m"
        addMCheckBox!!.addActionListener {
            generateCode()
            mViewTableModel.setAddM(addMCheckBox!!.isSelected)
            mViewTableModel.fireTableDataChanged()
        }

        // 是否私有
        isPrivateCheckBox!!.addActionListener { generateCode() }

        // 是否为 API26 及以上
        isTarget26CheckBox!!.addActionListener {
            generateCode()
        }

        // 添加rootView
        addRootViewCheckBox!!.addActionListener {
            edtRootView!!.isEnabled = addRootViewCheckBox!!.isSelected
            generateCode()
        }


        // 输入rootView名称
        edtRootView!!.document.addDocumentListener(object : DocumentListener {
            override fun changedUpdate(e: DocumentEvent?) {
            }

            override fun insertUpdate(e: DocumentEvent?) {
                if (addRootViewCheckBox!!.isSelected) {
                    generateCode()
                }
            }

            override fun removeUpdate(e: DocumentEvent?) {
                if (addRootViewCheckBox!!.isSelected) {
                    generateCode()
                }
            }
        })

    }

    /**
     * 绑定数据到视图
     */
    private fun bindData() {
        // 适配表格
        viewTable!!.model = mViewTableModel
        generateCode()
    }

    /**
     * 确认
     */
    override fun onOK() {
        tvCode!!.text.toClipboard()
        dispose()
    }

    /**
     * 取消
     */
    override fun onCancel() {
        dispose()
    }

    /**
     * 生成代码
     */
    private fun generateCode() {
        tvCode!!.text = mViewInfoList.gengrateJavaCode(addMCheckBox!!.isSelected, if (addRootViewCheckBox!!.isSelected) edtRootView!!.text else "",
                isPrivateCheckBox!!.isSelected, isTarget26CheckBox!!.isSelected)
    }

}
