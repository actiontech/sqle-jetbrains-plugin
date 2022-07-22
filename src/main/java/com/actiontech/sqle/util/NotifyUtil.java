/*
 * Copyright 2018 HelloWoodes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.actiontech.sqle.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Optional;

import static com.actiontech.sqle.constant.Constant.*;


public class NotifyUtil {

    public static void showErrorMessageDialog(String title, String message) {
        showMessageDialog(title, message, Messages.getErrorIcon());
    }

    public static void showMessageDialog(String title, String message, Icon icon) {
        ApplicationManager.getApplication().invokeLater(() -> Messages.showMessageDialog(message, title, icon));
    }

    public static String getExceptionMessage(Exception ex) {
        Optional<Exception> optionalException = Optional.of(ex);
        String errorMessage = optionalException.map(cause -> ex.getCause())
                .map(Throwable::getMessage)
                .orElse(ex.getMessage());

        return Optional.ofNullable(errorMessage)
                .orElse(COMMON_ERROR_MESSAGE);
    }

    public static void showTipsDialog(String title, String content) {
        ApplicationManager.getApplication().invokeLater(() -> {
            JEditorPane label = new JEditorPane();
            label.setBackground(null);
            label.setEditable(false);
            label.setText(content);
            label.setSize(DIALOG_SIZE);
            DialogBuilder dialog = new DialogBuilder();
            dialog.setTitle(title);
            dialog.centerPanel(label);
            dialog.addOkAction();
            dialog.show();
        });
    }

    public static void showNotifyPopup(@NotNull JComponent component, String messageContent) {
        if (StringUtils.isNoneBlank(messageContent)) {
            ApplicationManager.getApplication().invokeLater(() -> {
                JBPopupFactory factory = JBPopupFactory.getInstance();
                factory.createHtmlTextBalloonBuilder(messageContent, null, MessageType.WARNING.getPopupBackground(), null)
                        .setShadow(true)
                        .setFadeoutTime(5000)
                        .setDialogMode(true)
                        .setHideOnAction(false)
                        .createBalloon()
                        .show(factory.guessBestPopupLocation(component), Balloon.Position.below);
            });
        }
    }
}
