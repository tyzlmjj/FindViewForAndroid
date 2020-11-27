package me.majiajie.fvfa.helper

import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class DefaultDocumentListener(private val change:() -> Unit) : DocumentListener {

    override fun insertUpdate(e: DocumentEvent?) {
        change.invoke()
    }

    override fun removeUpdate(e: DocumentEvent?) {
        change.invoke()
    }

    override fun changedUpdate(e: DocumentEvent?) {

    }

}