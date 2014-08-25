package com.mumfrey.liteloader.installer.actions;

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.mumfrey.liteloader.installer.OperationCancelledException;
import com.mumfrey.liteloader.installer.VersionInfo;
import com.mumfrey.liteloader.installer.gui.CancelledException;
import com.mumfrey.liteloader.installer.gui.IInstallerMonitor;
import com.mumfrey.liteloader.installer.targets.VersionList;

public abstract class ClientAction implements ActionType
{
	protected static final int LEFT_MARGIN = 130;
	protected static final int RIGHT_MARGIN = 60;
	
	protected VersionList versionList;
	
	protected String lastError = null;

	protected JPanel optionsPanel;
	
	protected boolean currentPathIsValid = false;
	
	@Override
	public final JPanel getOptionsPanel()
	{
		return this.optionsPanel;
	}

	@Override
	public void refresh(File targetDir)
	{
		this.currentPathIsValid = this.validatePath(targetDir);

		if (targetDir != null)
		{
			this.versionList = new VersionList(new File(targetDir, "versions"));
		}
	}

	protected boolean validateTarget(File target) throws HeadlessException
	{
		if (!target.exists())
		{
			if (!this.setLastError("noMinecraft"))
			{
				this.showMessageDialog(null, "There is no minecraft installation at this location!", "Error", JOptionPane.ERROR_MESSAGE);
			}
			throw new CancelledException();
		}
		
		return true;
	}

	protected boolean validateLauncherProfiles(File launcherProfiles) throws HeadlessException
	{
		if (!launcherProfiles.exists())
		{
			if (!this.setLastError("noLauncher"))
			{
				this.showMessageDialog(null, "There is no minecraft launcher profile at this location, you need to run the launcher first!", "Error", JOptionPane.ERROR_MESSAGE);
			}
			
			throw new CancelledException();
		}
		
		return true;
	}

	/**
	 * @param target
	 * @param monitor
	 * @return
	 * @throws HeadlessException
	 * @throws OperationCancelledException
	 */
	protected boolean extractLibraries(File target, IInstallerMonitor monitor) throws HeadlessException, com.mumfrey.liteloader.installer.OperationCancelledException
	{
		File libraries = new File(target, "libraries");
		File targetLibraryFile = VersionInfo.getLibraryPath(libraries);
		if (!this.extractLibrary(targetLibraryFile, VersionInfo.getContainedFile())) return false;
		return true;
	}

	protected final boolean extractLibrary(File targetLibraryFile, String containedFile) throws HeadlessException
	{
		if (!targetLibraryFile.getParentFile().mkdirs() && !targetLibraryFile.getParentFile().isDirectory())
		{
			if (!targetLibraryFile.getParentFile().delete())
			{
				if (!this.setLastError("noTarget", targetLibraryFile.getAbsolutePath()))
				{
					this.showMessageDialog(null, "There was a problem with the launcher version data. You will need to clear " + targetLibraryFile.getAbsolutePath() + " manually", "Error", JOptionPane.ERROR_MESSAGE);
				}
				return false;
			}
			
			targetLibraryFile.getParentFile().mkdirs();
		}
		
		try
		{
			this.logInfo("Extracting %s...", containedFile);
			VersionInfo.extractFile(targetLibraryFile, containedFile);
		}
		catch (Exception e)
		{
			if (!this.setLastError("copyLibFail", containedFile))
			{
				this.showMessageDialog(null, "There was a problem writing the system library file", "Error", JOptionPane.ERROR_MESSAGE);
			}
			return false;
		}
		
		return true;
	}

	@Override
	public final boolean isPathValid(File targetDir)
	{
		return this.currentPathIsValid;
	}

	/**
	 * @param targetDir
	 * @return
	 */
	private boolean validatePath(File targetDir)
	{
		if (targetDir == null) return false;
		
		if (targetDir.isDirectory())
		{
			File launcherProfiles = new File(targetDir, "launcher_profiles.json");
			return launcherProfiles.isFile();
		}
		
		return false;
	}
	
	@Override
	public String getFileError(File targetDir)
	{
		if (targetDir.exists())
		{
			return "The directory is missing a launcher profile. Please run the minecraft launcher first";
		}
		
		return "Invalid minecraft directory. Choose an alternative, or run the minecraft launcher to create one.";
	}

	@Override
	public final String getFailureMessage()
	{
		return this.lastError;
	}
	
	protected boolean setLastError(String messageName, Object... params)
	{
		return false;
	}

	protected void showMessageDialog(Component parentComponent, Object message, String title, int messageType)
    {
    	JOptionPane.showMessageDialog(parentComponent, message, title, messageType);
    }

	protected boolean returnFalseOnBadState()
	{
		return false;
	}

	protected void logInfo(String format, Object... args)
	{
		System.out.println(String.format(format, args));
	}
}