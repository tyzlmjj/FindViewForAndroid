package me.majiajie.fvfa.ui

import com.intellij.psi.PsiFile
import me.majiajie.fvfa.bean.Element
import me.majiajie.fvfa.extensions.*

import javax.swing.*
import java.awt.event.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class XMLDialog(private val file: PsiFile, list: List<Element>) : BaseJDialog() {

    override lateinit var contentPane: JPanel
    override lateinit var buttonOK: JButton
    override lateinit var buttonCancel: JButton
    private lateinit var tvCode: JTextArea
    private lateinit var viewTable: JTable
    private lateinit var selectAllButton: JButton
    private lateinit var selectNoneButton: JButton
    private lateinit var selectInvert: JButton
    private lateinit var isActivityRadioButton: JRadioButton
    private lateinit var isFragmentRadioButton: JRadioButton
    private lateinit var isLocalVariableRadioButton: JRadioButton
    private lateinit var edtKtRootView: JTextField
    private lateinit var isPrivateCheckBox: JCheckBox
    private lateinit var addMCheckBox: JCheckBox
    private lateinit var kotlinRadioButton: JRadioButton
    private lateinit var javaRadioButton: JRadioButton
    private lateinit var isReclerViewHolderRadioButton: JRadioButton
    private lateinit var isEpoxyRadioButton: JRadioButton
    private lateinit var addRootViewCheckBox: JCheckBox
    private lateinit var edtRootView: JTextField
    private lateinit var isReclerViewHolderCheckBox: JCheckBox
    private lateinit var isTarget26CheckBox: JCheckBox
    private lateinit var isLocalVariableCheckBox: JCheckBox
    private lateinit var isEpoxyModelCheckBox: JCheckBox
    private lateinit var kotlinPanel: JPanel
    private lateinit var javaPanel: JPanel
    private lateinit var edtKTViewHolderName: JTextField
    private lateinit var edtJavaViewHolderName: JTextField
    private lateinit var edtKTEpoxyModelName: JTextField
    private lateinit var edtJavaEpoxyModelName: JTextField

    private var mViewInfoList = list.toViewInfoList()

    private val mViewTableModel = ViewTableModel(mViewInfoList, addMCheckBox.isSelected,
            object : ViewTableModel.TableEditListener {
                override fun onEdited() {
                    generateCode()
                }
            }
    )

    init {
        title = "Generate findViewById code (XML)"

        init()

        javaPanel.isVisible = false

        initEvent()

        bindData()
    }

    /**
     * 初始化事件
     */
    private fun initEvent() {

        // 选择kotlin
        kotlinRadioButton.addActionListener {
            kotlinPanel.isVisible = true
            javaPanel.isVisible = false
            generateCode()
        }

        // 选择Java
        javaRadioButton.addActionListener {
            kotlinPanel.isVisible = false
            javaPanel.isVisible = true
            pack()
//            setLocationRelativeTo(null)
            generateCode()
        }

        // 全选
        selectAllButton.addActionListener {
            for (viewInfo in mViewInfoList) {
                viewInfo.isChecked = true
            }
            mViewTableModel.fireTableDataChanged()
            generateCode()
        }

        // 全不选
        selectNoneButton.addActionListener {
            for (viewInfo in mViewInfoList) {
                viewInfo.isChecked = false
            }
            mViewTableModel.fireTableDataChanged()
            tvCode.text = ""
        }

        // 反选
        selectInvert.addActionListener {
            for (viewInfo in mViewInfoList) {
                viewInfo.isChecked = !viewInfo.isChecked
            }
            mViewTableModel.fireTableDataChanged()
            generateCode()
        }

        // 变量添加"m"
        addMCheckBox.addActionListener {
            generateCode()
            mViewTableModel.setAddM(addMCheckBox.isSelected)
            mViewTableModel.fireTableDataChanged()
        }

        // 是否私有
        isPrivateCheckBox.addActionListener { generateCode() }

        initKotlinEvent()

        initJavaEvent()
    }

    private fun initKotlinEvent() {

        val kotlinTypeAction = ActionListener {
            edtKTViewHolderName.isEnabled = isReclerViewHolderRadioButton.isSelected
            edtKTEpoxyModelName.isEnabled = isEpoxyRadioButton.isSelected
            edtKtRootView.isEnabled = isLocalVariableRadioButton.isSelected

            if (isReclerViewHolderRadioButton.isSelected || isEpoxyRadioButton.isSelected) {// ViewHolder or EpoxyModel
                isPrivateCheckBox.isSelected = false
                addMCheckBox.isSelected = false

                mViewTableModel.setAddM(addMCheckBox.isSelected)
                mViewTableModel.fireTableDataChanged()
            }
            generateCode()
        }

        // Fragment
        isFragmentRadioButton.addActionListener(kotlinTypeAction)

        // Activity
        isActivityRadioButton.addActionListener(kotlinTypeAction)

        // LocalVariable
        isLocalVariableRadioButton.addActionListener(kotlinTypeAction)

        // ReclerViewHolder
        isReclerViewHolderRadioButton.addActionListener(kotlinTypeAction)

        // EpoxyModel
        isEpoxyRadioButton.addActionListener(kotlinTypeAction)

        // 输入ViewHolder名称
        edtKTViewHolderName.document.addDocumentListener(object : DocumentListener {
            override fun changedUpdate(e: DocumentEvent?) {}

            override fun insertUpdate(e: DocumentEvent?) {
                if (isReclerViewHolderRadioButton.isSelected) {
                    generateCode()
                }
            }

            override fun removeUpdate(e: DocumentEvent?) {
                if (isReclerViewHolderRadioButton.isSelected) {
                    generateCode()
                }
            }
        })

        // 输入KtRootView名称
        edtKtRootView.document.addDocumentListener(object : DocumentListener {
            override fun changedUpdate(e: DocumentEvent?) {}

            override fun insertUpdate(e: DocumentEvent?) {
                if (isLocalVariableRadioButton.isSelected) {
                    generateCode()
                }
            }

            override fun removeUpdate(e: DocumentEvent?) {
                if (isLocalVariableRadioButton.isSelected) {
                    generateCode()
                }
            }
        })

        // 输入EpoxyModel名称
        edtKTEpoxyModelName.document.addDocumentListener(object : DocumentListener {
            override fun changedUpdate(e: DocumentEvent?) {
            }

            override fun insertUpdate(e: DocumentEvent?) {
                if (isEpoxyRadioButton.isSelected) {
                    generateCode()
                }
            }

            override fun removeUpdate(e: DocumentEvent?) {
                if (isEpoxyRadioButton.isSelected) {
                    generateCode()
                }
            }
        })
    }

    private fun initJavaEvent() {

        // 是否为 API26 及以上
        isTarget26CheckBox.addActionListener {
            generateCode()
        }

        // 是否为局部变量
        isLocalVariableCheckBox.addActionListener {
            generateCode()
        }

        // 添加rootView
        addRootViewCheckBox.addActionListener {
            edtRootView.isEnabled = addRootViewCheckBox.isSelected
            generateCode()
        }

        // ReclerViewHolder
        isReclerViewHolderCheckBox.addActionListener {
            edtJavaViewHolderName.isEnabled = isReclerViewHolderCheckBox.isSelected
            if (isReclerViewHolderCheckBox.isSelected) {
                isEpoxyModelCheckBox.isSelected = false
                edtJavaEpoxyModelName.isEnabled = false
            }
            generateCode()
        }

        // Epoxy Model
        isEpoxyModelCheckBox.addActionListener {
            edtJavaEpoxyModelName.isEnabled = isEpoxyModelCheckBox.isSelected
            if (isEpoxyModelCheckBox.isSelected) {
                isReclerViewHolderCheckBox.isSelected = false
                edtJavaViewHolderName.isEnabled = false
            }
            generateCode()
        }

        // 输入rootView名称
        edtRootView.document.addDocumentListener(object : DocumentListener {
            override fun changedUpdate(e: DocumentEvent?) {
            }

            override fun insertUpdate(e: DocumentEvent?) {
                if (addRootViewCheckBox.isSelected) {
                    generateCode()
                }
            }

            override fun removeUpdate(e: DocumentEvent?) {
                if (addRootViewCheckBox.isSelected) {
                    generateCode()
                }
            }
        })

        // 输入ViewHolder名称
        edtJavaViewHolderName.document.addDocumentListener(object : DocumentListener {
            override fun changedUpdate(e: DocumentEvent?) {
            }

            override fun insertUpdate(e: DocumentEvent?) {
                if (isReclerViewHolderCheckBox.isSelected) {
                    generateCode()
                }
            }

            override fun removeUpdate(e: DocumentEvent?) {
                if (isReclerViewHolderCheckBox.isSelected) {
                    generateCode()
                }
            }
        })

        // 输入EpoxyModel名称
        edtJavaEpoxyModelName.document.addDocumentListener(object : DocumentListener {
            override fun changedUpdate(e: DocumentEvent?) {
            }

            override fun insertUpdate(e: DocumentEvent?) {
                if (isEpoxyModelCheckBox.isSelected) {
                    generateCode()
                }
            }

            override fun removeUpdate(e: DocumentEvent?) {
                if (isEpoxyModelCheckBox.isSelected) {
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
        kotlinGroup.add(isEpoxyRadioButton)

        // 适配表格
        viewTable.model = mViewTableModel

        generateCode()
    }

    /**
     * 确认
     */
    override fun onOK() {
        tvCode.text.toClipboard()
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
        if (kotlinRadioButton.isSelected) {// kotlin
            val addM = addMCheckBox.isSelected
            val isPrivate = isPrivateCheckBox.isSelected
            val isLocalVariable = isLocalVariableRadioButton.isSelected
            val rootView = when {
                isLocalVariable -> edtKtRootView.text
                isFragmentRadioButton.isSelected -> "view!!"
                else -> ""
            }
            tvCode.text = when {
                isEpoxyRadioButton.isSelected -> // EpoxyModel
                    mViewInfoList.generateKTEpoxyModelCode(file.name.removeSuffix(".xml"),
                            edtKTEpoxyModelName.text ?: "", addM)
                isReclerViewHolderRadioButton.isSelected -> // Recycler ViewHolder
                    mViewInfoList.generateKTViewHolderCode(file.name.removeSuffix(".xml"),
                            edtKTViewHolderName.text ?: "", addM, isPrivate)
                else -> mViewInfoList.generateKTCode(addM, isPrivate, isLocalVariable, rootView)
            }
        } else {// java
            val addM = addMCheckBox.isSelected
            val isPrivate = isPrivateCheckBox.isSelected
            val isTarget26 = isTarget26CheckBox.isSelected
            val isLocalVariable = isLocalVariableCheckBox.isSelected
            val rootView = if (addRootViewCheckBox.isSelected) edtRootView.text else ""

            tvCode.text = when {
                isEpoxyModelCheckBox.isSelected -> // EpoxyModel
                    mViewInfoList.generateJavaEpoxyModelCode(file.name.removeSuffix(".xml"),
                            edtJavaEpoxyModelName.text ?: "", addM, isPrivate, isTarget26)
                isReclerViewHolderCheckBox.isSelected -> // viewHolder
                    mViewInfoList.generateJavaViewHolderCode(file.name.removeSuffix(".xml"),
                            edtJavaViewHolderName.text ?: "", addM, isPrivate, isTarget26)
                else -> mViewInfoList.generateJavaCode(addM, rootView, isPrivate, isTarget26, isLocalVariable)
            }
        }

        // 将光标移动到开始位置（用于控制垂直滚动在代码生成后一直在顶部）
        tvCode.caretPosition = 0
    }
}
