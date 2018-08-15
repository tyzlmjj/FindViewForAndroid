package ui

import bean.*
import com.intellij.psi.PsiFile
import extensions.*

import javax.swing.*
import java.awt.event.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class XMLDialog(private val file: PsiFile, list: List<Element>) : BaseJDialog() {

    override var contentPane: JPanel? = null
    override var buttonOK: JButton? = null
    override var buttonCancel: JButton? = null
    private var tvCode: JTextArea? = null
    private var viewTable: JTable? = null
    private var selectAllButton: JButton? = null
    private var selectNoneButton: JButton? = null
    private var selectInvert: JButton? = null
    private var isActivityRadioButton: JRadioButton? = null
    private var isFragmentRadioButton: JRadioButton? = null
    private var isLocalVariableRadioButton: JRadioButton? = null
    private var edtKtRootView: JTextField? = null
    private var isPrivateCheckBox: JCheckBox? = null
    private var addMCheckBox: JCheckBox? = null
    private var kotlinRadioButton: JRadioButton? = null
    private var javaRadioButton: JRadioButton? = null
    private var isReclerViewHolderRadioButton: JRadioButton? = null
    private var addRootViewCheckBox: JCheckBox? = null
    private var edtRootView: JTextField? = null
    private var isReclerViewHolderCheckBox: JCheckBox? = null
    private var isTarget26CheckBox: JCheckBox? = null
    private var isLocalVariableCheckBox: JCheckBox? = null
    private var kotlinPanel: JPanel? = null
    private var javaPanel: JPanel? = null
    private var tvKTViewHolderName: JTextField? = null
    private var tvJavaViewHolderName: JTextField? = null

    private var mViewInfoList = list.toViewInfoList()

    private val mViewTableModel = ViewTableModel(mViewInfoList, addMCheckBox!!.isSelected,
            object : ViewTableModel.TableEditListener {
                override fun onEdited() {
                    generateCode()
                }
            }
    )

    init {
        title = "Generate findViewById code (XML)"

        // 设置大小和位置
        layoutSize(900, 550)

        init()

        initEvent()

        bindData()
    }

    /**
     * 初始化事件
     */
    private fun initEvent() {

        // 选择kotlin
        kotlinRadioButton!!.addActionListener {
            kotlinPanel!!.isVisible = true
            javaPanel!!.isVisible = false
            generateCode()
        }

        // 选择kotlin
        javaRadioButton!!.addActionListener {
            kotlinPanel!!.isVisible = false
            javaPanel!!.isVisible = true
            generateCode()
        }


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

        initKotlinEvent()

        initJavaEvent()
    }

    private fun initKotlinEvent() {

        val kotlinTypeAction = ActionListener {
            tvKTViewHolderName!!.isEnabled = isReclerViewHolderRadioButton!!.isSelected
            edtKtRootView!!.isEnabled = isLocalVariableRadioButton!!.isSelected

            if (isReclerViewHolderRadioButton!!.isSelected) {// viewHolder
                isPrivateCheckBox!!.isSelected = false
                addMCheckBox!!.isSelected = false

                mViewTableModel.setAddM(addMCheckBox!!.isSelected)
                mViewTableModel.fireTableDataChanged()

                generateCode()
            } else {
                generateCode()
            }

        }

        // Fragment
        isFragmentRadioButton!!.addActionListener(kotlinTypeAction)

        // Activity
        isActivityRadioButton!!.addActionListener(kotlinTypeAction)

        // LocalVariable
        isLocalVariableRadioButton!!.addActionListener(kotlinTypeAction)

        // ReclerViewHolder
        isReclerViewHolderRadioButton!!.addActionListener(kotlinTypeAction)

        // 输入ViewHolder名称
        tvKTViewHolderName!!.document.addDocumentListener(object : DocumentListener {
            override fun changedUpdate(e: DocumentEvent?) {}

            override fun insertUpdate(e: DocumentEvent?) {
                if (isReclerViewHolderRadioButton!!.isSelected) {
                    generateCode()
                }
            }

            override fun removeUpdate(e: DocumentEvent?) {
                if (isReclerViewHolderRadioButton!!.isSelected) {
                    generateCode()
                }
            }
        })

        // 输入KtRootView名称
        edtKtRootView!!.document.addDocumentListener(object : DocumentListener {
            override fun changedUpdate(e: DocumentEvent?) {}

            override fun insertUpdate(e: DocumentEvent?) {
                if (isLocalVariableRadioButton!!.isSelected) {
                    generateCode()
                }
            }

            override fun removeUpdate(e: DocumentEvent?) {
                if (isLocalVariableRadioButton!!.isSelected) {
                    generateCode()
                }
            }
        })
    }

    private fun initJavaEvent() {

        // 是否为 API26 及以上
        isTarget26CheckBox!!.addActionListener {
            generateCode()
        }

        // 是否为局部变量
        isLocalVariableCheckBox!!.addActionListener {
            generateCode()
        }

        // 添加rootView
        addRootViewCheckBox!!.addActionListener {
            edtRootView!!.isEnabled = addRootViewCheckBox!!.isSelected
            generateCode()
        }

        // ReclerViewHolder
        isReclerViewHolderCheckBox!!.addActionListener {
            tvJavaViewHolderName!!.isEnabled = isReclerViewHolderCheckBox!!.isSelected
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

        // 输入ViewHolder名称
        tvJavaViewHolderName!!.document.addDocumentListener(object : DocumentListener {
            override fun changedUpdate(e: DocumentEvent?) {
            }

            override fun insertUpdate(e: DocumentEvent?) {
                if (isReclerViewHolderCheckBox!!.isSelected) {
                    generateCode()
                }
            }

            override fun removeUpdate(e: DocumentEvent?) {
                if (isReclerViewHolderCheckBox!!.isSelected) {
                    generateCode()
                }
            }
        })
    }

    /**
     * 绑定数据到视图
     */
    private fun bindData() {
        // 语言单选按钮分组
        val group = ButtonGroup()
        group.add(javaRadioButton)
        group.add(kotlinRadioButton)

        // kotlin类型按钮分组
        val kotlinGroup = ButtonGroup()
        kotlinGroup.add(isActivityRadioButton)
        kotlinGroup.add(isFragmentRadioButton)
        kotlinGroup.add(isLocalVariableRadioButton)
        kotlinGroup.add(isReclerViewHolderRadioButton)

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
        if (kotlinRadioButton!!.isSelected) {// kotlin
            val addM = addMCheckBox!!.isSelected
            val isPrivate = isPrivateCheckBox!!.isSelected
            val isLocalVariable = isLocalVariableRadioButton!!.isSelected
            val rootView = when {
                isLocalVariable -> edtKtRootView!!.text
                isFragmentRadioButton!!.isSelected -> "view!!"
                else -> ""
            }

            if (isReclerViewHolderRadioButton!!.isSelected) {// viewHolder
                tvCode!!.text = mViewInfoList.gengrateKTViewHolderCode(file.name.removeSuffix(".xml"),
                        tvKTViewHolderName?.text ?: "", addM, isPrivate)
            } else {
                tvCode!!.text = mViewInfoList.gengrateKTCode(addM, isPrivate, isLocalVariable, rootView)
            }
        } else {// java
            val addM = addMCheckBox!!.isSelected
            val isPrivate = isPrivateCheckBox!!.isSelected
            val isTarget26 = isTarget26CheckBox!!.isSelected
            val isLocalVariable = isLocalVariableCheckBox!!.isSelected
            val rootView = if (addRootViewCheckBox!!.isSelected) edtRootView!!.text else ""

            if (isReclerViewHolderCheckBox!!.isSelected) {// viewHolder
                tvCode!!.text = mViewInfoList.gengrateJavaViewHolderCode(file.name.removeSuffix(".xml"),
                        tvJavaViewHolderName?.text ?: "", addM, isPrivate, isTarget26)
            } else {
                tvCode!!.text = mViewInfoList.gengrateJavaCode(addM, rootView, isPrivate, isTarget26, isLocalVariable)
            }
        }

        // 将光标移动到开始位置（用于控制垂直滚动在代码生成后一直在顶部）
        tvCode!!.caretPosition = 0
    }
}
