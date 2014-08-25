package com.mumfrey.liteloader.installer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.google.common.base.Throwables;
import com.mumfrey.liteloader.installer.Installer;
import com.mumfrey.liteloader.installer.VersionInfo;
import com.mumfrey.liteloader.installer.actions.InstallerAction;
import com.mumfrey.liteloader.installer.modifiers.CascadeModifier;
import com.mumfrey.liteloader.installer.modifiers.InstallationModifier;
import com.mumfrey.liteloader.installer.modifiers.InstallerModifier;

@SuppressWarnings("serial")
public class InstallerPanel extends ImagePanel
{
	public static final Color BACKGROUND_COLOUR = new Color(0xE1E7F9);
	public static final Color FOREGROUND_COLOUR = Color.WHITE;
	public static final int CONTENT_WIDTH = 450;
	protected File targetDir;
	private ButtonGroup choiceButtonGroup;
	private JTextField selectedDirText;
	private JLabel infoLabel;
	private JDialog dialog;
	private JPanel fileEntryPanel;
	private Image dialogIcon;
	// private List<JCheckBox> modifierCheckBoxes = new ArrayList<JCheckBox>();
	// private Map<CascadeModifier, JComboBox> modifierControls = new
	// HashMap<CascadeModifier, JComboBox>();
	private boolean exclusivityConflict = false;
	
