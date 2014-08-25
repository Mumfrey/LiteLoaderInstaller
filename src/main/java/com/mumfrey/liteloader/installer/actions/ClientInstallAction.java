package com.mumfrey.liteloader.installer.actions;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import argo.format.PrettyJsonFormatter;
import argo.jdom.JdomParser;
import argo.jdom.JsonField;
import argo.jdom.JsonNode;
import argo.jdom.JsonNodeFactories;
import argo.jdom.JsonRootNode;
import argo.jdom.JsonStringNode;
import argo.saj.InvalidSyntaxException;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.mumfrey.liteloader.installer.OperationCancelledException;
import com.mumfrey.liteloader.installer.VersionInfo;
import com.mumfrey.liteloader.installer.gui.CancelledException;
import com.mumfrey.liteloader.installer.gui.IInstallerMonitor;
import com.mumfrey.liteloader.installer.gui.InstallerPanel;
import com.mumfrey.liteloader.installer.modifiers.InstallationModifier;
import com.mumfrey.liteloader.installer.targets.TargetVersion;

public class ClientInstallAction extends ClientAction
{
	protected final JComboBox cmbVersion;
	
	protected final JCheckBox chkAllVersions;
	
	protected final JTextField txtProfile;
	
	protected boolean shownWarning = false;

	protected String lastText = "";
	
