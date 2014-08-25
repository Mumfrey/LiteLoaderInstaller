package com.mumfrey.liteloader.installer.targets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import argo.jdom.JdomParser;
import argo.jdom.JsonField;
import argo.jdom.JsonNodeFactories;
import argo.jdom.JsonRootNode;
import argo.jdom.JsonStringNode;
import argo.saj.InvalidSyntaxException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.mumfrey.liteloader.installer.VersionInfo;
import com.mumfrey.liteloader.installer.modifiers.ActionModifier;
import com.mumfrey.liteloader.installer.modifiers.InstallationModifier;

public class TargetVersion implements InstallationModifier
{
	Pattern forgeVersionPattern = Pattern.compile("forge([0-9\\.]+)", Pattern.CASE_INSENSITIVE);
	Pattern fmlVersionPattern = Pattern.compile("fml([0-9\\.]+)", Pattern.CASE_INSENSITIVE);
	Pattern ofVersionPattern = Pattern.compile("optifine_([a-z0-9_]+)$", Pattern.CASE_INSENSITIVE);

	private final String name;
	private final boolean valid;
	private boolean vanilla;
	private final ActionModifier modifier;
	private final String minecraftArguments;
	
	public TargetVersion(File file) throws IllegalArgumentException, FileNotFoundException, IOException, InvalidSyntaxException
	{
		if (!file.isDirectory())
		{
			throw new IllegalArgumentException("Version must be a directory reading: " + file.getAbsolutePath());
		}
		
		String name = file.getName();
		File json = new File(file, name + ".json");
		if (!json.isFile())
		{
			throw new IllegalArgumentException("Version json file not found reading: " + json.getAbsolutePath());
		}
		
		JsonRootNode versionData = new JdomParser().parse(Files.newReader(json, Charsets.UTF_8));
		String versionId = versionData.getStringValue("id");
		if (versionId == null || !versionId.equals(name))
		{
			throw new IllegalArgumentException("Version id does not match container in: " + json.getAbsolutePath());
		}
		
		this.name = name;
		this.valid = TargetVersion.isValid(name);
		this.vanilla = false;
		this.modifier = null;
		this.minecraftArguments = TargetVersion.getMinecraftArguments(versionData);
	}

	public TargetVersion(String name)
	{
		this(name, null);
	}

	public TargetVersion(String name, ActionModifier modifier)
	{
		this.name = name;
		this.valid = TargetVersion.isValid(name);
		this.vanilla = true;
		this.modifier = modifier;
		this.minecraftArguments = null;
	}
	
	@Override
	public String getExclusivityKey()
	{
		return this.name;
	}
	
	@Override
	public JsonRootNode modifyVersion(JsonRootNode versionJson)
	{
		try
		{
			List<JsonField> copyFields = new ArrayList<JsonField>();
			
			for (JsonField field : versionJson.getFieldList())
			{
				JsonStringNode fieldName = field.getName();
				
				if ("id".equals(fieldName.getText()))
				{
					field = new JsonField(fieldName, JsonNodeFactories.string(VersionInfo.getVersionTarget(this)));
				}
				else if ("minecraftArguments".equals(fieldName.getText()) && this.minecraftArguments != null)
				{
					field = new JsonField(fieldName, JsonNodeFactories.string("--tweakClass " + VersionInfo.getTweakClass() + " " + this.minecraftArguments));
				}
				else if ("inheritsFrom".equals(fieldName.getText()))
				{
					field = new JsonField(fieldName, JsonNodeFactories.string(this.name));
				}
				
				copyFields.add(field);
			}
			
			return JsonNodeFactories.object(copyFields);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error modifying version JSON: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
		
		return versionJson;
	}

	@Override
	public void modifyJvmArgs(List<InstallationModifier> modifiers, Set<String> jvmArgs)
	{
	}
		
	@Override
	public void modifyFields(List<JsonField> fields)
	{
	}

	public String getName()
	{
		return this.name;
	}
	
	public boolean isValid()
	{
		return this.valid;
	}
	
	public ActionModifier getModifier()
	{
		return this.modifier;
	}

	public boolean isVanilla()
	{
		return this.vanilla;
	}

	private static boolean isValid(String name)
	{
		if (name.toLowerCase().contains("liteloader")) return false;
		String minecraftVersion = VersionInfo.getMinecraftVersion();
		return name.equals(minecraftVersion) || name.startsWith(minecraftVersion + "-");
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (other == this) return true;
		if (!(other instanceof TargetVersion)) return false;
		return ((TargetVersion)other).name.equals(this.name);
	}
	
	@Override
	public int hashCode()
	{
		return this.name.hashCode() * 37;
	}
	
	@Override
	public String toString()
	{
		return this.isValid() ? this.name : String.format("<html><font color=\"red\"><i>%s</i></font></html>", this.name); 
	}

	public void print()
	{
		System.err.println("Version: " + this.name + "\n{");
		System.err.println("   vanilla=" + this.vanilla);
		System.err.println("   minecraftArguments=\"" + this.minecraftArguments + "\"\n}");
	}

	public String getSuggestedProfileName()
	{
		String profileName = VersionInfo.getProfileName();
		if (this.isVanilla()) return profileName;
		
		String lcase = this.name.toLowerCase();
		
		if (lcase.contains("forge"))   return profileName + " with Forge" + this.guessVersion(this.forgeVersionPattern);
		if (lcase.contains("fml"))     return profileName + " with FML" + this.guessVersion(this.fmlVersionPattern);
		if (lcase.contains("optif"))   return profileName + " with Optifine" + this.guessVersion(this.ofVersionPattern);
		if (lcase.contains("mcpatch")) return profileName + " with MCPatcher";
		if (lcase.contains("shader"))  return profileName + " with Shaders";
		
		return profileName + " with " + this.getName().replace(VersionInfo.getMinecraftVersion(), "").replace("--", "-").replaceAll("^[\\-_]", "");
	}

	private String guessVersion(Pattern pattern)
	{
		Matcher matcher = pattern.matcher(this.name);
		if (matcher.find())
		{
			String ver = matcher.group(1);
			return ver != null ? " " + ver : "";
		}
		return "";
	}

	/**
	 * @param versionData
	 * @return
	 */
	private static String getMinecraftArguments(JsonRootNode versionData)
	{
		String minecraftArguments = versionData.getStringValue("minecraftArguments");
		if (minecraftArguments != null)
		{
			minecraftArguments = minecraftArguments.replace("optifine.OptiFineTweaker", "optifine.OptiFineForgeTweaker");
		}
		
		return minecraftArguments;
	}
}
