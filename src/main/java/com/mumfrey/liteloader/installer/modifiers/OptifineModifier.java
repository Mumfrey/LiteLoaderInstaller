package com.mumfrey.liteloader.installer.modifiers;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;

import com.mumfrey.liteloader.installer.VersionInfo;

import argo.jdom.JsonField;
import argo.jdom.JsonNode;
import argo.jdom.JsonNodeBuilders;
import argo.jdom.JsonNodeFactories;

public class OptifineModifier extends CascadeModifier
{
	protected String name = null;
	
	protected String version = null;
	
	protected String minRequiredVersion = "0.0.0.0";
	
	protected boolean isAvailable;
	
	public OptifineModifier()
	{
		this("Optifine", "");
	}
	
	protected OptifineModifier(String name, String minRequiredVersion)
	{
		this.name = name;
		this.minRequiredVersion = minRequiredVersion;
		this.versionPrefix = VersionInfo.getMinecraftVersion() + "_";
	}
	
	@Override
	public void modifyFields(List<JsonField> fields)
	{
		super.modifyFields(fields);
		
		fields.add(JsonNodeFactories.field("javaArgs", JsonNodeFactories.string("-Xmx1G -Dfml.ignoreInvalidMinecraftCertificates=true -Dfml.ignorePatchDiscrepancies=true")));
	}
	
	@Override
	public String getLabel()
	{
		return String.format("Chain to %s %s", this.name, this.version != null ? (this.hasMultipleVersions() ? "" : this.version) : "(Not detected)");
	}
	
	@Override
	public String getTooltip()
	{
		return String.format("Chains the LiteLoader tweaker to the %s tweaker", this.name);
	}
	
	@Override
	protected String getFailureMessage()
	{
		return String.format("There was a problem chaining to %s, check the version JSON", this.name);
	}
	
	@Override
	protected String getTweakClass()
	{
		return "optifine.OptiFineForgeTweaker";
	}
	
	@Override
	public String getExclusivityKey()
	{
		return "optifine";
	}
	
	/**
	 * @return
	 */
	@Override
	protected String getLibPath()
	{
		return "optifine/OptiFine";
	}
	
	@Override
	protected String getLibName()
	{
		return "optifine:OptiFine";
	}
	
	@Override
	protected String getLibHint()
	{
		return "optifine";
	}
	
	@Override
	public boolean isAvailable()
	{
		return this.isAvailable;
	}
	
	@Override
	public String getLatestVersion()
	{
		return null;
	}
	
	@Override
	protected void addLibraries(List<JsonNode> libraries)
	{
		JsonNode forgeNode = JsonNodeBuilders.anObjectBuilder().withField("name", JsonNodeBuilders.aStringBuilder("optifine:OptiFine:" + this.versionPrefix + this.version))
		// .withField("url",
		// JsonNodeBuilders.aStringBuilder("http://files.minecraftforge.net/maven/"))
				.build();
		libraries.add(forgeNode);
	}
	
	@Override
	public void prepare(Object object)
	{
		if (object instanceof JComboBox)
		{
			Object selectedItem = ((JComboBox)object).getSelectedItem();
			if (selectedItem != null)
				this.version = selectedItem.toString();
		}
	}
	
	@Override
	public void refresh(boolean valid, File targetDir, Map<String, List<JsonNode>> versionLibraries)
	{
		this.isAvailable = false;
		this.version = null;
		this.validVersions.clear();
		if (valid)
		{
			String version = this.getLatestLibraryVersion(targetDir, this.getLibPath(), this.getLibName(), versionLibraries);
			if (version == null)
				return;
			
			String maxVersion = this.getMaxVersion(this.minRequiredVersion, version);
			
			if (maxVersion != this.minRequiredVersion)
			{
				this.version = version;
				this.isAvailable = valid;
			}
		}
	}
	
	@Override
	protected boolean isValidLibrary(String name, String libVersion)
	{
		return true;
	}
	
	@Override
	protected String getMaxVersion(String maxVersion, String version)
	{
		return version;
	}
}
