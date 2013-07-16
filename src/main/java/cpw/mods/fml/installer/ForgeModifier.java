package cpw.mods.fml.installer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import argo.jdom.JsonField;
import argo.jdom.JsonNode;
import argo.jdom.JsonNodeBuilders;
import argo.jdom.JsonNodeFactories;
import argo.jdom.JsonRootNode;
import argo.jdom.JsonStringNode;

public class ForgeModifier implements ActionModifier
{
    private static final String FORGE_VERSION = "9.10.0.793";

    @Override
    public String getLabel()
    {
        return "Chain to Minecraft Forge " + FORGE_VERSION;
    }
    
    @Override
    public String getTooltip()
    {
        return "Chains the LiteLoader tweaker to the Minecraft Forge tweaker";
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
                
                if ("libraries".equals(fieldName.getText()))
                {
                    List<JsonNode> libraries = new ArrayList<JsonNode>();
                    
                    this.addLibraries(libraries);
                    
                    libraries.addAll(field.getValue().getArrayNode());
                    copyFields.add(new JsonField(fieldName, JsonNodeFactories.array(libraries)));
                }
                else if ("minecraftArguments".equals(fieldName.getText()))
                {
                    copyFields.add(new JsonField(fieldName, JsonNodeFactories.string(field.getValue().getText() + " --cascadedTweaks cpw.mods.fml.common.launcher.FMLTweaker")));
                }
                else
                {
                    copyFields.add(field);
                }
            }
            
            return JsonNodeFactories.object(copyFields);
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(null, "There was a problem chaining to Minecraft Forge, check the version JSON", "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return versionJson;
    }

    protected void addLibraries(List<JsonNode> libraries)
    {
        JsonNode forgeNode = JsonNodeBuilders.anObjectBuilder()
            .withField("name", JsonNodeBuilders.aStringBuilder("net.minecraftforge:minecraftforge:" + FORGE_VERSION))
            .withField("url", JsonNodeBuilders.aStringBuilder("http://files.minecraftforge.net/maven/"))
            .build();
        
        JsonNode asmNode = JsonNodeBuilders.anObjectBuilder()
                .withField("name", JsonNodeBuilders.aStringBuilder("org.ow2.asm:asm-all:4.1"))
                .build();
        
        libraries.add(forgeNode);
        libraries.add(asmNode);
    }
}
