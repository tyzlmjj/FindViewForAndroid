package ui

import java.awt.Dimension
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*

/**
 * Dialog基类
 */
abstract class BaseJDialog : JDialog(){

    abstract var contentPane: JPanel?
    abstract var buttonOK: JButton?
    abstract var buttonCancel: JButton?

    abstract fun onOK()

    abstract fun onCancel()
}

/**
 * 初始化
 */
fun BaseJDialog.init(){

    this.setContentPane(contentPane)
    this.isModal = true
    this.rootPane.defaultButton = buttonOK

    // 确认按钮
    buttonOK?.addActionListener { onOK() }

    // 取消按钮
    buttonCancel?.addActionListener { onCancel() }

    // 窗体关闭事件
    defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
    this.addWindowListener(object : WindowAdapter() {
        override fun windowClosing(e: WindowEvent?) {
            onCancel()
        }
    })

    // 键盘ESC事件
    contentPane?.registerKeyboardAction({ onCancel() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
}


/**
 * 设置Dialog大小,并居中显示
 */
fun JDialog.layoutSize(width:Int,height:Int){
    val dimension = Dimension(width, height)
    preferredSize = dimension
    size = dimension
    setLocationRelativeTo(null)
}