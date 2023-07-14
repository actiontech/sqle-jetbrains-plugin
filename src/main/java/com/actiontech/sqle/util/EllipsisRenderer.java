package com.actiontech.sqle.util;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class EllipsisRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setToolTipText(value.toString()); // 设置鼠标提示文本以显示完整文本
        label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5)); // 设置边框以提高可读性
        label.setMaximumSize(new Dimension(7, 7)); // 设置列宽
        label.setPreferredSize(new Dimension(7, 7)); // 设置列宽
        label.setHorizontalAlignment(SwingConstants.LEFT); // 设置文本左对齐
        label.setOpaque(true);
        return label;
    }
}