package com.mumfrey.liteloader.installer.actions;

import java.io.File;
import java.util.List;

import javax.swing.JPanel;

import com.google.common.base.Throwables;
import com.mumfrey.liteloader.installer.gui.IInstallerMonitor;
import com.mumfrey.liteloader.installer.modifiers.InstallationModifier;

public enum InstallerAction implements IInstallerMonitor
{
	CLIENT("Install LiteLoader (recommended)", "Install a new profile to the Mojang client launcher", ClientInstallAction.class, true),
	EXTRACT("Extract LiteLoader jar", "Extract the contained jar file", ExtractAction.class, false);
	
	private String label;
	private String tooltip;
	private ActionType action;
	private boolean allowModifiers;
	
	private InstallerAction(String label, String tooltip, Class<? extends ActionType> action, boolean allowModifiers)
	{
		this.label = label;
		this.tooltip = tooltip;
		this.allowModifiers = allowModifiers;
		try
		{
			this.action = action.newInstance();
		}
		catch (Exception e)
		{
			throw Throwables.propagate(e);
		}
	}
	
	public String getButtonLabel()
	{
		return this.label;
	}
	
	public String getTooltip()
	{
		return this.tooltip;
	}
	
	public ActionType getAction()
	{
		return this.action;
	}

	public boolean allowsModifiers()
	{
		return this.allowModifiers;
	}
	
	public boolean run(File path, List<InstallationModifier> modifiers)
	{
		return this.action.run(path, modifiers, this);
	}
	
	public boolean run(File path, List<InstallationModifier> modifiers, IInstallerMonitor status)
	{
		return this.action.run(path, modifiers, status);
	}

	public String getFailureMessage()
	{
		return this.action.getFailureMessage();
	}

	public boolean isPathValid(File targetDir)
	{
		return this.action.isPathValid(targetDir);
	}
	
	public String getFileError(File targetDir)
	{
		return this.action.getFileError(targetDir);
	}
	
	public String getSuccessMessage()
	{
		return this.action.getSuccessMessage();
	}
	
	public JPanel getOptionsPanel()
	{
		return this.action.getOptionsPanel();
	}

	public static void refreshActions(File targetDir, InstallerAction selected)
	{
		for (InstallerAction action : InstallerAction.values())
		{
			action.action.setSelected(action == selected);
			action.action.refresh(targetDir);
		}
	}

	@Override
	public boolean isCancelled()
	{
		return false;
	}

	@Override
	public void setProgressMessage(String message)
	{
	}

	@Override
	public void setProgress(int progress)
	{
	}

	@Override
	public void setProgressAndMessage(int progress, String message)
	{
	}

	@Override
	public void beginTask(String message)
	{
	}

	@Override
	public void onTaskCompleted(boolean success, String message)
	{
	}
}
