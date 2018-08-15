package ui

import bean.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiFile
import extensions.gengrateKTCode
import extensions.toClipboard
import extensions.toViewInfoList
import helper.KtFileWriteHelper

import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * Kotlin文件中生成时使用
 */
class KotlinDialog(
        private val project: Project,
        private val psiFile: PsiFile,
        private val selectedInfo: SelectedInfo,
        list: List<Element>)
    : BaseJDialog() {

    override var contentPane: JPanel? = null
    override var buttonOK: JButton? = null
    override var buttonCancel: JButton? = null
    private var buttonCopyCode: JButton? = null
    private var tvCode: JTextArea? = null
    private var viewTable: JTable? = null
    private var selectAllButton: JButton? = null
    private var selectNoneButton: JButton? = null
    private var selectInvert: JButton? = null
    private var isActivityRadioButton: JRadioButton? = null
    private var isFragmentRadioButton: JRadioButton? = null
    private var isLocalVariableRadioButton: JRadioButton? = null
    private var edtRootView: JTextField? = null
    private var isPrivateCheckBox: JCheckBox? = null
    private var addMCheckBox: JCheckBox? = null

    private var mViewInfoList = list.toViewInfoList()

    private val mViewTableModel = ViewTableModel(mViewInfoList, addMCheckBox!!.isSelected,
            object : ViewTableModel.TableEditListener {
                override fun onEdited() {
                    generateCode()
                }
            }
    )

    init {
        title = "Generate findViewById code (Kotlin)"

        layoutSize(700, 550)

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

        // Fragment
        isFragmentRadioButton!!.addActionListener { generateCode() }

        // Activity
        isActivityRadioButton!!.addActionListener { generateCode() }

        // Local Variable
        isLocalVariableRadioButton!!.addActionListener {
            generateCode()
            edtRootView!!.isEnabled = isLocalVariableRadioButton!!.isSelected
        }

        // Copy Code
        buttonCopyCode!!.addActionListener {
            tvCode!!.text.toClipboard()
            dispose()
        }

        // 输入RootView名称
        edtRootView!!.document.addDocumentListener(object : DocumentListener {
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

    /**
     * 绑定数据到视图
     */
    private fun bindData() {
        // 单选按钮分组
        val group = ButtonGroup()
        group.add(isActivityRadioButton)
        group.add(isFragmentRadioButton)
        group.add(isLocalVariableRadioButton)

        // 适配表格
        viewTable!!.model = mViewTableModel

        generateCode()
    }

    /**
     * 确认
     */
    override fun onOK() {
        try {
            val addM = addMCheckBox!!.isSelected
            val isPrivate = isPrivateCheckBox!!.isSelected
            val isLocalVariable = isLocalVariableRadioButton!!.isSelected
            val rootView = when {
                isLocalVariable -> edtRootView!!.text
                isFragmentRadioButton!!.isSelected -> "view!!"
                else -> ""
            }

            KtFileWriteHelper<Any>(project, psiFile,selectedInfo, mViewInfoList, addM, isPrivate,isLocalVariable, rootView)
                    .execute()
            dispose()
        } catch (e: Exception) {
            Messages.showErrorDialog(e.message, "Generate code error")
        }
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
        val addM = addMCheckBox!!.isSelected
        val isPrivate = isPrivateCheckBox!!.isSelected
        val isLocalVariable = isLocalVariableRadioButton!!.isSelected
        val rootView = when {
            isLocalVariable -> edtRootView!!.text
            isFragmentRadioButton!!.isSelected -> "view!!"
            else -> ""
        }

        tvCode!!.text = mViewInfoList.gengrateKTCode(addM, isPrivate, isLocalVariable, rootView)

        // 将光标移动到开始位置（用于控制垂直滚动在代码生成后一直在顶部）
        tvCode!!.caretPosition = 0
    }

}