	protected class VersionCheckBoxChangeListener extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			ClientInstallAction.this.onToggleAllVersions();
		}
	}
	
	protected class TargetVersionChangeListener extends AbstractAction
	{
		private static final long serialVersionUID = 1L;
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			ClientInstallAction.this.onSelectedVersionChanged();
		}
	}
	
	public ClientInstallAction()
	{
		this.optionsPanel = new JPanel();
		this.optionsPanel.setLayout(new BoxLayout(this.optionsPanel, BoxLayout.Y_AXIS));
//		this.optionPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.optionsPanel.setOpaque(false);
		this.optionsPanel.setPreferredSize(new Dimension(InstallerPanel.CONTENT_WIDTH, 96));
		
		JPanel targetVersionPanel = new JPanel(new BorderLayout());
		targetVersionPanel.setOpaque(false);
		
		JLabel lblVersion = new JLabel("Extend from:");
		lblVersion.setPreferredSize(new Dimension(LEFT_MARGIN,20));
		lblVersion.setForeground(InstallerPanel.FOREGROUND_COLOUR);
		lblVersion.setBorder(new EmptyBorder(0, 32, 0, 10));
		lblVersion.setHorizontalTextPosition(SwingConstants.RIGHT);

		this.cmbVersion = new JComboBox();
		this.cmbVersion.addItem(new TargetVersion(VersionInfo.getMinecraftVersion()));
		this.cmbVersion.setBorder(new EmptyBorder(0, 0, 0, 0));
		this.cmbVersion.setMaximumRowCount(20);
		this.cmbVersion.addActionListener(new TargetVersionChangeListener());
		
		targetVersionPanel.add(lblVersion, BorderLayout.WEST);
		targetVersionPanel.add(this.cmbVersion, BorderLayout.CENTER);
		targetVersionPanel.add(Box.createHorizontalStrut(RIGHT_MARGIN), BorderLayout.EAST);
		
		this.optionsPanel.add(Box.createVerticalStrut(8));
		this.optionsPanel.add(targetVersionPanel);
		
		JPanel checkBoxPanel = new JPanel(new BorderLayout());
		checkBoxPanel.setOpaque(false);
		
		this.chkAllVersions = new JCheckBox("<html>Show incompatible versions (<font color=\"#FF8080\"><b>dangerous</b></font>)</html>");
		this.chkAllVersions.setForeground(InstallerPanel.FOREGROUND_COLOUR);
		this.chkAllVersions.setOpaque(false);
		this.chkAllVersions.addActionListener(new VersionCheckBoxChangeListener());
		checkBoxPanel.add(Box.createHorizontalStrut(130), BorderLayout.WEST);
		checkBoxPanel.add(this.chkAllVersions, BorderLayout.CENTER);
		
		this.optionsPanel.add(checkBoxPanel);
		this.optionsPanel.add(Box.createVerticalStrut(8));
		
		JPanel targetProfilePanel = new JPanel(new BorderLayout());
		targetProfilePanel.setOpaque(false);
		
		JLabel lblProfile = new JLabel("New Profile:");
		lblProfile.setPreferredSize(new Dimension(LEFT_MARGIN,18));
		lblProfile.setForeground(InstallerPanel.FOREGROUND_COLOUR);
		lblProfile.setBorder(new EmptyBorder(0, 32, 0, 10));
		lblProfile.setHorizontalTextPosition(SwingConstants.RIGHT);
		
		this.txtProfile = new JTextField();
		this.txtProfile.setBorder(new EmptyBorder(0, 4, 0, 4));
		
		targetProfilePanel.add(lblProfile, BorderLayout.WEST);
		targetProfilePanel.add(this.txtProfile, BorderLayout.CENTER);
		targetProfilePanel.add(Box.createHorizontalStrut(RIGHT_MARGIN), BorderLayout.EAST);
		
		this.optionsPanel.add(targetProfilePanel);
		this.optionsPanel.add(Box.createVerticalStrut(8));
		
		this.setProfileName(VersionInfo.getProfileName());
	}
	
	@Override
	public String getLabelSuffix()
	{
		return "";
	}
	
	private final void setProfileName(String newText)
	{
		String existingText = this.txtProfile.getText();
		if (this.lastText.equals(existingText))
		{
			this.lastText = newText;
			this.txtProfile.setText(newText);
		}
	}

	protected final void onToggleAllVersions()
	{
		if (this.chkAllVersions.isSelected() && !this.shownWarning)
		{
			this.shownWarning = true;
			if (JOptionPane.showConfirmDialog(null, "<html><b><font color=\"red\">Warning!</font></b><br><br>Showing unsupported versions allows you to extend <b>any</b> installed version, this is likely<br>to end badly unless you know what you're doing! <b>Use this option at your own risk!</b><br><br>Are you absolutely sure you want to show all versions?<br></html>", "Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE) == JOptionPane.CANCEL_OPTION)
			{
				this.shownWarning = false;
				this.chkAllVersions.setSelected(false);
			}
			
		}
		
		this.refresh(null);
	}

	public final void onSelectedVersionChanged()
	{
		TargetVersion targetVersion = (TargetVersion)this.cmbVersion.getSelectedItem();
		if (targetVersion != null)
		{
			this.setProfileName(targetVersion.getSuggestedProfileName());
		}
		else
		{
			this.setProfileName(VersionInfo.getProfileName());
		}
	}
	
	@Override
	public boolean isEnabled()
	{
		return true;
	}
	
	@Override
	public final void setSelected(boolean selected)
	{
		this.txtProfile.setEnabled(selected);
		this.chkAllVersions.setEnabled(selected);
		this.cmbVersion.setEnabled(selected);
	}
	
	@Override
	public final void refresh(File targetDir)
	{
		super.refresh(targetDir);
		
		this.cmbVersion.removeAllItems();
		
		for (TargetVersion version : this.versionList.getVersions(this.chkAllVersions.isSelected()))
		{
			this.cmbVersion.addItem(version);
		}
	}
	
	@Override
	public final boolean run(File target, List<InstallationModifier> modifiers, IInstallerMonitor monitor)
	{
		try
		{
			TargetVersion targetVersion = this.getTargetVersion();
			modifiers.add(targetVersion);
			
			if (!this.validateTarget(target)) return false;
			
			File launcherProfiles = new File(target, VersionInfo.getLauncherProfilesJson());
			if (!this.validateLauncherProfiles(launcherProfiles)) return false;
			if (monitor.isCancelled()) throw new OperationCancelledException();

			File versionTarget = this.prepareVersionDir(target);
			if (versionTarget == null) return false;
			if (monitor.isCancelled()) throw new OperationCancelledException();

			if (!this.extractLibraries(target, monitor)) return false;
			if (monitor.isCancelled()) throw new OperationCancelledException();
			
			if (!this.writeVersionFile(modifiers, versionTarget)) return false;
			
			JsonRootNode jsonProfileData = this.readLauncherProfiles(launcherProfiles);
			if (jsonProfileData == null) return false;
			
			HashMap<JsonStringNode, JsonNode> modifiedData = this.modifyProfileData(target, jsonProfileData, modifiers);
			jsonProfileData = JsonNodeFactories.object(modifiedData);
			
			if (!writeLauncherProfiles(jsonProfileData, launcherProfiles)) return false;
		}
		catch (CancelledException ex)
		{
			if (this.returnFalseOnBadState()) return false;
			throw ex;
		}
		
		return true;
	}

	public final String getProfileName()
	{
		String text = this.txtProfile.getText();
		return text.length() > 0 ? text : VersionInfo.getProfileName();
	}
	
	protected final TargetVersion getTargetVersion()
	{
		TargetVersion targetVersion = (TargetVersion)this.cmbVersion.getSelectedItem();
		
		if (!targetVersion.isValid())
		{
			this.handleInvalidTargetVersion(targetVersion);
		}
		return targetVersion;
	}

	protected final String getVersion()
	{
		TargetVersion targetVersion = (TargetVersion)this.cmbVersion.getSelectedItem();
		return VersionInfo.getVersionTarget(targetVersion);
	}

	private File prepareVersionDir(File target) throws HeadlessException
	{
		File versionRootDir = new File(target, "versions");
		File versionTarget = new File(versionRootDir, this.getVersion());
		if (!versionTarget.mkdirs() && !versionTarget.isDirectory())
		{
			if (!versionTarget.delete())
			{
				if (!this.setLastError("noTarget", versionTarget.getAbsolutePath()))
				{
					this.showMessageDialog(null, "There was a problem with the launcher version data. You will need to clear " + versionTarget.getAbsolutePath() + " manually", "Error", JOptionPane.ERROR_MESSAGE);
				}
				
				return null;
			}
			
			versionTarget.mkdirs();
		}
		return versionTarget;
	}

	private boolean writeVersionFile(List<InstallationModifier> modifiers, File versionTarget) throws HeadlessException
	{
		File versionJsonFile = new File(versionTarget, this.getVersion() + ".json");
		JsonRootNode versionJson = JsonNodeFactories.object(VersionInfo.getVersionInfo().getFields());
		
		try
		{
			for (InstallationModifier modifier : modifiers)
			{
				versionJson = modifier.modifyVersion(versionJson);
			}
			
			BufferedWriter newWriter = Files.newWriter(versionJsonFile, Charsets.UTF_8);
			PrettyJsonFormatter.fieldOrderPreservingPrettyJsonFormatter().format(versionJson, newWriter);
			newWriter.close();
		}
		catch (Exception e)
		{
			if (!this.setLastError("noCreateVersion"))
			{
				this.showMessageDialog(null, "There was a problem writing the launcher version data,  is it write protected?", "Error", JOptionPane.ERROR_MESSAGE);
			}
			return false;
		}
		
		return true;
	}

	private JsonRootNode readLauncherProfiles(File launcherProfiles) throws HeadlessException, RuntimeException
	{
		JdomParser parser = new JdomParser();
		JsonRootNode jsonProfileData;
		
		try
		{
			jsonProfileData = parser.parse(Files.newReader(launcherProfiles, Charsets.UTF_8));
		}
		catch (InvalidSyntaxException e)
		{
			if (!this.setLastError("profileJsonBad"))
			{
				this.showMessageDialog(null, "The launcher profile file is corrupted. Re-run the minecraft launcher to fix it!", "Error", JOptionPane.ERROR_MESSAGE);
			}
			return null;
		}
		catch (Exception e)
		{
			throw Throwables.propagate(e);
		}
		return jsonProfileData;
	}

	private HashMap<JsonStringNode, JsonNode> modifyProfileData(File target, JsonRootNode jsonProfileData, List<InstallationModifier> modifiers)
	{
		Set<String> jvmArgs = new LinkedHashSet<String>();
		jvmArgs.add("-Xmx1G");
		List<InstallationModifier> allModifiers = Collections.unmodifiableList(modifiers);
		
		for (InstallationModifier modifier : modifiers)
		{
			modifier.modifyJvmArgs(allModifiers, jvmArgs);
		}
		
		List<JsonField> fields = new ArrayList<JsonField>();
		fields.add(JsonNodeFactories.field("name", JsonNodeFactories.string(this.getProfileName())));
		fields.add(JsonNodeFactories.field("lastVersionId", JsonNodeFactories.string(this.getVersion())));
		fields.add(JsonNodeFactories.field("useHopperCrashService", JsonNodeFactories.string("false")));
		fields.add(JsonNodeFactories.field("javaArgs", JsonNodeFactories.string(Joiner.on(" ").join(jvmArgs))));
		this.modifyFields(target, fields);
		
		for (InstallationModifier modifier : modifiers)
			modifier.modifyFields(fields);
		
		HashMap<JsonStringNode, JsonNode> profileCopy = Maps.newHashMap(jsonProfileData.getNode("profiles").getFields());
		HashMap<JsonStringNode, JsonNode> modifiedData = Maps.newHashMap(jsonProfileData.getFields());
		
		for (Entry<JsonStringNode, JsonNode> profile : profileCopy.entrySet())
		{
			if (profile.getKey().getText().equals(VersionInfo.getProfileName()))
			{
				for (JsonField field : profile.getValue().getFieldList())
				{
					if (!field.getName().getText().matches("^(name|lastVersionId|useHopperCrashService|gameDir|javaArgs)$"))
					{
						fields.add(field);
					}
				}
			}
		}
		
		profileCopy.put(JsonNodeFactories.string(this.getProfileName()), JsonNodeFactories.object(fields));
		JsonRootNode profileJsonCopy = JsonNodeFactories.object(profileCopy);
		modifiedData.put(JsonNodeFactories.string("profiles"), profileJsonCopy);
		
		return modifiedData;
	}

	private boolean writeLauncherProfiles(JsonRootNode jsonProfileData, File launcherProfiles) throws HeadlessException
	{
		try
		{
			BufferedWriter newWriter = Files.newWriter(launcherProfiles, Charsets.UTF_8);
			PrettyJsonFormatter.fieldOrderPreservingPrettyJsonFormatter().format(jsonProfileData, newWriter);
			newWriter.close();
			return true;
		}
		catch (Exception e)
		{
			if (!this.setLastError("launchProfileBad"))
			{
				this.showMessageDialog(null, "There was a problem writing the launch profile,  is it write protected?", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		return false;
	}
	
	@Override
	public String getSuccessMessage()
	{
		return String.format("<html>Successfully created new profile \"<font color=\"blue\"><b>%s</b></font>\" for version <font color=\"blue\"><b>%s</b></font> into launcher.<br>Select the new profile in the launcher in order to launch with this version.", this.getProfileName(), VersionInfo.getVersion());
	}

	/**
	 * @param targetVersion
	 * @throws HeadlessException
	 * @throws CancelledException
	 */
	protected void handleInvalidTargetVersion(TargetVersion targetVersion) throws HeadlessException, CancelledException
	{
		if (JOptionPane.showConfirmDialog(null, "<html><b><font color=\"red\">Warning!</font></b><br>You are about to create a version extending the <b>unsupported</b> version <b>" + targetVersion.getName() + "</b>.<br>Are you sure you want to continue?<br></html>", "Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE) == JOptionPane.CANCEL_OPTION)
		{
			throw new CancelledException();
		}
	}
	
	protected void modifyFields(File target, List<JsonField> fields)
	{
	}
}
