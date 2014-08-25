package com.mumfrey.liteloader.installer.modifiers;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;

import com.google.common.base.Throwables;

public enum InstallerModifier
{
	FORGE(ForgeModifier.class), FML(FMLModifier.class), OPTIFINE(OptifineModifier.class);
	
	private ActionModifier modifier;
	
	private InstallerModifier(Class<? extends ActionModifier> modifierType)
	{
		try
		{
			this.modifier = modifierType.newInstance();
		}
		catch (Exception e)
		{
			throw Throwables.propagate(e);
		}
	}
	
	public String getButtonLabel()
	{
		return this.modifier.getLabel();
	}
	
	public String getTooltip()
	{
		return this.modifier.getTooltip();
	}
	
	public ActionModifier getModifier()
	{
		return this.modifier;
	}
	
	private void refresh(boolean valid, File targetDir, Map<String, List<JsonNode>> versionLibraries)
	{
		this.modifier.refresh(valid, targetDir, versionLibraries);
	}
	
	boolean isAvailable()
	{
		return this.modifier.isAvailable();
	}
	
	public String getExclusivityKey()
	{
		return this.modifier.getExclusivityKey();
	}
	
	public static void refreshModifiers(boolean valid, File targetDir)
	{
		Map<String, List<JsonNode>> versionLibraries = InstallerModifier.readVersionLibraries(targetDir);
		
		for (InstallerModifier modifier : InstallerModifier.values())
		{
			modifier.refresh(valid, targetDir, versionLibraries);
		}
	}
	
	/**
	 * @param targetDir
	 * @return
	 */
	private static Map<String, List<JsonNode>> readVersionLibraries(File targetDir)
	{
		Map<String, List<JsonNode>> versionLibraries = new HashMap<String, List<JsonNode>>();
		
		File versions = new File(targetDir, "versions");
		if (versions.exists() && versions.isDirectory())
		{
			for (File version : versions.listFiles())
			{
				if (version.isDirectory())
				{
					String versionName = version.getName();
					versionLibraries.put(versionName, new ArrayList<JsonNode>());
					File versionJson = new File(version, versionName + ".json");
					if (versionJson.exists())
					{
						try
						{
							JdomParser parser = new JdomParser();
							JsonRootNode json = parser.parse(new FileReader(versionJson));
							List<JsonNode> libraries = json.getArrayNode("libraries");
							for (JsonNode library : libraries)
							{
								versionLibraries.get(versionName).add(library);
							}
						}
						catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}
				}
			}
		}
		return versionLibraries;
	}
}
