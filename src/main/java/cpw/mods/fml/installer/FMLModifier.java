package cpw.mods.fml.installer;

import java.util.List;

import argo.jdom.JsonNode;
import argo.jdom.JsonNodeBuilders;

public class FMLModifier extends ForgeModifier
{
    public FMLModifier()
    {
        super("Forge ModLoader", "6.4.0.752");
    }
    
    @Override
    public String getTooltip()
    {
        return "Chains the LiteLoader tweaker to the Forge ModLoader tweaker";
    }
    
    @Override
    protected String getFailureMessage()
    {
        return "There was a problem chaining to Forge ModLoader, check the version JSON";
    }

    /**
     * @return
     */
    @Override
    protected String getLibPath()
    {
        return "cpw/mods/fml";
    }

    @Override
    protected void addLibraries(List<JsonNode> libraries)
    {
        JsonNode forgeNode = JsonNodeBuilders.anObjectBuilder()
            .withField("name", JsonNodeBuilders.aStringBuilder("cpw.mods:fml:" + this.version))
            .withField("url", JsonNodeBuilders.aStringBuilder("http://files.minecraftforge.net/maven/"))
            .build();
        libraries.add(forgeNode);
        
        this.addCommonLibraries(libraries);
    }
}
