package com.mumfrey.liteloader.installer;

import java.io.File;
import java.util.Locale;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.mumfrey.liteloader.installer.gui.InstallerPanel;

public class Installer
{
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        try
        {
            if (System.getProperty("os.name").toLowerCase().contains("win"))
            {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        }
        catch (Throwable th)
        {
        }

        String userHomeDir = System.getProperty("user.home", ".");
        String osType = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        File targetDir = null;
        String mcDir = ".minecraft";
        if (osType.contains("win") && System.getenv("APPDATA") != null)
        {
            targetDir = new File(System.getenv("APPDATA"), mcDir);
        }
        else if (osType.contains("mac"))
        {
            targetDir = new File(new File(new File(userHomeDir, "Library"), "Application Support"), "minecraft");
        }
        else
        {
            targetDir = new File(userHomeDir, mcDir);
        }

        try
        {
            VersionInfo.getVersionTarget("");
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Corrupt download detected, cannot install", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        InstallerPanel panel = new InstallerPanel(false, targetDir);
        try
        {
            while (!panel.run());
        }
        catch (Throwable th)
        {
            th.printStackTrace();
        }

        System.exit(0);
    }

}
