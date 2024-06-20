// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.demo.com.demo;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

final class CalendarToolWindowFactory implements ToolWindowFactory, DumbAware {
  private static ArrayList<String> fileNames = new ArrayList<>();
  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    CalendarToolWindowContent toolWindowContent = new CalendarToolWindowContent(toolWindow);
    Content content = ContentFactory.getInstance().createContent(toolWindowContent.getContentPanel(), "", false);
    toolWindow.getContentManager().addContent(content);
    TimerTask timerTask = new TimerTask() {
      @Override
      public void run() {
        toolWindowContent.updateCurrentDateTime(false);
      }
    };
    Timer timer = new Timer();
    timer.schedule(timerTask, 60*1000, 60*1000);
  }

  private static class CalendarToolWindowContent {
    private String filePath;

    private final JPanel contentPanel = new JPanel();
    private final JLabel currentDate = new JLabel();
    private final JLabel timeZone = new JLabel();
    private final JLabel currentTime = new JLabel();
    private final JTextArea content = new JTextArea();
    private final JLabel fileName = new JLabel();
    private final JBTextField jbTextField = new JBTextField();

    public CalendarToolWindowContent(ToolWindow toolWindow) {
      contentPanel.setLayout(new BorderLayout(0, 0));
      contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
      contentPanel.add(createCalendarPanel(), BorderLayout.PAGE_START);
      contentPanel.add(createControlsPanel(toolWindow), BorderLayout.CENTER);
      contentPanel.add(createTextPanel(), BorderLayout.PAGE_END);
      jbTextField.setText("0");
      updateCurrentDateTime(true);
    }
    private JPanel createTextPanel(){
      JPanel textPanel = new JPanel();
      JBScrollPane jbScrollPane = new JBScrollPane(content);
      content.setFont(new Font("微软雅黑", Font.PLAIN, 15));
      jbScrollPane.setPreferredSize(new Dimension(1000,40));
      jbScrollPane.setMaximumSize(new Dimension(200,40));
      jbScrollPane.setMinimumSize(new Dimension(200,40));
      textPanel.add(jbScrollPane);
      textPanel.add(fileName);
      textPanel.add(jbTextField);
      return textPanel;
    }
    private void updateText(Project project) {
      FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor();
      // 显示文件选择对话框
      FileChooser.chooseFile(descriptor, project, null, files -> {
        // 用户选择完文件后的回调
        if (files.exists()) {
          File realFile = new File(files.getPath());
          FileUtil.collectFiles(realFile,fileNames);
            turnToPage(0);
        }
      });
    }
    @NotNull
    private JPanel createCalendarPanel() {
      JPanel calendarPanel = new JPanel();
      calendarPanel.add(currentDate);
      calendarPanel.add(timeZone);
      calendarPanel.add(currentTime);
      return calendarPanel;
    }

    @NotNull
    private JPanel createControlsPanel(ToolWindow toolWindow) {
      JPanel controlsPanel = new JPanel();
      JButton lastButton = new JButton("Last");
      lastButton.addActionListener(e -> {
        int pageNum = Integer.parseInt(jbTextField.getText());
        turnToPage(pageNum-1);
      });
      controlsPanel.add(lastButton);

      JButton refreshDateAndTimeButton = new JButton("Refresh");
      refreshDateAndTimeButton.addActionListener(e -> updateCurrentDateTime(false));
      controlsPanel.add(refreshDateAndTimeButton);

      JButton hideToolWindowButton = new JButton("Hide");
      hideToolWindowButton.addActionListener(e -> toolWindow.hide(null));
      controlsPanel.add(hideToolWindowButton);

      JButton fileChooserButton = new JButton("FileChooser");
      fileChooserButton.addActionListener(e -> updateText(toolWindow.getProject()));
      controlsPanel.add(fileChooserButton);

      JButton nextButton = new JButton("Next");
      nextButton.addActionListener(e -> {
        int pageNum = Integer.parseInt(jbTextField.getText());
        turnToPage(pageNum+1);
      });
      controlsPanel.add(nextButton);
      return controlsPanel;
    }

    private void updateCurrentDateTime(Boolean init) {
      Calendar calendar = Calendar.getInstance();
      currentDate.setText(getCurrentDate(calendar));
      timeZone.setText(getTimeZone(calendar));
      currentTime.setText(getCurrentTime(calendar));
      if(!init){
        int pageNum = Integer.parseInt(jbTextField.getText());
        turnToPage(pageNum);
      }
    }

    private String getCurrentDate(Calendar calendar) {
      return calendar.get(Calendar.DAY_OF_MONTH) + "/"
          + (calendar.get(Calendar.MONTH) + 1) + "/"
          + calendar.get(Calendar.YEAR);
    }

    private String getTimeZone(Calendar calendar) {
      long gmtOffset = calendar.get(Calendar.ZONE_OFFSET); // offset from GMT in milliseconds
      String gmtOffsetString = String.valueOf(gmtOffset / 3600000);
      return (gmtOffset > 0) ? "GMT + " + gmtOffsetString : "GMT - " + gmtOffsetString;
    }

    private String getCurrentTime(Calendar calendar) {
      return getFormattedValue(calendar, Calendar.HOUR_OF_DAY) + ":" + getFormattedValue(calendar, Calendar.MINUTE);
    }

    private String getFormattedValue(Calendar calendar, int calendarField) {
      int value = calendar.get(calendarField);
      return StringUtils.leftPad(Integer.toString(value), 2, "0");
    }

    public JPanel getContentPanel() {
      return contentPanel;
    }
    public void turnToPage(int page){
      if (page<0 || page>=fileNames.size()){
        return;
      }
      jbTextField.setText(String.valueOf(page));
      String path = fileNames.get(page);
      filePath = path;
      StringBuilder sb = new StringBuilder();
      try(BufferedReader br = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8))){
        String line;
        while((line = br.readLine()) != null){
          sb.append(line);
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      content.setText(sb.toString());
      fileName.setText(path.substring(filePath.lastIndexOf("\\") + 1));
    }
}


}
