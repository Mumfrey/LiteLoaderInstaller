package com.mumfrey.liteloader.installer.modifiers;

import java.util.List;
import java.util.Set;

import argo.jdom.JsonField;
import argo.jdom.JsonRootNode;

public interface InstallationModifier
{
	public abstract String getExclusivityKey();
	
	public abstract JsonRootNode modifyVersion(JsonRootNode versionJson);
	
	public abstract void modifyFields(List<JsonField> fields);
	
	public abstract void modifyJvmArgs(List<InstallationModifier> modifiers, Set<String> jvmArgs);
}