	protected class FileSelectAction extends AbstractAction
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			JFileChooser dirChooser = new JFileChooser();
			dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			dirChooser.setFileHidingEnabled(false);
			dirChooser.ensureFileIsVisible(InstallerPanel.this.targetDir);
			dirChooser.setSelectedFile(InstallerPanel.this.targetDir);
			int response = dirChooser.showOpenDialog(InstallerPanel.this);
			switch (response)
			{
				case JFileChooser.APPROVE_OPTION:
					InstallerPanel.this.targetDir = dirChooser.getSelectedFile();
					InstallerPanel.this.updateFilePath();
					break;
				default:
					break;
			}
		}
	}
	
	protected class SelectButtonAction extends AbstractAction
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			InstallerPanel.this.updateFilePath();
		}
		
	}
	
	protected class ActionChangeAction extends AbstractAction
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
//		InstallerAction action = InstallerAction.valueOf(choiceButtonGroup.getSelection().getActionCommand());
//		for (JCheckBox checkBox : modifierCheckBoxes)
//		{
//			InstallerModifier modifier = InstallerModifier.valueOf(checkBox.getActionCommand());
//			checkBox.setEnabled(action.allowsModifiers() && modifier.isAvailable());
//			checkBox.setText(modifier.getButtonLabel());
//		}
//		
//		for (Entry<CascadeModifier, JComboBox> comboBox : this.modifierControls.entrySet())
//		{
//			updateCombo(comboBox.getKey(), comboBox.getValue());
//		}
	}
	
	/**
	 * @param modifierAction
	 * @param comboBox
	 */
	public void updateCombo(CascadeModifier modifierAction, JComboBox comboBox)
	{
		comboBox.removeAllItems();
		
		if (modifierAction.hasMultipleVersions())
		{
			comboBox.setVisible(true);
			
			String latestVersion = modifierAction.getLatestVersion();
			if (latestVersion != null)
				comboBox.addItem(latestVersion);
			
			for (String version : modifierAction.getOtherVersions())
				comboBox.addItem(version);
		}
		else
		{
			comboBox.setVisible(false);
		}
	}
	
	protected boolean exclusivityCheck()
	{
//		Map<String, String> exclusivity = new HashMap<String, String>();
//		
//		for (JCheckBox modifierCheckBox : this.modifierCheckBoxes)
//		{
//			if (modifierCheckBox.isEnabled())
//			{
//				ActionModifier modifier = InstallerModifier.valueOf(modifierCheckBox.getActionCommand()).getModifier();
//				if (exclusivity.containsKey(modifier.getExclusivityKey()) && modifierCheckBox.isSelected())
//				{
//					String conflictMessage = "Unable to continue: " + modifier.getLabel() + " conflicts with " + exclusivity.get(modifier.getExclusivityKey()) + "\nUncheck one of these options";
//					JOptionPane.showMessageDialog(null, conflictMessage, "Conflict", JOptionPane.WARNING_MESSAGE);
//					return true;
//				}
//				else if (modifierCheckBox.isSelected())
//					exclusivity.put(modifier.getExclusivityKey(), modifier.getLabel());
//			}
//		}
		
		return false;
	}
	
	public InstallerPanel(File targetDir)
	{
		InstallerAction.refreshActions(targetDir, InstallerAction.CLIENT);
		InstallerModifier.refreshModifiers(true, targetDir);
		
		this.setBackground(BACKGROUND_COLOUR);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		BufferedImage bg;
		try
		{
			bg = ImageIO.read(Installer.class.getResourceAsStream(VersionInfo.getBackgroundFileName()));
			this.setBackgroundImage(bg);
		}
		catch (IOException e)
		{
			throw Throwables.propagate(e);
		}
		
		try
		{
			this.dialogIcon = ImageIO.read(Installer.class.getResourceAsStream(VersionInfo.getIconFileName()));
		}
		catch (IOException e)
		{
		}
		
		BorderLayout outerLayout = new BorderLayout();
		JPanel outerPanel = new JPanel();
		outerPanel.setOpaque(false);
		outerPanel.setLayout(outerLayout);
		outerPanel.setPreferredSize(new Dimension(850, 450));
		outerPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		
		outerPanel.add(Box.createVerticalStrut(140), BorderLayout.NORTH);
		outerPanel.add(Box.createHorizontalStrut(300), BorderLayout.WEST);
		outerPanel.add(Box.createHorizontalStrut(32), BorderLayout.EAST);
		
		JPanel contentPanel = new JPanel();
		contentPanel.setOpaque(false);
		contentPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		contentPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		this.choiceButtonGroup = new ButtonGroup();
		
		JPanel choicePanel = new JPanel();
		choicePanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		choicePanel.setLayout(new BoxLayout(choicePanel, BoxLayout.Y_AXIS));
		choicePanel.setOpaque(false);
//		choicePanel.setOpaque(true); choicePanel.setBackground(Color.BLUE);
		boolean first = true;
		
		SelectButtonAction selectButtonAction = new SelectButtonAction();
		for (InstallerAction action : InstallerAction.values())
		{
			JPanel radioButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			radioButtonPanel.setOpaque(false);
			
			JRadioButton actionRadioButton = new JRadioButton();
			actionRadioButton.setOpaque(false);
			actionRadioButton.setForeground(FOREGROUND_COLOUR);
			actionRadioButton.setAction(selectButtonAction);
			actionRadioButton.setText(action.getButtonLabel());
			actionRadioButton.setActionCommand(action.name());
			actionRadioButton.setToolTipText(action.getTooltip());
			actionRadioButton.setSelected(first);
			actionRadioButton.setAlignmentX(LEFT_ALIGNMENT);
			actionRadioButton.setAlignmentY(CENTER_ALIGNMENT);
			actionRadioButton.addActionListener(new ActionChangeAction());
			Font font = actionRadioButton.getFont();
			actionRadioButton.setFont(font.deriveFont(font.getSize() + 6.0F));
			this.choiceButtonGroup.add(actionRadioButton);
			radioButtonPanel.add(actionRadioButton);
			choicePanel.add(radioButtonPanel);
			
			JPanel choiceOptionsPanel = action.getOptionsPanel();
			if (choiceOptionsPanel != null)
			{
				choicePanel.add(choiceOptionsPanel);
			}
			
			first = false;
		}
		
//		choicePanel.add(Box.createVerticalStrut(16));
		
//		JLabel chainLabel = new JLabel("Chaining options: (additional tweakers to load)");
//		chainLabel.setPreferredSize(new Dimension(30, 30));
//		chainLabel.setForeground(FOREGROUND_COLOUR);
//		choicePanel.add(chainLabel);
		
//		JPanel modifiersPanel = new JPanel();
//		modifiersPanel.setOpaque(false);
//		modifiersPanel.setAlignmentX(LEFT_ALIGNMENT);
//		modifiersPanel.setLayout(new BoxLayout(modifiersPanel, BoxLayout.Y_AXIS));
		
//		for (InstallerModifier modifier : InstallerModifier.values())
//		{
//			JPanel modifierPanel = new JPanel();
//			FlowLayout modifierPanelLayout = new FlowLayout(FlowLayout.LEFT);
//			modifierPanelLayout.setVgap(0);
//			modifierPanelLayout.setHgap(0);
//			modifierPanel.setAlignmentX(LEFT_ALIGNMENT);
//			modifierPanel.setLayout(modifierPanelLayout);
//			modifierPanel.setOpaque(false);
//			
//			JCheckBox modifierCheckBox = new JCheckBox();
//			modifierCheckBox.setOpaque(false);
//			modifierCheckBox.setForeground(FOREGROUND_COLOUR);
//			modifierCheckBoxes.add(modifierCheckBox);
//			modifierCheckBox.setAction(sba);
//			modifierCheckBox.setText(modifier.getButtonLabel());
//			modifierCheckBox.setActionCommand(modifier.name());
//			modifierCheckBox.setToolTipText(modifier.getTooltip());
//			modifierCheckBox.setSelected(false);
//			modifierCheckBox.setAlignmentX(LEFT_ALIGNMENT);
//			modifierCheckBox.setAlignmentY(CENTER_ALIGNMENT);
//			modifierPanel.add(modifierCheckBox);
//			
//			if (modifier.getModifier() instanceof CascadeModifier)
//			{
//				JComboBox comboBox = new JComboBox();
//				comboBox.setOpaque(false);
//				CascadeModifier modifierAction =
//						(CascadeModifier)modifier.getModifier();
//				modifierControls.put(modifierAction, comboBox);
//				modifierPanel.add(comboBox);
//				updateCombo(modifierAction, comboBox);
//			}
//			
//			modifiersPanel.add(modifierPanel);
//		}
		
//		choicePanel.add(modifiersPanel);
		
//		choicePanel.add(Box.createVerticalStrut(20));
		choicePanel.setAlignmentX(LEFT_ALIGNMENT);
		
		contentPanel.add(choicePanel);
		JPanel selectFilePanel = new JPanel();
		selectFilePanel.setPreferredSize(new Dimension(InstallerPanel.CONTENT_WIDTH, 21));
		selectFilePanel.setLayout(new BoxLayout(selectFilePanel, BoxLayout.X_AXIS));
		selectFilePanel.setOpaque(false);
		
		this.targetDir = targetDir;
		this.selectedDirText = new JTextField();
		this.selectedDirText.setEditable(false);
		this.selectedDirText.setToolTipText("Path to minecraft");
		this.selectedDirText.setColumns(30);
		JButton btnBrowse = new JButton();
		btnBrowse.setPreferredSize(new Dimension(40, 21));
		btnBrowse.setAction(new FileSelectAction());
		btnBrowse.setText("...");
		btnBrowse.setToolTipText("Select an alternative minecraft directory");
		btnBrowse.setOpaque(false);

		selectFilePanel.add(this.selectedDirText);
		selectFilePanel.add(Box.createHorizontalStrut(2));
		selectFilePanel.add(btnBrowse);
		selectFilePanel.add(Box.createHorizontalStrut(20));
		
		selectFilePanel.setAlignmentX(LEFT_ALIGNMENT);
		selectFilePanel.setAlignmentY(TOP_ALIGNMENT);
		this.infoLabel = new JLabel();
		this.infoLabel.setBorder(new EmptyBorder(4, 4, 4, 0));
		this.infoLabel.setHorizontalTextPosition(SwingConstants.LEFT);
		this.infoLabel.setVerticalTextPosition(SwingConstants.TOP);
		this.infoLabel.setAlignmentX(LEFT_ALIGNMENT);
		this.infoLabel.setAlignmentY(TOP_ALIGNMENT);
		this.infoLabel.setForeground(FOREGROUND_COLOUR);
		this.infoLabel.setVisible(false);
		
		JPanel fileEntryPanelContainer = new JPanel();
		fileEntryPanelContainer.setOpaque(false);
		fileEntryPanelContainer.setLayout(new BorderLayout());
		fileEntryPanelContainer.setPreferredSize(new Dimension(InstallerPanel.CONTENT_WIDTH + 50, 96));
		
		JLabel lblTargetDir = new JLabel("Choose the minecraft directory for installation/extraction");
		lblTargetDir.setBorder(new EmptyBorder(12, 12, 4, 0));
		lblTargetDir.setForeground(FOREGROUND_COLOUR);
		
		this.fileEntryPanel = new JPanel();
		this.fileEntryPanel.setOpaque(false);
		this.fileEntryPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.fileEntryPanel.add(this.infoLabel);
		this.fileEntryPanel.add(Box.createVerticalGlue());
		this.fileEntryPanel.add(selectFilePanel);
		this.fileEntryPanel.setAlignmentX(LEFT_ALIGNMENT);
		this.fileEntryPanel.setAlignmentY(TOP_ALIGNMENT);
		fileEntryPanelContainer.add(lblTargetDir, BorderLayout.NORTH);
		fileEntryPanelContainer.add(this.fileEntryPanel, BorderLayout.CENTER);
		
		contentPanel.add(fileEntryPanelContainer);
		outerPanel.add(contentPanel, BorderLayout.CENTER);
		this.add(outerPanel);
		updateFilePath();
	}
	
	protected void updateFilePath()
	{
		try
		{
			this.targetDir = this.targetDir.getCanonicalFile();
			this.selectedDirText.setText(this.targetDir.getPath());
		}
		catch (IOException e)
		{
			
		}
		
		InstallerAction action = InstallerAction.valueOf(this.choiceButtonGroup.getSelection().getActionCommand());
		InstallerAction.refreshActions(this.targetDir, action);
		
		boolean valid = action.isPathValid(this.targetDir);
		
		InstallerModifier.refreshModifiers(valid, this.targetDir);
		
		if (valid)
		{
			this.selectedDirText.setForeground(Color.BLACK);
			this.infoLabel.setVisible(false);
			this.fileEntryPanel.setBorder(null);
			if (this.dialog != null)
			{
				this.dialog.invalidate();
				this.dialog.pack();
			}
		}
		else
		{
			this.selectedDirText.setForeground(Color.RED);
			this.fileEntryPanel.setBorder(new LineBorder(Color.RED));
			this.infoLabel.setText("<html>" + action.getFileError(this.targetDir) + "</html>");
			this.infoLabel.setVisible(true);
			if (this.dialog != null)
			{
				this.dialog.invalidate();
				this.dialog.pack();
			}
		}
		
		this.updateModifiers();
	}
	
	public boolean run()
	{
		ImageOptionPane optionPane = new ImageOptionPane(this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		optionPane.setBackgroundImage(this.getBackgroundImage());
		optionPane.setOpaque(false);
		optionPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		
		((JPanel)optionPane.getComponent(1)).setBorder(new EmptyBorder(10, 0, 10, 0));
		((JComponent)optionPane.getComponent(1)).setOpaque(false);
		
		this.dialog = optionPane.createDialog(null, VersionInfo.getDialogTitle());
		this.dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.dialog.setModal(true);
		if (this.dialogIcon != null)
			this.dialog.setIconImage(this.dialogIcon);
		int result = -1;
		do
		{
			this.dialog.setVisible(true);
			result = (Integer)(optionPane.getValue() != null ? optionPane.getValue() : -1);
			this.exclusivityConflict = result == JOptionPane.OK_OPTION && exclusivityCheck();
		} while (this.exclusivityConflict);
		
		if (result == JOptionPane.OK_OPTION)
		{
			InstallerAction action = InstallerAction.valueOf(this.choiceButtonGroup.getSelection().getActionCommand());
			
			ArrayList<InstallationModifier> modifiers = new ArrayList<InstallationModifier>();
//			for (JCheckBox modifierCheckBox : modifierCheckBoxes)
//			{
//				if (modifierCheckBox.isEnabled() && modifierCheckBox.isSelected())
//				{
//					ActionModifier modifier = InstallerModifier.valueOf(modifierCheckBox.getActionCommand()).getModifier();
//					modifier.prepare(modifierControls.get(modifier));
//					modifiers.add(modifier);
//				}
//			}
			
			try
			{
				if (action.run(this.targetDir, modifiers))
				{
					JOptionPane.showMessageDialog(null, action.getSuccessMessage(), "Complete", JOptionPane.INFORMATION_MESSAGE);
				}
			}
			catch (CancelledException ex)
			{
				return false;
			}
			catch (Throwable th)
			{
				th.printStackTrace();
				JOptionPane.showMessageDialog(null, "An error occurred during the operation: " + th.getClass().getSimpleName() + " (" + th.getMessage() + ")", "Failed", JOptionPane.ERROR_MESSAGE);
				this.dialog.dispose();
				return true;
			}
		}
		this.dialog.dispose();
		return true;
	}
}
