package com.actiontech.sqle.util;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class NewWindow extends JFrame {
    public NewWindow() {
        // 设置新窗口的标题
        setTitle("知识库");
        // 设置新窗口的大小和位置
        setSize(720, 450);
        // 居中显示
        setLocationRelativeTo(null);
    }
}