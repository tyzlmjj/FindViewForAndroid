package me.majiajie.fvfa.ui

import com.intellij.psi.PsiFile
import me.majiajie.fvfa.bean.Element
import me.majiajie.fvfa.extensions.*
import me.majiajie.fvfa.helper.DefaultDocumentListener

import javax.swing.*
import java.awt.event.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class XMLDialog(private val file: PsiFile, list: List<Element>) : BaseJDialog() {

    override lateinit var contentPane: JPanel
    override lateinit var buttonOK: JButton
    override lateinit var buttonCancel: JButton
    private lateinit var viewsPane: JScrollPane
    private lateinit var selectButtonsPane: JPanel
    private lateinit var isPrivateCheckBox: JCheckBox
    private lateinit var addMCheckBox: JCheckBox
    private lateinit var kotlinRadioButton: JRadioButton
    private lateinit var javaRadioButton: JRadioButton
    private lateinit var tvCode: JTextArea
    private lateinit var viewTable: JTable
    private lateinit var selectAllButton: JButton
    private lateinit var selectNoneButton: JButton
    private lateinit var selectInvert: JButton

    // kt
    private lateinit var kotlinPanel: JPanel
    private lateinit var kotlinPanelViewBinding: JPanel
    private lateinit var isActivityRadioButton: JRadioButton
    private lateinit var isFragmentRadioButton: JRadioButton
    private lateinit var isLocalVariableRadioButton: JRadioButton
    private lateinit var edtKtRootView: JTextField
    private lateinit var isReclerViewHolderRadioButton: JRadioButton
    private lateinit var isReclerViewHolderViewBindingRadioButton: JRadioButton
    private lateinit var isEpoxyRadioButton: JRadioButton
    private lateinit var isEpoxyViewBindingRadioButton: JRadioButton
    private lateinit var edtKTViewHolderName: JTextField
    private lateinit var edtKTViewHolderViewBindingName: JTextField
    private lateinit var edtKTEpoxyModelName: JTextField
    private lateinit var edtKTEpoxyModelViewBindingName: JTextField

    // java
    private lateinit var javaPanel: JPanel
    private lateinit var javaPanelViewBinding: JPanel
    private lateinit var addRootViewCheckBox: JCheckBox
    private lateinit var edtRootView: JTextField
    private lateinit var isReclerViewHolderCheckBox: JCheckBox
    private lateinit var isReclerViewHolderViewBindingCheckBox: JCheckBox
    private lateinit var isEpoxyModelCheckBox: JCheckBox
    private lateinit var isEpoxyModelViewBindingCheckBox: JCheckBox
    private lateinit var isTarget26CheckBox: JCheckBox
    private lateinit var isLocalVariableCheckBox: JCheckBox
    private lateinit var edtJavaViewHolderName: JTextField
    private lateinit var edtJavaViewHolderViewBindingName: JTextField
    private lateinit var edtJavaEpoxyModelName: JTextField
    private lateinit var edtJavaEpoxyModelViewBindingName: JTextField

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
        javaPanelViewBinding.isVisible = false

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
            kotlinPanelViewBinding.isVisible = true
            javaPanel.isVisible = false
            javaPanelViewBinding.isVisible = false
            useViewBinding(isReclerViewHolderViewBindingRadioButton.isSelected || isEpoxyViewBindingRadioButton.isSelected)
            generateCode()
        }

        // 选择Java
        javaRadioButton.addActionListener {
            kotlinPanel.isVisible = false
            kotlinPanelViewBinding.isVisible = false
            javaPanel.isVisible = true
            javaPanelViewBinding.isVisible = true
            useViewBinding(isReclerViewHolderViewBindingCheckBox.isSelected || isEpoxyModelViewBindingCheckBox.isSelected)
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
            edtKtRootView.isEnabled = isLocalVariableRadioButton.isSelected
            edtKTViewHolderName.isEnabled = isReclerViewHolderRadioButton.isSelected
            edtKTEpoxyModelName.isEnabled = isEpoxyRadioButton.isSelected
            edtKTViewHolderViewBindingName.isEnabled = isReclerViewHolderViewBindingRadioButton.isSelected
            edtKTEpoxyModelViewBindingName.isEnabled = isEpoxyViewBindingRadioButton.isSelected

            if (isReclerViewHolderRadioButton.isSelected || isEpoxyRadioButton.isSelected) {// ViewHolder or EpoxyModel
                isPrivateCheckBox.isSelected = false
                addMCheckBox.isSelected = false
                mViewTableModel.setAddM(addMCheckBox.isSelected)
                mViewTableModel.fireTableDataChanged()
            }

            if (isReclerViewHolderViewBindingRadioButton.isSelected || isEpoxyViewBindingRadioButton.isSelected) {
                isPrivateCheckBox.isSelected = false
                useViewBinding(true)
            } else {
                useViewBinding(false)
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

        // ReclerViewHolder(ViewBinding)
        isReclerViewHolderViewBindingRadioButton.addActionListener(kotlinTypeAction)

        // EpoxyModel(ViewBinding)
        isEpoxyViewBindingRadioButton.addActionListener(kotlinTypeAction)

        // 输入KtRootView名称
        edtKtRootView.document.addDocumentListener(DefaultDocumentListener {
            if (isLocalVariableRadioButton.isSelected) {
                generateCode()
            }
        })

        // 输入ViewHolder名称
        edtKTViewHolderName.document.addDocumentListener(DefaultDocumentListener {
            if (isReclerViewHolderRadioButton.isSelected) {
                generateCode()
            }
        })

        // 输入EpoxyModel名称
        edtKTEpoxyModelName.document.addDocumentListener(DefaultDocumentListener {
            if (isEpoxyRadioButton.isSelected) {
                generateCode()
            }
        })

        // 输入ViewHolder(ViewBinding)名称
        edtKTViewHolderViewBindingName.document.addDocumentListener(DefaultDocumentListener {
            if (isReclerViewHolderViewBindingRadioButton.isSelected) {
                generateCode()
            }
        })

        // 输入EpoxyModel(ViewBinding)名称
        edtKTEpoxyModelViewBindingName.document.addDocumentListener(DefaultDocumentListener {
            if (isEpoxyViewBindingRadioButton.isSelected) {
                generateCode()
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

//        val javaTypeAction = ActionListener {
//            it.id == isReclerViewHolderCheckBox.uiClassID
//            edtJavaViewHolderName.isEnabled = isReclerViewHolderCheckBox.isSelected
//            edtJavaEpoxyModelName.isEnabled = isEpoxyModelCheckBox.isSelected
//
//            generateCode()
//        }

        // ReclerViewHolder
        isReclerViewHolderCheckBox.addActionListener {
            edtJavaViewHolderName.isEnabled = isReclerViewHolderCheckBox.isSelected
            if (isReclerViewHolderCheckBox.isSelected) {
                isEpoxyModelCheckBox.isSelected = false
                edtJavaEpoxyModelName.isEnabled = false

                isReclerViewHolderViewBindingCheckBox.isSelected = false
                edtJavaViewHolderViewBindingName.isEnabled = false
                isEpoxyModelViewBindingCheckBox.isSelected = false
                edtJavaEpoxyModelViewBindingName.isEnabled = false
            }
            useViewBinding(false)
            generateCode()
        }

        // Epoxy Model
        isEpoxyModelCheckBox.addActionListener {
            edtJavaEpoxyModelName.isEnabled = isEpoxyModelCheckBox.isSelected
            if (isEpoxyModelCheckBox.isSelected) {
                isReclerViewHolderCheckBox.isSelected = false
                edtJavaViewHolderName.isEnabled = false

                isReclerViewHolderViewBindingCheckBox.isSelected = false
                edtJavaViewHolderViewBindingName.isEnabled = false
                isEpoxyModelViewBindingCheckBox.isSelected = false
                edtJavaEpoxyModelViewBindingName.isEnabled = false
            }
            useViewBinding(false)
            generateCode()
        }

        // ReclerViewHolder(ViewBinding)
        isReclerViewHolderViewBindingCheckBox.addActionListener {
            edtJavaViewHolderViewBindingName.isEnabled = isReclerViewHolderViewBindingCheckBox.isSelected
            if (isReclerViewHolderViewBindingCheckBox.isSelected) {
                isEpoxyModelCheckBox.isSelected = false
                edtJavaEpoxyModelName.isEnabled = false
                isReclerViewHolderCheckBox.isSelected = false
                edtJavaViewHolderName.isEnabled = false

                isEpoxyModelViewBindingCheckBox.isSelected = false
                edtJavaEpoxyModelViewBindingName.isEnabled = false
            }
            useViewBinding(isReclerViewHolderViewBindingCheckBox.isSelected)
            generateCode()
        }

        // Epoxy Model(ViewBinding)
        isEpoxyModelViewBindingCheckBox.addActionListener {
            edtJavaEpoxyModelViewBindingName.isEnabled = isEpoxyModelViewBindingCheckBox.isSelected
            if (isEpoxyModelViewBindingCheckBox.isSelected) {
                isEpoxyModelCheckBox.isSelected = false
                edtJavaEpoxyModelName.isEnabled = false
                isReclerViewHolderCheckBox.isSelected = false
                edtJavaViewHolderName.isEnabled = false

                isReclerViewHolderViewBindingCheckBox.isSelected = false
                edtJavaViewHolderViewBindingName.isEnabled = false
            }
            useViewBinding(isEpoxyModelViewBindingCheckBox.isSelected)
            generateCode()
        }

        // 输入rootView名称
        edtRootView.document.addDocumentListener(DefaultDocumentListener {
            if (addRootViewCheckBox.isSelected) {
                generateCode()
            }
        })

        // 输入ViewHolder名称
        edtJavaViewHolderName.document.addDocumentListener(DefaultDocumentListener {
            if (isReclerViewHolderCheckBox.isSelected) {
                generateCode()
            }
        })

        // 输入EpoxyModel名称
        edtJavaEpoxyModelName.document.addDocumentListener(DefaultDocumentListener {
            if (isEpoxyModelCheckBox.isSelected) {
                generateCode()
            }
        })

        // 输入ViewHolder(ViewBinding)名称
        edtJavaViewHolderViewBindingName.document.addDocumentListener(DefaultDocumentListener {
            if (isReclerViewHolderViewBindingCheckBox.isSelected) {
                generateCode()
            }
        })

        // 输入EpoxyModel(ViewBinding)名称
        edtJavaEpoxyModelViewBindingName.document.addDocumentListener(DefaultDocumentListener {
            if (isEpoxyModelViewBindingCheckBox.isSelected) {
                generateCode()
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
        kotlinGroup.add(isReclerViewHolderViewBindingRadioButton)
        kotlinGroup.add(isEpoxyRadioButton)
        kotlinGroup.add(isEpoxyViewBindingRadioButton)

        // 适配表格
        viewTable.model = mViewTableModel

        generateCode()
    }

    /**
     * 设置ViewBinding的使用，更新布局
     */
    private fun useViewBinding(viewBinding: Boolean) {
        viewsPane.isVisible = !viewBinding
        selectButtonsPane.isVisible = !viewBinding
        pack()
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
        val addM = addMCheckBox.isSelected
        val isPrivate = isPrivateCheckBox.isSelected
        if (kotlinRadioButton.isSelected) {// kotlin
            val isLocalVariable = isLocalVariableRadioButton.isSelected
            val rootView = when {
                isLocalVariable -> edtKtRootView.text
                isFragmentRadioButton.isSelected -> "view!!"
                else -> ""
            }
            tvCode.text = when {
                isReclerViewHolderRadioButton.isSelected -> {// Recycler ViewHolder
                    mViewInfoList.generateKTViewHolderCode(file.name.removeSuffix(".xml"),
                            edtKTViewHolderName.text ?: "", addM, isPrivate)
                }
                isEpoxyRadioButton.isSelected -> {// EpoxyModel
                    mViewInfoList.generateKTEpoxyModelCode(file.name.removeSuffix(".xml"),
                            edtKTEpoxyModelName.text ?: "", addM)
                }
                isReclerViewHolderViewBindingRadioButton.isSelected -> {// Recycler ViewHolder(ViewBinding)
                    file.name.removeSuffix(".xml").generateKTViewHolderViewBindingCode(edtKTViewHolderViewBindingName.text
                            ?: "")
                }
                isEpoxyViewBindingRadioButton.isSelected -> {// EpoxyModel(ViewBinding)
                    file.name.removeSuffix(".xml").generateKTEpoxyModelViewBindingCode(edtKTEpoxyModelViewBindingName.text
                            ?: "")
                }
                else -> {
                    mViewInfoList.generateKTCode(addM, isPrivate, isLocalVariable, rootView)
                }
            }
        } else {// java
            val isTarget26 = isTarget26CheckBox.isSelected
            val isLocalVariable = isLocalVariableCheckBox.isSelected
            val rootView = if (addRootViewCheckBox.isSelected) edtRootView.text else ""

            tvCode.text = when {
                isReclerViewHolderCheckBox.isSelected -> {// viewHolder
                    mViewInfoList.generateJavaViewHolderCode(file.name.removeSuffix(".xml"),
                            edtJavaViewHolderName.text ?: "", addM, isPrivate, isTarget26)
                }
                isEpoxyModelCheckBox.isSelected -> {// EpoxyModel
                    mViewInfoList.generateJavaEpoxyModelCode(file.name.removeSuffix(".xml"),
                            edtJavaEpoxyModelName.text ?: "", addM, isPrivate, isTarget26)
                }
                isReclerViewHolderViewBindingCheckBox.isSelected -> {// viewHolder(ViewBinding)
                    file.name.removeSuffix(".xml").generateJavaViewHolderViewBindingCode(edtJavaViewHolderViewBindingName.text
                            ?: "", isPrivate)
                }
                isEpoxyModelViewBindingCheckBox.isSelected -> {// EpoxyModel(ViewBinding)
                    file.name.removeSuffix(".xml").generateJavaEpoxyModelViewBindingCode(
                            edtJavaEpoxyModelViewBindingName.text ?: "", isPrivate)
                }
                else -> {
                    mViewInfoList.generateJavaCode(addM, rootView, isPrivate, isTarget26, isLocalVariable)
                }
            }
        }

        // 将光标移动到开始位置（用于控制垂直滚动在代码生成后一直在顶部）
        tvCode.caretPosition = 0
    }
}
