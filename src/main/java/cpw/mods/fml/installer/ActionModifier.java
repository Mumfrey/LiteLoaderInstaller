package cpw.mods.fml.installer;

import argo.jdom.JsonRootNode;

public interface ActionModifier {

    JsonRootNode modifyVersion(JsonRootNode versionJson);

    String getLabel();

    String getTooltip();
}
