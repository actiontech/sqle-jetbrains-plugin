package com.actiontech.sqle.action;

import com.actiontech.sqle.util.HttpClientUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;

import com.intellij.ide.highlighter.XmlFileType;

public class AuditMyBatis extends AnAction {
    private ArrayList<String> filePaths;

    @Override
    public void update(@NotNull AnActionEvent e) {
        filePaths = new ArrayList<>();
        VirtualFile vFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (vFile == null) {
            return;
        }
        System.out.println("test log");
        String path = vFile.getPath();
        if (vFile.isDirectory()) {
            gatherFilesFromDir(path);
        } else if (vFile.getFileType().getClass() == XmlFileType.class) {
            addFilePath(path);
        } else {
            e.getPresentation().setEnabled(false);
        }
    }

    private void addFilePath(String path) {
        if (filePaths.contains(path)) {
            return;
        }
        filePaths.add(path);
    }

    private void gatherFilesFromDir(String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            File dir = new File(path);
            File[] files = dir.listFiles();
            if (files == null) {
                return;
            }
            for (File f : files) {
                if (f.isDirectory()) {
                    gatherFilesFromDir(f.getPath());
                } else if (f.getName().endsWith(".xml")) {
                    addFilePath(f.getPath());
                }
            }
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        ArrayList<String> texts = new ArrayList<>();
        for (String filePath : filePaths) {
            try {
                String text = Files.readString(Path.of(filePath));
                texts.add(text);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Audit.Audit(event, texts.toArray(new String[0]), HttpClientUtil.AuditType.MyBatis);
    }
}