package com.mumfrey.liteloader.installer.gui;

import java.awt.Color;

import javax.swing.JRadioButton;

import com.mumfrey.liteloader.installer.actions.InstallerAction;

public class ActionRadioButton extends JRadioButton
{
	private static final long serialVersionUID = 1L;
	
	private InstallerAction action;

	public ActionRadioButton(InstallerAction action, boolean selected)
	{
		this.action = action;
	}

	public InstallerAction getInstallerAction()
	{
		return this.action;
	}

	public void updateRadioButton()
	{
		this.setEnabled(this.action.isEnabled());
		this.setForeground(this.action.isEnabled() ? InstallerPanel.FOREGROUND_COLOUR : Color.GRAY);
		this.setText(this.action.getButtonLabel());
	}
}
