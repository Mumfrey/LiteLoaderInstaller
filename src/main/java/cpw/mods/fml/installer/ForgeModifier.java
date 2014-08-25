package cpw.mods.fml.installer;

import java.io.File;
import java.util.List;

import argo.jdom.JsonNode;
import argo.jdom.JsonNodeBuilders;

public class ForgeModifier extends CascadeModifier
{
    protected String name = null;
    
    protected String version = null;
    
    protected String minRequiredVersion = "0.0.0.0";
    
    protected boolean isAvailable;
    
    public ForgeModifier()
    {
        this("Minecraft Forge", "9.11.0.879");
    }
    
    protected ForgeModifier(String name, String minRequiredVersion)
    {
        this.name = name;
        this.minRequiredVersion = minRequiredVersion;
    }

    @Override
    public String getLabel()
    {
        return String.format("Chain to %s %s", this.name, this.version != null ? this.version : "(Not detected)");
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
            .withField("name", JsonNodeBuilders.aStringBuilder("net.minecraftforge:minecraftforge:" + this.version))
            .withField("url", JsonNodeBuilders.aStringBuilder("http://files.minecraftforge.net/maven/"))
            .build();
        libraries.add(forgeNode);
        
        this.addCommonLibraries(libraries);
    }

    /**
     * @param libraries
     */
    protected void addCommonLibraries(List<JsonNode> libraries)
    {
        JsonNode asmNode = JsonNodeBuilders.anObjectBuilder()
                .withField("name", JsonNodeBuilders.aStringBuilder("org.ow2.asm:asm-all:4.1"))
                .build();
        libraries.add(asmNode);

        JsonNode scalaLibraryNode = JsonNodeBuilders.anObjectBuilder()
                .withField("name", JsonNodeBuilders.aStringBuilder("org.scala-lang:scala-library:2.10.2"))
                .withField("url", JsonNodeBuilders.aStringBuilder("http://files.minecraftforge.net/maven/"))
                .build();
        libraries.add(scalaLibraryNode);

        JsonNode scalaCompilerNode = JsonNodeBuilders.anObjectBuilder()
                .withField("name", JsonNodeBuilders.aStringBuilder("org.scala-lang:scala-compiler:2.10.2"))
                .withField("url", JsonNodeBuilders.aStringBuilder("http://files.minecraftforge.net/maven/"))
                .build();
        libraries.add(scalaCompilerNode);
    }
    
    @Override
    public void refresh(boolean valid, File targetDir)
    {
        this.isAvailable = false;
        this.version = null;
        
        if (valid)
        {
            String version = this.getLatestLibraryVersion(targetDir, this.getLibPath());
            if (version == null) return;
            
            String maxVersion = this.getMaxVersion(this.minRequiredVersion, version);
            
            if (maxVersion != this.minRequiredVersion)
            {
                this.version = version;
                this.isAvailable = valid;
            }
        }
    }
}
