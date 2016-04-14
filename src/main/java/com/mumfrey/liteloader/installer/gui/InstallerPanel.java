package com.mumfrey.liteloader.installer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
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
	public class Metrics
	{
		private final boolean isHighResolution;
		
		public final int padding, contentPadding, choicePaddingV, choicePaddingH;
		public final int panelWidth, panelHeight, topSpacing, leftSpacing, rightSpacing, contentWidth;
		public final int browseButtonWidth, browseButtonHeight;
		public final int filePanelRightPadding, filePanelRightSpacing, filePanelWidth, filePanelHeight;
		public final int selectDirColumns;
		
		Metrics(boolean highResolution)
		{
			this.isHighResolution = highResolution;
			
			this.padding               = 0;
			this.contentPadding        = 0;
			this.choicePaddingV        = 0;
			this.choicePaddingH        = 0;
			this.panelWidth            = 850;
			this.panelHeight           = 450;
			this.topSpacing            = 120;
			this.leftSpacing           = 300;
			this.rightSpacing          = 32;
			this.contentWidth          = 450;
			this.browseButtonWidth     = 40;
			this.browseButtonHeight    = 21;
			this.filePanelRightSpacing = 20;
			this.filePanelHeight       = 96;
			this.selectDirColumns      = 30;
			this.filePanelRightPadding = this.browseButtonWidth + 10;
			this.filePanelWidth        = this.contentWidth + this.filePanelRightPadding;
		}

		public boolean isHighResolution()
		{
			return this.isHighResolution;
		}

		public Dimension getPanelSize()
		{
			return new Dimension(this.panelWidth, this.panelHeight);
		}
		
		public Dimension getFilePanelSize()
		{
			return new Dimension(this.filePanelWidth, this.filePanelHeight);
		}
		
		public Dimension getSelectFilePanelSize()
		{
			return new Dimension(this.contentWidth, this.browseButtonHeight);
		}
		
		public Dimension getBrowseButtonSize()
		{
			return new Dimension(this.browseButtonWidth, this.browseButtonHeight);
		}
		
		public Border getTargetDirBorder()
		{
			return new EmptyBorder(8, 12, 4, 0);
		}
		
		public Border getLayoutBorder()
		{
			return new EmptyBorder(this.padding, this.padding, this.padding , this.padding);
		}

		public Border getContentBorder()
		{
			return new EmptyBorder(this.contentPadding, this.contentPadding, this.contentPadding, this.contentPadding);
		}

		public Border getChoicesBorder()
		{
			return new EmptyBorder(this.choicePaddingV, this.choicePaddingH, this.choicePaddingV, this.choicePaddingH);
		}
	}
	
	public static final Color BACKGROUND_COLOUR = new Color(0xE1E7F9);
	public static final Color FOREGROUND_COLOUR = Color.WHITE;
	
	private static InstallerPanel instance;
	
	private JDialog container;
	private JTextField txtSelectedDir;
	private JLabel lblDestinationInfo, lblTargetDir;
	private JPanel pnlOuter, pnlContent, pnlSelectDir, pnlTargetDir;
	private JButton btnBrowseDir;
	private ButtonGroup grpChoiceButtons = new ButtonGroup();
	
	private final Metrics metrics;

	private BufferedImage backgroundImage;
	private Image dialogIconImage;
	
	protected File targetDir;
	protected boolean exclusivityConflict = false;
	
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
			InstallerPanel.this.updateModifiers();
		}
	}
	
	public InstallerPanel(boolean highResolution, File targetDir)
	{
		this(highResolution);
		this.targetDir = targetDir;
		this.init();
	}
	
	protected InstallerPanel(boolean highResolution)
	{
		InstallerPanel.instance = this;
		this.metrics = new Metrics(highResolution);
	}

	private void init()
	{
		InstallerAction.refreshActions(this.targetDir, InstallerAction.CLIENT);
		InstallerModifier.refreshModifiers(true, this.targetDir);
		
		this.initBackground();
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		this.pnlOuter = this.initLayout();
		this.pnlContent = this.initContentPanel();
		this.pnlOuter.add(this.pnlContent, BorderLayout.CENTER);
		this.add(this.pnlOuter);
		this.updateFilePath();
	}

	private void initBackground()
	{
		this.setBackground(InstallerPanel.BACKGROUND_COLOUR);

		try
		{
			this.backgroundImage = ImageIO.read(Installer.class.getResourceAsStream(VersionInfo.getBackgroundFileName()));
			this.setBackgroundImage(this.backgroundImage);
		}
		catch (IOException e)
		{
			throw Throwables.propagate(e);
		}
		
		try
		{
			this.dialogIconImage = ImageIO.read(Installer.class.getResourceAsStream(VersionInfo.getIconFileName()));
		}
		catch (IOException e)
		{
		}
	}

	private JPanel initLayout()
	{
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new BorderLayout());
		panel.setPreferredSize(this.metrics.getPanelSize());
		panel.setBorder(this.metrics.getLayoutBorder());
		
		panel.add(Box.createVerticalStrut(this.metrics.topSpacing), BorderLayout.NORTH);
		panel.add(Box.createHorizontalStrut(this.metrics.leftSpacing), BorderLayout.WEST);
		panel.add(Box.createHorizontalStrut(this.metrics.rightSpacing), BorderLayout.EAST);
		
		return panel;
	}

	private JPanel initContentPanel()
	{
		JPanel contentPanel = new JPanel();
		contentPanel.setOpaque(false);
		contentPanel.setBorder(this.metrics.getContentBorder());
		contentPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		contentPanel.add(this.initChoices(this.grpChoiceButtons));
		contentPanel.add(this.initDestinationPanel());
		
		return contentPanel;
	}

	private JPanel initChoices(ButtonGroup buttonGroup)
	{
		JPanel panel = new JPanel();
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.setBorder(this.metrics.getChoicesBorder());
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setOpaque(false);
		
		this.populateChoices(buttonGroup, panel, new SelectButtonAction(), new ActionChangeAction());
		
		return panel;
	}

	private void populateChoices(ButtonGroup buttonGroup, JPanel panel, Action selectAction, Action changeAction)
	{
		boolean first = true;
		for (InstallerAction installerAction : InstallerAction.values())
		{
			JPanel radioButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			radioButtonPanel.setOpaque(false);
			
			ActionRadioButton actionRadioButton = new ActionRadioButton(installerAction, first);
			actionRadioButton.setForeground(InstallerPanel.FOREGROUND_COLOUR);
			actionRadioButton.setAction(selectAction);
			actionRadioButton.addActionListener(changeAction);
			actionRadioButton.setActionCommand(installerAction.name());
			actionRadioButton.setOpaque(false);
			actionRadioButton.setText(installerAction.getButtonLabel());
			actionRadioButton.setActionCommand(installerAction.name());
			actionRadioButton.setToolTipText(installerAction.getTooltip());
			actionRadioButton.setSelected(first);
			actionRadioButton.setAlignmentX(Component.LEFT_ALIGNMENT);
			actionRadioButton.setAlignmentY(Component.CENTER_ALIGNMENT);
			Font font = this.getFont();
			actionRadioButton.setFont(font.deriveFont(font.getSize() + 6.0F));
			buttonGroup.add(actionRadioButton);
			radioButtonPanel.add(actionRadioButton);
			panel.add(radioButtonPanel);
			
			JPanel choiceOptionsPanel = installerAction.getOptionsPanel();
			if (choiceOptionsPanel != null)
			{
				panel.add(choiceOptionsPanel);
			}
			
			first = false;
		}
	}

	private JPanel initDestinationPanel()
	{
		this.lblDestinationInfo = new JLabel();
		this.lblDestinationInfo.setBorder(new EmptyBorder(4, 4, 4, 0));
		this.lblDestinationInfo.setHorizontalTextPosition(SwingConstants.LEFT);
		this.lblDestinationInfo.setVerticalTextPosition(SwingConstants.TOP);
		this.lblDestinationInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.lblDestinationInfo.setAlignmentY(Component.TOP_ALIGNMENT);
		this.lblDestinationInfo.setForeground(InstallerPanel.FOREGROUND_COLOUR);
		this.lblDestinationInfo.setVisible(false);

		this.txtSelectedDir = new JTextField();
		this.txtSelectedDir.setEditable(false);
		this.txtSelectedDir.setToolTipText("Path to minecraft");
		this.txtSelectedDir.setColumns(this.metrics.selectDirColumns);
		
		this.btnBrowseDir = new JButton();
		this.btnBrowseDir.setPreferredSize(this.metrics.getBrowseButtonSize());
		this.btnBrowseDir.setAction(new FileSelectAction());
		this.btnBrowseDir.setText("...");
		this.btnBrowseDir.setToolTipText("Select an alternative minecraft directory");
		this.btnBrowseDir.setOpaque(false);

		this.pnlSelectDir = new JPanel();
		this.pnlSelectDir.setPreferredSize(this.metrics.getSelectFilePanelSize());
		this.pnlSelectDir.setLayout(new BoxLayout(this.pnlSelectDir, BoxLayout.X_AXIS));
		this.pnlSelectDir.setOpaque(false);
		this.pnlSelectDir.add(this.txtSelectedDir);
		this.pnlSelectDir.add(Box.createHorizontalStrut(2));
		this.pnlSelectDir.add(this.btnBrowseDir);
		this.pnlSelectDir.add(Box.createHorizontalStrut(this.metrics.filePanelRightSpacing));
		this.pnlSelectDir.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.pnlSelectDir.setAlignmentY(Component.TOP_ALIGNMENT);
		
		this.lblTargetDir = new JLabel("Choose the minecraft directory for installation/extraction");
		this.lblTargetDir.setBorder(this.metrics.getTargetDirBorder());
		this.lblTargetDir.setForeground(InstallerPanel.FOREGROUND_COLOUR);
		
		this.pnlTargetDir = new JPanel();
		this.pnlTargetDir.setOpaque(false);
		this.pnlTargetDir.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.pnlTargetDir.add(this.lblDestinationInfo);
		this.pnlTargetDir.add(Box.createVerticalGlue());
		this.pnlTargetDir.add(this.pnlSelectDir);
		this.pnlTargetDir.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.pnlTargetDir.setAlignmentY(Component.TOP_ALIGNMENT);

		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new BorderLayout());
		panel.setPreferredSize(this.metrics.getFilePanelSize());
		panel.add(this.lblTargetDir, BorderLayout.NORTH);
		panel.add(this.pnlTargetDir, BorderLayout.CENTER);
		
		return panel;
	}
	
	protected void updateFilePath()
	{
		try
		{
			this.targetDir = this.targetDir.getCanonicalFile();
			this.txtSelectedDir.setText(this.targetDir.getPath());
		}
		catch (IOException e) {}
		
		InstallerAction action = InstallerAction.valueOf(this.grpChoiceButtons.getSelection().getActionCommand());
		InstallerAction.refreshActions(this.targetDir, action);

		boolean valid = action.isPathValid(this.targetDir);

		InstallerModifier.refreshModifiers(valid, this.targetDir);
		
		if (valid)
		{
			this.txtSelectedDir.setForeground(Color.BLACK);
			this.lblDestinationInfo.setVisible(false);
			this.pnlTargetDir.setBorder(null);
			if (this.container != null)
			{
				this.container.invalidate();
				this.container.pack();
			}
		}
		else
		{
			this.txtSelectedDir.setForeground(Color.RED);
			this.pnlTargetDir.setBorder(new LineBorder(Color.RED));
			this.lblDestinationInfo.setText("<html>" + action.getFileError(this.targetDir) + "</html>");
			this.lblDestinationInfo.setVisible(true);
			if (this.container != null)
			{
				this.container.invalidate();
				this.container.pack();
			}
		}

		for (Enumeration<AbstractButton> buttons = this.grpChoiceButtons.getElements(); buttons.hasMoreElements(); )
		{
			ActionRadioButton button = (ActionRadioButton)buttons.nextElement();
			button.updateRadioButton();
		}
		
		this.updateModifiers();
	}
	
	public boolean run()
	{
		ImageOptionPane optionPane = new ImageOptionPane(this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		optionPane.setBackgroundImage(this.getBackgroundImage());
		optionPane.setOpaque(false);
		optionPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		
		try
        {
            this.prettify(optionPane);
        }
        catch (Exception ex1)
        {
            ex1.printStackTrace();
        }
		
		this.container = optionPane.createDialog(null, VersionInfo.getDialogTitle());
		this.container.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.container.setModal(true);
		if (this.dialogIconImage != null)
			this.container.setIconImage(this.dialogIconImage);
		int result = -1;
		do
		{
			this.container.setVisible(true);
			result = (Integer)(optionPane.getValue() != null ? optionPane.getValue() : -1);
			this.exclusivityConflict = result == JOptionPane.OK_OPTION && exclusivityCheck();
		} while (this.exclusivityConflict);
		
		if (result == JOptionPane.OK_OPTION)
		{
			InstallerAction action = InstallerAction.valueOf(this.grpChoiceButtons.getSelection().getActionCommand());
			
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
				this.container.dispose();
				return true;
			}
		}
		this.container.dispose();
		return true;
	}

    protected void prettify(ImageOptionPane optionPane)
    {
        JPanel buttonBar = null;
        for (int id = 0; id < optionPane.getComponentCount(); id++)
        {
            Component component = optionPane.getComponent(id);
            if (component instanceof JPanel)
            {
                buttonBar = (JPanel)component;
            }
            if (component instanceof JSeparator)
            {
                component.setVisible(false);
            }
        }
        
        if (buttonBar != null)
        {
            buttonBar.setBorder(new EmptyBorder(10, 10, 10, 10));
            buttonBar.setOpaque(false);
        }
    }

    protected Component getButtonBarComponent(ImageOptionPane optionPane)
    {
        for (int id = 0; id < optionPane.getComponentCount(); id++)
        {
            System.err.println(">> " + optionPane.getComponent(id).getClass());
        }
        return optionPane.getComponent(1);
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

	public Metrics getMetrics()
	{
		return this.metrics;
	}
	
	public static InstallerPanel getInstance()
	{
		return instance;
	}
}
