package me.majiajie.fvfa.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiFile
import me.majiajie.fvfa.extensions.generateJavaCode
import me.majiajie.fvfa.extensions.toClipboard
import me.majiajie.fvfa.extensions.toViewInfoList
import me.majiajie.fvfa.helper.JavaFileWriteHelper
import me.majiajie.fvfa.bean.Element
import me.majiajie.fvfa.bean.SelectedInfo
import me.majiajie.fvfa.ui.BaseJDialog
import me.majiajie.fvfa.ui.ViewTableModel
import me.majiajie.fvfa.ui.init
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class JavaDialog(private val project: Project,
                 private val selectedInfo: SelectedInfo,
                 private val psiFile: PsiFile, list: List<Element>) : BaseJDialog() {

    override lateinit var contentPane: JPanel
    override lateinit var buttonOK: JButton
    override lateinit var buttonCancel: JButton
    private lateinit var buttonCopyCode: JButton
    private lateinit var tvCode: JTextArea
    private lateinit var viewTable: JTable
    private lateinit var selectAllButton: JButton
    private lateinit var selectNoneButton: JButton
    private lateinit var selectInvert: JButton
    private lateinit var isPrivateCheckBox: JCheckBox
    private lateinit var addMCheckBox: JCheckBox

    private lateinit var addRootViewCheckBox: JCheckBox
    private lateinit var edtRootView: JTextField

    private lateinit var isTarget26CheckBox: JCheckBox
    private lateinit var isLocalVariableCheckBox: JCheckBox

    private var mViewInfoList = list.toViewInfoList()

    private val mViewTableModel = ViewTableModel(mViewInfoList, addMCheckBox.isSelected,
            object : ViewTableModel.TableEditListener {
                override fun onEdited() {
                    generateCode()
                }
            }
    )

    init {
        title = "Generate findViewById code (JAVA)"

        init()

        initEvent()

        bindData()
    }

    /**
     * 初始化事件
     */
    private fun initEvent() {

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

        // 复制代码
        buttonCopyCode.addActionListener {
            tvCode.text.toClipboard()
            dispose()
        }

    }

    /**
     * 绑定数据到视图
     */
    private fun bindData() {
        // 适配表格
        viewTable.model = mViewTableModel
        generateCode()
    }

    /**
     * 确认
     */
    override fun onOK() {
        try {
            val addM = addMCheckBox.isSelected
            val isPrivate = isPrivateCheckBox.isSelected
            val rootView = if (addRootViewCheckBox.isSelected) edtRootView.text else ""
            val isTarget26 = isTarget26CheckBox.isSelected
            val isLocalVariable = isLocalVariableCheckBox.isSelected
            JavaFileWriteHelper<Any>(project, psiFile, selectedInfo, mViewInfoList, addM, isPrivate, rootView, isTarget26, isLocalVariable)
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
        val addM = addMCheckBox.isSelected
        val isPrivate = isPrivateCheckBox.isSelected
        val rootView = if (addRootViewCheckBox.isSelected) edtRootView.text else ""
        val isTarget26 = isTarget26CheckBox.isSelected
        val isLocalVariable = isLocalVariableCheckBox.isSelected
        tvCode.text = mViewInfoList.generateJavaCode(addM, rootView, isPrivate, isTarget26, isLocalVariable)

        // 将光标移动到开始位置（用于控制垂直滚动在代码生成后一直在顶部）
        tvCode.caretPosition = 0
    }

}
