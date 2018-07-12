package ui

import javax.swing.JButton
import javax.swing.JPanel

class JavaDialog : BaseJDialog() {

    override var contentPane: JPanel? = null
    override var buttonOK: JButton? = null
    override var buttonCancel: JButton? = null

    override fun onOK() {}

    override fun onCancel() {}

    init {
        init()
    }

}
