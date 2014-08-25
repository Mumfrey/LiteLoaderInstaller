package cpw.mods.fml.installer;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;

import javax.swing.JComboBox;

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
        this("Minecraft Forge", "10.12.0.967");
    }
    
    protected ForgeModifier(String name, String minRequiredVersion)
    {
        this.name = name;
        this.minRequiredVersion = minRequiredVersion;
    }

    @Override
    public String getLabel()
    {
        return String.format("Chain to %s %s", this.name, this.version != null ? (this.hasMultipleVersions() ? "" : this.version) : "(Not detected)");
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
        return "net/minecraftforge/forge";
    }
    
    @Override
    public boolean isAvailable()
    {
        return this.isAvailable;
    }
    
    @Override
    public String getLatestVersion()
    {
        return this.version;
    }

    @Override
    protected void addLibraries(List<JsonNode> libraries)
    {
        JsonNode forgeNode = JsonNodeBuilders.anObjectBuilder()
            .withField("name", JsonNodeBuilders.aStringBuilder("net.minecraftforge:forge:" + this.versionPrefix + this.version))
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

        JsonNode lzmaNode = JsonNodeBuilders.anObjectBuilder()
                .withField("name", JsonNodeBuilders.aStringBuilder("lzma:lzma:0.0.1"))
                .build();
        libraries.add(lzmaNode);
        
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
    public void prepare(Object object)
    {
        if (object instanceof JComboBox)
        {
            Object selectedItem = ((JComboBox)object).getSelectedItem();
            if (selectedItem != null) this.version = selectedItem.toString();
        }
    }
    
    @Override
    public void refresh(boolean valid, File targetDir)
    {
        this.isAvailable = false;
        this.version = null;
        this.validVersions.clear();
        
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
    
    @Override
    protected boolean isValidLibrary(String name, String libVersion)
    {
        return true;
    }

    @Override
    protected String getMaxVersion(String maxVersion, String version)
    {
        Matcher maxVersionPatternMatcher = versionPattern.matcher(maxVersion);
        Matcher versionPatternMatcher = versionPattern.matcher(version);
        
        if (versionPatternMatcher.matches())
        {
            if (!maxVersionPatternMatcher.matches()) return version;
            
            for (int part = 1; part < 5; part++)
            {
                String winner = compare(maxVersion, version, maxVersionPatternMatcher.group(part), versionPatternMatcher.group(part));
                if (winner != null) return winner;
            }
        }
        
        return maxVersion;
    }
    
    protected String compare(String maxVersion, String version, String cmpMax, String cmpVer) throws NumberFormatException
    {
        int maxVersionNum    = Integer.parseInt(cmpMax);
        int versionNum       = Integer.parseInt(cmpVer);

        if (versionNum > maxVersionNum) return version;
        if (maxVersionNum > versionNum) return maxVersion;
        
        return null;
    }
}
