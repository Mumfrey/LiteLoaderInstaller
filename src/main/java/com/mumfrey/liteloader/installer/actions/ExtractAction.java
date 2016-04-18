package com.mumfrey.liteloader.installer.actions;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.mumfrey.liteloader.installer.VersionInfo;
import com.mumfrey.liteloader.installer.gui.IInstallerMonitor;
import com.mumfrey.liteloader.installer.modifiers.InstallationModifier;

public class ExtractAction implements ActionType
{
    private String lastError = null;

    @Override
    public boolean run(File target, List<InstallationModifier> modifiers, IInstallerMonitor monitor)
    {
        String containedFile = VersionInfo.getContainedFile();
        File file = new File(target, containedFile);
        try
        {
            VersionInfo.extractFile(file, containedFile);
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(null, "An error occurred extracting file", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    @Override
    public String getLabelSuffix()
    {
        return "";
    }

    @Override
    public JPanel getOptionsPanel()
    {
        return null;
    }

    @Override
    public boolean isEnabled()
    {
        return true;
    }

    @Override
    public void setSelected(boolean selected)
    {
    }

    @Override
    public void refresh(File targetDir)
    {
    }

    @Override
    public String getFailureMessage()
    {
        return this.lastError;
    }

    @Override
    public boolean isPathValid(File targetDir)
    {
        return targetDir.exists() && targetDir.isDirectory();
    }

    @Override
    public String getFileError(File targetDir)
    {
        return !targetDir.exists() ? "Target directory does not exist" : !targetDir.isDirectory() ? "Target is not a directory" : "";
    }

    @Override
    public String getSuccessMessage()
    {
        return "Extracted successfully";
    }

}
