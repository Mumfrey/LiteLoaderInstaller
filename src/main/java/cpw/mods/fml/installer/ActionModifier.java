package cpw.mods.fml.installer;

import java.io.File;

import argo.jdom.JsonRootNode;

public interface ActionModifier {

    JsonRootNode modifyVersion(JsonRootNode versionJson);

    String getLabel();

    String getTooltip();
    
    void refresh(boolean valid, File targetDir);
    
    boolean isAvailable();
}
