package com.mumfrey.liteloader.installer.targets;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import com.mumfrey.liteloader.installer.VersionInfo;

public class VersionList
{
	private final File versionsDir;
	
	private final Set<TargetVersion> allVersions = new LinkedHashSet<TargetVersion>();
	private final Set<TargetVersion> validVersions = new LinkedHashSet<TargetVersion>();

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
	
	public void update()
	{
		this.allVersions.clear();
		this.validVersions.clear();
		TargetVersion baseVersion = new TargetVersion(VersionInfo.getMinecraftVersion());
		this.allVersions.add(baseVersion);
		this.validVersions.add(baseVersion);
		
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
			if (version.isValid()) this.validVersions.add(version);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
