package com.demo.com.demo;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class NovelWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        NovelWindow novelWindow = new NovelWindow(toolWindow);
        Content content = ContentFactory.getInstance().createContent(novelWindow.getContentPanel(),"", false);
        toolWindow.getContentManager().addContent(content);
    }

    private static class NovelWindow {
        private final JPanel contentPanel = new JPanel();

        private NovelWindow(ToolWindow toolWindow){

        }
        public JPanel getContentPanel() {
            return contentPanel;
        }
    }


}
