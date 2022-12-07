package com.actiontech.sqle.action;

import com.actiontech.sqle.util.HttpClientUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AuditSQLFile extends AnAction {
    private PsiFile file;
    private String fileName;

    @Override
    public void update(@NotNull AnActionEvent e) {
        this.file = e.getData(CommonDataKeys.PSI_FILE);
        this.fileName = this.file.getOriginalFile().getName();

        if (!this.fileName.endsWith(".sql")) {
            e.getPresentation().setEnabled(false);
            return;
        }
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        String text = Objects.requireNonNull(file.getViewProvider().getDocument()).getText();
        Audit.Audit(e, text, HttpClientUtil.AuditType.SQL);
    }
}