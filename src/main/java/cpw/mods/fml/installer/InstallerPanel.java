package cpw.mods.fml.installer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;

import com.google.common.base.Throwables;

public class InstallerPanel extends JPanel {
    private File targetDir;
    private ButtonGroup choiceButtonGroup;
    private JTextField selectedDirText;
    private JLabel infoLabel;
    private JDialog dialog;
    private JPanel fileEntryPanel;
    private Image dialogIcon;
    private List<JCheckBox> modifierCheckBoxes = new ArrayList<JCheckBox>();

    private class FileSelectAction extends AbstractAction
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            JFileChooser dirChooser = new JFileChooser();
            dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            dirChooser.setFileHidingEnabled(false);
            dirChooser.ensureFileIsVisible(targetDir);
            dirChooser.setSelectedFile(targetDir);
            int response = dirChooser.showOpenDialog(InstallerPanel.this);
            switch (response)
            {
            case JFileChooser.APPROVE_OPTION:
                targetDir = dirChooser.getSelectedFile();
                updateFilePath();
                break;
            default:
                break;
            }
        }
    }

    private class SelectButtonAction extends AbstractAction
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            updateFilePath();
        }

    }
    
    private class ActionChangeAction extends AbstractAction
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            updateModifiers();
        }
    }

    /**
     * @param e
     */
    protected void updateModifiers()
    {
        InstallerAction action = InstallerAction.valueOf(choiceButtonGroup.getSelection().getActionCommand());
        for (JCheckBox checkBox : modifierCheckBoxes)
        {
            InstallerModifier modifier = InstallerModifier.valueOf(checkBox.getActionCommand());
            checkBox.setEnabled(action.allowsModifiers() && modifier.isAvailable());
            checkBox.setText(modifier.getButtonLabel());
        }
    }
    
    public InstallerPanel(File targetDir)
    {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        BufferedImage image;
        try
        {
            image = ImageIO.read(SimpleInstaller.class.getResourceAsStream(VersionInfo.getLogoFileName()));
        }
        catch (IOException e)
        {
            throw Throwables.propagate(e);
        }
        
        try
        {
            this.dialogIcon = ImageIO.read(SimpleInstaller.class.getResourceAsStream(VersionInfo.getIconFileName()));
        }
        catch (IOException e)
        {
            // don't care if no icon
        }
        
        JPanel logoSplash = new JPanel();
        logoSplash.setLayout(new BoxLayout(logoSplash, BoxLayout.Y_AXIS));
        ImageIcon icon = new ImageIcon(image);
        JLabel logoLabel = new JLabel(icon);
        logoLabel.setAlignmentX(CENTER_ALIGNMENT);
        logoLabel.setAlignmentY(CENTER_ALIGNMENT);
        logoLabel.setSize(image.getWidth(), image.getHeight());
        logoSplash.add(logoLabel);
        JLabel tag = new JLabel(VersionInfo.getWelcomeMessage());
        tag.setAlignmentX(CENTER_ALIGNMENT);
        tag.setAlignmentY(CENTER_ALIGNMENT);
        logoSplash.add(tag);
        tag = new JLabel(VersionInfo.getVersion());
        tag.setAlignmentX(CENTER_ALIGNMENT);
        tag.setAlignmentY(CENTER_ALIGNMENT);
        logoSplash.add(tag);

        logoSplash.setAlignmentX(CENTER_ALIGNMENT);
        logoSplash.setAlignmentY(TOP_ALIGNMENT);
        this.add(logoSplash);
        choiceButtonGroup = new ButtonGroup();

        JPanel choicePanel = new JPanel();
        choicePanel.setLayout(new BoxLayout(choicePanel, BoxLayout.Y_AXIS));
        boolean first = true;
        SelectButtonAction sba = new SelectButtonAction();
        for (InstallerAction action : InstallerAction.values())
        {
            JRadioButton radioButton = new JRadioButton();
            radioButton.setAction(sba);
            radioButton.setText(action.getButtonLabel());
            radioButton.setActionCommand(action.name());
            radioButton.setToolTipText(action.getTooltip());
            radioButton.setSelected(first);
            radioButton.setAlignmentX(LEFT_ALIGNMENT);
            radioButton.setAlignmentY(CENTER_ALIGNMENT);
            radioButton.addActionListener(new ActionChangeAction());
            choiceButtonGroup.add(radioButton);
            choicePanel.add(radioButton);
            first = false;
        }
        
        JLabel chainLabel = new JLabel("Chaining options: (cascadedTweaks)");
        choicePanel.add(chainLabel);

        for (InstallerModifier modifier : InstallerModifier.values())
        {
            JCheckBox modifierCheckBox = new JCheckBox();
            modifierCheckBoxes.add(modifierCheckBox);
            modifierCheckBox.setAction(sba);
            modifierCheckBox.setText(modifier.getButtonLabel());
            modifierCheckBox.setActionCommand(modifier.name());
            modifierCheckBox.setToolTipText(modifier.getTooltip());
            modifierCheckBox.setSelected(false);
            modifierCheckBox.setAlignmentX(LEFT_ALIGNMENT);
            modifierCheckBox.setAlignmentY(CENTER_ALIGNMENT);
            choicePanel.add(modifierCheckBox);
        }
        
        choicePanel.setAlignmentX(RIGHT_ALIGNMENT);
        choicePanel.setAlignmentY(CENTER_ALIGNMENT);
        add(choicePanel);
        JPanel entryPanel = new JPanel();
        entryPanel.setLayout(new BoxLayout(entryPanel,BoxLayout.X_AXIS));

        this.targetDir = targetDir;
        selectedDirText = new JTextField();
        selectedDirText.setEditable(false);
        selectedDirText.setToolTipText("Path to minecraft");
        selectedDirText.setColumns(30);
//        homeDir.setMaximumSize(homeDir.getPreferredSize());
        entryPanel.add(selectedDirText);
        JButton dirSelect = new JButton();
        dirSelect.setAction(new FileSelectAction());
        dirSelect.setText("...");
        dirSelect.setToolTipText("Select an alternative minecraft directory");
        entryPanel.add(dirSelect);

        entryPanel.setAlignmentX(LEFT_ALIGNMENT);
        entryPanel.setAlignmentY(TOP_ALIGNMENT);
        infoLabel = new JLabel();
        infoLabel.setHorizontalTextPosition(JLabel.LEFT);
        infoLabel.setVerticalTextPosition(JLabel.TOP);
        infoLabel.setAlignmentX(LEFT_ALIGNMENT);
        infoLabel.setAlignmentY(TOP_ALIGNMENT);
        infoLabel.setForeground(Color.RED);
        infoLabel.setVisible(false);

        fileEntryPanel = new JPanel();
        fileEntryPanel.setLayout(new BoxLayout(fileEntryPanel,BoxLayout.Y_AXIS));
        fileEntryPanel.add(infoLabel);
        fileEntryPanel.add(Box.createVerticalGlue());
        fileEntryPanel.add(entryPanel);
        fileEntryPanel.setAlignmentX(CENTER_ALIGNMENT);
        fileEntryPanel.setAlignmentY(TOP_ALIGNMENT);
        this.add(fileEntryPanel);
        updateFilePath();
    }

    private void updateFilePath()
    {
        try
        {
            targetDir = targetDir.getCanonicalFile();
            selectedDirText.setText(targetDir.getPath());
        }
        catch (IOException e)
        {

        }

        InstallerAction action = InstallerAction.valueOf(choiceButtonGroup.getSelection().getActionCommand());
        boolean valid = action.isPathValid(targetDir);
        
        for (InstallerModifier installerModifier : InstallerModifier.values())
            installerModifier.getModifier().refresh(valid, targetDir);

        if (valid)
        {
            selectedDirText.setForeground(Color.BLACK);
            infoLabel.setVisible(false);
            fileEntryPanel.setBorder(null);
            if (dialog!=null)
            {
                dialog.invalidate();
                dialog.pack();
            }
        }
        else
        {
            selectedDirText.setForeground(Color.RED);
            fileEntryPanel.setBorder(new LineBorder(Color.RED));
            infoLabel.setText("<html>"+action.getFileError(targetDir)+"</html>");
            infoLabel.setVisible(true);
            if (dialog!=null)
            {
                dialog.invalidate();
                dialog.pack();
            }
        }
        
        updateModifiers();
    }

    public void run()
    {
        JOptionPane optionPane = new JOptionPane(this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);

        dialog = optionPane.createDialog(null, VersionInfo.getDialogTitle());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setModal(true);
        if (this.dialogIcon != null) dialog.setIconImage(this.dialogIcon);
        dialog.setVisible(true);
        int result = (Integer) (optionPane.getValue() != null ? optionPane.getValue() : -1);
        if (result == JOptionPane.OK_OPTION)
        {
            InstallerAction action = InstallerAction.valueOf(choiceButtonGroup.getSelection().getActionCommand());
            ArrayList<ActionModifier> modifiers = new ArrayList<ActionModifier>();
            for (JCheckBox modifierCheckBox : modifierCheckBoxes)
            {            
                if (modifierCheckBox.isEnabled() && modifierCheckBox.isSelected())
                    modifiers.add(InstallerModifier.valueOf(modifierCheckBox.getActionCommand()).getModifier());
            }
            if (action.run(targetDir, modifiers))
            {
                JOptionPane.showMessageDialog(null, action.getSuccessMessage(), "Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        dialog.dispose();
    }
}
