package at.shockbytes.plugin.view

import javax.swing.*
import java.awt.*

/**
 * Author:  Mescht
 * Date:    17.08.2016
 */
class MaterialListCellRenderer : DefaultListCellRenderer() {

    override fun getListCellRendererComponent(list: JList<*>, value: Any?,
                                              index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
        val c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
        if (isSelected) {
            // old #80CBC4
            c.background = Color.decode("#2196F3")
            c.foreground = Color.decode("#323232")
        }
        return c
    }

}
