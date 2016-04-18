package com.mumfrey.liteloader.installer.actions;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import com.mumfrey.liteloader.installer.OperationCancelledException;
import com.mumfrey.liteloader.installer.VersionInfo;
import com.mumfrey.liteloader.installer.gui.CancelledException;
import com.mumfrey.liteloader.installer.gui.IInstallerMonitor;
import com.mumfrey.liteloader.installer.modifiers.InstallationModifier;
import com.mumfrey.liteloader.installer.targets.TargetVersion;

public class ClientUpgradeAction extends ClientAction
{
    private boolean versionExists;

    private String existingVersion = null;
    
    public ClientUpgradeAction()
    {
    }

    @Override
    public String getLabelSuffix()
    {
        return this.existingVersion != null ? " (current version: " + this.existingVersion + ")" : "";
    }

    @Override
    public boolean run(File target, List<InstallationModifier> modifiers, IInstallerMonitor monitor)
    {
        try
        {
            if (!this.validateTarget(target)) return false;

            File launcherProfiles = new File(target, VersionInfo.getLauncherProfilesJson());
            if (!this.validateLauncherProfiles(launcherProfiles)) return false;
            if (monitor.isCancelled()) throw new OperationCancelledException();
            
            if (!this.extractLibraries(target, monitor)) return false;
            if (monitor.isCancelled()) throw new OperationCancelledException();
        }
        catch (CancelledException ex)
        {
            if (this.returnFalseOnBadState()) return false;
            throw ex;
        }

        return true;
    }

    @Override
    public boolean isEnabled()
    {
        return this.currentPathIsValid && this.existingVersion != null && this.versionExists;
    }
    
    @Override
    public void setSelected(boolean selected)
    {
    }

    @Override
    public void refresh(File targetDir)
    {
        super.refresh(targetDir);

        this.versionExists = false;
        this.existingVersion = null;

        for (TargetVersion version : this.versionList.getLiteLoaderVersions())
        {
            if (version.getName().toLowerCase().contains("lite"))
            {
                this.versionExists = true;
                break;
            }
        }

        File libraries = new File(targetDir, "libraries");
        File targetLibraryFile = VersionInfo.getLibraryPath(libraries);
        if (targetLibraryFile.isFile())
        {
            this.existingVersion = this.readManifestEntry(targetLibraryFile, "Implementation-Version");
        }
    }

    private String readManifestEntry(File file, String attribute)
    {
        JarFile jar = null;
        try
        {
            jar = new JarFile(file);
            Manifest manifest = jar.getManifest();
            Attributes attributes = manifest.getMainAttributes();
            return attributes.getValue(attribute);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            if (jar != null)
            {
                try
                {
                    jar.close();
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            }
        }

        return null;
    }
    
    @Override
    public String getSuccessMessage()
    {
        return String.format("<html>Successfully upgraded \"<font color=\"blue\"><b>%s</b></font>\" from previous version <font color=\"blue\"><b>%s</b></font>.", VersionInfo.getContainedFile(), this.existingVersion);
    }
}
