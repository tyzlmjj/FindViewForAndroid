package ui

import bean.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import extensions.gengrateKTCode
import extensions.toClipboard
import extensions.toKtProperty
import extensions.toViewInfoList
import org.jetbrains.kotlin.psi.KtClass
import utils.KtFileWriteUtils

import javax.swing.*

/**
 * Kotlin文件中生成时使用
 */
class KotlinDialog(val project: Project,val psiFile: PsiFile, list: List<Element>) : BaseJDialog() {

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
    private var isPrivateCheckBox: JCheckBox? = null
    private var addMCheckBox: JCheckBox? = null

    private var mViewInfoList = list.toViewInfoList()

    private val mViewTableModel = ViewTableModel(mViewInfoList, addMCheckBox!!.isSelected,
            object: ViewTableModel.TableEditListener {
                override fun onEdited() {
                    generateCode()
                }
            }
    )

    init {
        title = "Generate findViewById code (Kotlin)"

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

        // Fragment
        isFragmentRadioButton!!.addActionListener { generateCode() }

        // Activity
        isActivityRadioButton!!.addActionListener { generateCode() }

        // Copy Code
        buttonCopyCode!!.addActionListener {
            tvCode!!.text.toClipboard()
            dispose()
        }
    }

    /**
     * 绑定数据到视图
     */
    private fun bindData() {
        // 单选按钮分组
        val group = ButtonGroup()
        group.add(isActivityRadioButton)
        group.add(isFragmentRadioButton)

        // 适配表格
        viewTable!!.model = mViewTableModel

        generateCode()
    }

    /**
     * 确认
     */
    override fun onOK() {

        val psiClass:KtClass? = PsiTreeUtil.findChildOfAnyType(psiFile, KtClass::class.java)
        if(psiClass == null){
            Messages.showErrorDialog(project, "无法找到类文件", "错误")
        } else {
            KtFileWriteUtils.addPropertyToKtClass(project,psiClass,mViewInfoList.toKtProperty(project,addMCheckBox!!.isSelected, isPrivateCheckBox!!.isSelected, if (isFragmentRadioButton!!.isSelected) "view" else ""))
            dispose()
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
        tvCode!!.text = mViewInfoList.gengrateKTCode(addMCheckBox!!.isSelected, isPrivateCheckBox!!.isSelected, if (isFragmentRadioButton!!.isSelected) "view" else "")
    }

}
