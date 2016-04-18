package com.mumfrey.liteloader.installer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import com.mumfrey.liteloader.installer.targets.TargetVersion;

import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;

public class VersionInfo
{
    public static final VersionInfo INSTANCE = new VersionInfo();
    public final JsonRootNode versionData;

    public VersionInfo()
    {
        InputStream installProfile = getClass().getResourceAsStream("/install_profile.json");
        JdomParser parser = new JdomParser();

        try
        {
            this.versionData = parser.parse(new InputStreamReader(installProfile, Charsets.UTF_8));
        }
        catch (Exception e)
        {
            throw Throwables.propagate(e);
        }
    }

    public static String getProfileName()
    {
        return INSTANCE.versionData.getStringValue("install", "profileName");
    }

    public static String getVersionTarget(String suffix)
    {
        return INSTANCE.versionData.getStringValue("install", "target") + suffix;
    }

    public static String getVersionTarget(TargetVersion target)
    {
        if (target.isVanilla())
        {
            return VersionInfo.getVersionTarget("");
        }

        return INSTANCE.versionData.getStringValue("install", "target") + "-" + target.getName();
    }

    public static File getLibraryPath(File root)
    {
        String path = INSTANCE.versionData.getStringValue("install", "path");
        String[] split = Iterables.toArray(Splitter.on(':').omitEmptyStrings().split(path), String.class);
        File dest = root;
        Iterable<String> subSplit = Splitter.on('.').omitEmptyStrings().split(split[0]);
        for (String part : subSplit)
        {
            dest = new File(dest, part);
        }
        dest = new File(new File(dest, split[1]), split[2]);
        String fileName = split[1] + "-" + split[2] + ".jar";
        return new File(dest, fileName);
    }

    public static String getVersion()
    {
        return INSTANCE.versionData.getStringValue("install", "version");
    }

    public static String getWelcomeMessage()
    {
        return INSTANCE.versionData.getStringValue("install", "welcome");
    }

    public static String getDialogTitle()
    {
        return INSTANCE.versionData.getStringValue("install", "title");
    }

    public static String getBackgroundFileName()
    {
        return INSTANCE.versionData.getStringValue("install", "bg");
    }

    public static String getIconFileName()
    {
        return INSTANCE.versionData.getStringValue("install", "icon");
    }
    
    public static String getTweakClass()
    {
        return INSTANCE.versionData.getStringValue("install", "tweakClass");
    }

    public static String getLauncherProfilesJson()
    {
        return INSTANCE.versionData.getStringValue("install", "launcherProfilesJson");
    }
    
    public static JsonNode getVersionInfo()
    {
        return INSTANCE.versionData.getNode("versionInfo");
    }

    public static List<JsonNode> getLibraries()
    {
        return INSTANCE.versionData.getNode("versionInfo").getArrayNode("libraries");
    }

    public static File getMinecraftFile(File path)
    {
        return new File(new File(path, getMinecraftVersion()), getMinecraftVersion() + ".jar");
    }

    public static String getContainedFile()
    {
        return INSTANCE.versionData.getStringValue("install", "filePath");
    }

    public static void extractFile(File path, String containedFile) throws IOException
    {
        INSTANCE.doFileExtract(path, containedFile);
    }

    private void doFileExtract(File path, String containedFile) throws IOException
    {
        InputStream inputStream = getClass().getResourceAsStream("/" + containedFile);
        Files.asByteSink(path).writeFrom(inputStream);
    }

    public static String getMinecraftVersion()
    {
        return INSTANCE.versionData.getStringValue("install", "minecraft");
    }
}
