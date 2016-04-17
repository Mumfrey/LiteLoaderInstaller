package com.mumfrey.liteloader.installer.targets;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import com.mumfrey.liteloader.installer.VersionInfo;

public class VersionList
{
    public static final TargetVersion BASE_VERSION = new TargetVersion(VersionInfo.getMinecraftVersion());

    private final File versionsDir;
	
	private final Set<TargetVersion> allVersions = new LinkedHashSet<TargetVersion>();
	private final Set<TargetVersion> validVersions = new LinkedHashSet<TargetVersion>();
	private final Set<TargetVersion> liteLoaderVersions = new LinkedHashSet<TargetVersion>();

	public VersionList(File versionsDir)
	{
		this.versionsDir = versionsDir;
		this.update();
	}
	
	public File getVersionsDir()
	{
		return this.versionsDir;
	}
	
	public Set<TargetVersion> getVersions(boolean showInvalid)
	{
		return showInvalid ? this.allVersions : this.validVersions;
	}
	
	public Set<TargetVersion> getLiteLoaderVersions()
	{
		return this.liteLoaderVersions;
	}
	
	public void update()
	{
		this.allVersions.clear();
		this.validVersions.clear();
		this.liteLoaderVersions.clear();
		this.allVersions.add(VersionList.BASE_VERSION);
		this.validVersions.add(VersionList.BASE_VERSION);
		
		if (this.versionsDir != null && this.versionsDir.isDirectory())
		{
			for (File versionDir : this.versionsDir.listFiles())
			{
				if (versionDir.isDirectory())
				{
					this.addVersion(versionDir);
				}
			}
		}
		
//		for (TargetVersion ver : this.allVersions)
//			ver.print();
//		System.exit(0);
	}

	private void addVersion(File versionDir)
	{
		try
		{
			TargetVersion version = new TargetVersion(versionDir);
			this.allVersions.add(version);
			if (version.isValid())
		    {
			    this.validVersions.add(version);
			    if (VersionList.BASE_VERSION.equals(version))
			    {
			        VersionList.BASE_VERSION.copyArgsFrom(version);
			    }
		    }
			
			if (version.isLiteLoaderVersion())
		    {
			    this.liteLoaderVersions.add(version);
		    }
		}
		catch (Exception ex)
		{
			System.err.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
	}
}
