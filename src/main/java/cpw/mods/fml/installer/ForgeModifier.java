package cpw.mods.fml.installer;

import java.io.File;
import java.util.List;

import argo.jdom.JsonNode;
import argo.jdom.JsonNodeBuilders;

public class ForgeModifier extends CascadeModifier
{
    private String forgeVersion = "9.10.0.804";
    
    protected boolean isAvailable;

    @Override
    public String getLabel()
    {
        return "Chain to Minecraft Forge " + this.forgeVersion;
    }
    
    @Override
    public String getTooltip()
    {
        return "Chains the LiteLoader tweaker to the Minecraft Forge tweaker";
    }
    
    @Override
    protected String getFailureMessage()
    {
        return "There was a problem chaining to Minecraft Forge, check the version JSON";
    }
    
    @Override
    protected String getTweakClass()
    {
        return "cpw.mods.fml.common.launcher.FMLTweaker";
    }
    
    @Override
    public String getExclusivityKey()
    {
        return "fml";
    }

    /**
     * @return
     */
    protected String getLibPath()
    {
        return "net/minecraftforge/minecraftforge";
    }
    
    @Override
    public boolean isAvailable()
    {
        return this.isAvailable;
    }

    @Override
    protected void addLibraries(List<JsonNode> libraries)
    {
        JsonNode forgeNode = JsonNodeBuilders.anObjectBuilder()
            .withField("name", JsonNodeBuilders.aStringBuilder("net.minecraftforge:minecraftforge:" + this.forgeVersion))
            .withField("url", JsonNodeBuilders.aStringBuilder("http://files.minecraftforge.net/maven/"))
            .build();
        
        JsonNode asmNode = JsonNodeBuilders.anObjectBuilder()
                .withField("name", JsonNodeBuilders.aStringBuilder("org.ow2.asm:asm-all:4.1"))
                .build();
        
        libraries.add(forgeNode);
        libraries.add(asmNode);
    }
    
    @Override
    public void refresh(boolean valid, File targetDir)
    {
        this.isAvailable = valid;
        
        if (valid)
        {
            String version = this.getLatestLibraryVersion(targetDir, this.getLibPath());
            if (version != null) this.forgeVersion = version;
        }
    }
}
