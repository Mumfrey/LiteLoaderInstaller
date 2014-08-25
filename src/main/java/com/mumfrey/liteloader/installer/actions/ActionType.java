package com.mumfrey.liteloader.installer.actions;

import java.io.File;
import java.util.List;

import javax.swing.JPanel;

import com.mumfrey.liteloader.installer.gui.IInstallerMonitor;
import com.mumfrey.liteloader.installer.modifiers.InstallationModifier;

public interface ActionType
{
	public abstract boolean run(File target, List<InstallationModifier> modifiers, IInstallerMonitor monitor);

	public abstract void setSelected(boolean selected);

	public abstract void refresh(File targetDir);
	
	public abstract boolean isPathValid(File targetDir);
	
	public abstract String getFileError(File targetDir);
	
	public abstract String getSuccessMessage();
	
	public abstract String getFailureMessage();

	public abstract JPanel getOptionsPanel();
}
