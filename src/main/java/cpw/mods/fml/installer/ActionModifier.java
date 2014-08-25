package cpw.mods.fml.installer;

import java.io.File;
import java.util.List;

import argo.jdom.JsonField;
import argo.jdom.JsonRootNode;

public interface ActionModifier {

    JsonRootNode modifyVersion(JsonRootNode versionJson);

    String getLabel();

    String getTooltip();
    
    String getExclusivityKey();
    
    void refresh(boolean valid, File targetDir);
    
    boolean isAvailable();

    void prepare(Object object);

    void modifyFields(List<JsonField> fields);
}
