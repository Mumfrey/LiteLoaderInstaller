package cpw.mods.fml.installer;

import java.util.List;

import argo.jdom.JsonNode;
import argo.jdom.JsonNodeBuilders;

public class FMLModifier extends ForgeModifier
{
    private String fmlVersion = "6.2.38.726";
    
    @Override
    public String getLabel()
    {
        return "Chain to Forge ModLoader " + this.fmlVersion;
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
            .withField("name", JsonNodeBuilders.aStringBuilder("cpw.mods:fml:" + this.fmlVersion))
            .withField("url", JsonNodeBuilders.aStringBuilder("http://files.minecraftforge.net/maven/"))
            .build();
        
        JsonNode asmNode = JsonNodeBuilders.anObjectBuilder()
                .withField("name", JsonNodeBuilders.aStringBuilder("org.ow2.asm:asm-all:4.1"))
                .build();
        
        libraries.add(forgeNode);
        libraries.add(asmNode);
    }
}
