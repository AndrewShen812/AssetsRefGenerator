package com.shenyong.flutter.settings;

import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.ui.FormBuilder;
import com.shenyong.flutter.Constants;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionListener;

/**
 * @author shenyong
 * @date 2022/12/28
 */
public class PluginSettingsComponent {

    private final JPanel myMainPanel;
    private final JBTextField etFilePath = new JBTextField(Constants.DEF_FILE_PATH, 32);
    private final JBTextField etClsName = new JBTextField(Constants.DEF_CLS_NAME, 32);
    private final JBRadioButton rbSnakeCase = new JBRadioButton("Snake Case(eg: first_name, last_name)");
    private final JBRadioButton rbCamelCase = new JBRadioButton("Camel Case(eg: firstName, lastName)");
    private final ButtonGroup rbGroup = new ButtonGroup();
    private final JBTextArea tvExample = new JBTextArea("", 8, 24);
    private String curRbCmd = rbSnakeCase.getText();

    public PluginSettingsComponent() {
        JButton btnReset = new JButton("Reset to Default");
        btnReset.addActionListener(e -> resetToDefault());

        myMainPanel = FormBuilder.createFormBuilder()
                .addComponent(btnReset, 1)
                .addComponent(titlePanel("Generating Settings", parent -> {
                    JPanel panel = FormBuilder.createFormBuilder()
                            .addLabeledComponent(new JBLabel("File Path(Relative to Project): "), etFilePath, 1, false)
                            .addLabeledComponent(new JBLabel("Class Name: "), etClsName, 1, false)
                            .getPanel();
                    parent.add(panel);
                }))
                .addComponent(titlePanel("Naming Conventions", parent -> {
                    rbGroup.add(rbSnakeCase);
                    rbGroup.add(rbCamelCase);

                    HyperlinkLabel linkLabel = new HyperlinkLabel();
                    linkLabel.setHyperlinkText("According to the ", "Dart language guide",
                            ", we should PREFER using lowerCamelCase for constant ");
                    linkLabel.setHyperlinkTarget("https://dart.dev/guides/language/effective-dart/style#prefer-using-lowercamelcase-for-constant-names");
                    JPanel panel = FormBuilder.createFormBuilder()
                            .addComponent(linkLabel, 1)
                            .addComponent(new JBLabel("names. But for historical reasons, the snake case option is kept here to maintain compatibility "), 1)
                            .addComponent(new JBLabel("with older versions."), 1)
                            .addVerticalGap(JBUIScale.scale(4))
                            .addComponent(rbCamelCase, 1)
                            .addComponent(rbSnakeCase, 1)
                            .getPanel();
                    parent.add(panel);
                }))
                .addComponent(titlePanel("Example", false, parent -> {
                    tvExample.setEditable(false);
                    parent.add(tvExample);
                }))
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
        etClsName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateExample();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateExample();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateExample();
            }
        });
        ActionListener rbListener = e -> {
            String actionCmd = e.getActionCommand();
            if (!actionCmd.equals(curRbCmd)) {
                System.out.println("actionCommand: " + actionCmd);
                curRbCmd = actionCmd;
                updateExample();
            }
        };
        rbSnakeCase.addActionListener(rbListener);
        rbCamelCase.addActionListener(rbListener);
    }

    private void resetToDefault() {
        etFilePath.setText(Constants.DEF_FILE_PATH);
        etClsName.setText(Constants.DEF_CLS_NAME);
        rbSnakeCase.setSelected(Constants.IS_SNAKE_CASE);
        rbCamelCase.setSelected(!Constants.IS_SNAKE_CASE);
        curRbCmd = rbSnakeCase.getText();
        updateExample();
    }


    private void updateExample() {
        String clsText = etClsName.getText().trim();
        if (!clsText.isEmpty()) {
            tvExample.setText(genExample());
        }
    }

    private String genExample() {
        boolean isSnakeCase = rbSnakeCase.isSelected();
        String resField1;
        String resField2;
        if (isSnakeCase) {
            resField1 = "ic_user";
            resField2 = "img_splash_bg";
        } else {
            resField1 = "icUser";
            resField2 = "imgSplashBg";
        }
        return "/// Generated by AssetsRefGenerator - DO NOT MODIFY BY HAND\n" +
                "class " + etClsName.getText() + " {\n" +
                "  static const String " + resField1 + " = \"assets/images/ic_user.png\";\n" +
                "  static const String " + resField2 + " = \"assets/images/img_splash_bg.png\";\n" +
                "  // ...\n" +
                "}\n";
    }

    private interface BodyCreator {
        /**
         * 提供一个JPanel类型的父容器，实现类填充自定义的内容
         * @param parent 父容器
         */
        void createBody(JPanel parent);
    }

    private JComponent titlePanel(String title, BodyCreator bodyCreator) {
        return titlePanel(title, false, bodyCreator);
    }

    private JComponent titlePanel(String title, boolean fill, BodyCreator bodyCreator) {
        JPanel innerPanel = new JPanel(UI.migLayout(String.valueOf(JBUIScale.scale(4))));
        bodyCreator.createBody(innerPanel);
        JPanel titlePanel = new JPanel(UI.migLayout());
        titlePanel.setBorder(IdeBorderFactory.createTitledBorder(title));
        if (fill) {
            titlePanel.add(innerPanel, UI.fillX());
        } else {
            titlePanel.add(innerPanel);
            titlePanel.add(new JPanel(), UI.fillX());
        }
        return titlePanel;
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public String getFilePath() {
        return etFilePath.getText().trim();
    }

    public void setFilePath(String filePath) {
        etFilePath.setText(filePath);
    }

    public String getClassName() {
        return etClsName.getText().trim();
    }

    public void setClassName(String filePath) {
        etClsName.setText(filePath);
        updateExample();
    }

    public boolean isSnakeCase() {
        return rbSnakeCase.isSelected();
    }

    public void setSnakeCase(boolean selected) {
        if (selected) {
            curRbCmd = rbSnakeCase.getText();
        } else {
            curRbCmd = rbCamelCase.getText();
        }
        rbSnakeCase.setSelected(selected);
        rbCamelCase.setSelected(!selected);
        updateExample();
    }
}
