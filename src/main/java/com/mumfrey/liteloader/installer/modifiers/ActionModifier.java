package com.mumfrey.liteloader.installer.modifiers;

import java.io.File;
import java.util.List;
import java.util.Map;

import argo.jdom.JsonNode;

public interface ActionModifier extends InstallationModifier
{
	public abstract String getLabel();
	
	public abstract String getTooltip();
	
	public abstract void refresh(boolean valid, File targetDir, Map<String, List<JsonNode>> versionLibraries);
	
	public abstract boolean isAvailable();
	
	public abstract void prepare(Object object);
}
