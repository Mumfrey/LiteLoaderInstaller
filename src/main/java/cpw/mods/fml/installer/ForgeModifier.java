package cpw.mods.fml.installer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import argo.jdom.JsonField;
import argo.jdom.JsonNode;
import argo.jdom.JsonNodeBuilders;
import argo.jdom.JsonNodeFactories;
import argo.jdom.JsonRootNode;
import argo.jdom.JsonStringNode;

public class ForgeModifier implements ActionModifier
{
    private static Pattern versionPattern = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)$");
    
    private String forgeVersion = "9.10.0.794";
    
    private boolean isAvailable;

    @Override
    public String getLabel()
    {
        return "Chain to Minecraft Forge " + forgeVersion;
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
            .withField("name", JsonNodeBuilders.aStringBuilder("net.minecraftforge:minecraftforge:" + forgeVersion))
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
//        this.isAvailable = false;
        this.isAvailable = valid;
        
        if (valid)
        {
            File libraries = new File(targetDir, "libraries");
            
            if (libraries.exists() && libraries.isDirectory())
            {
                File minecraftForge = new File(libraries, "net/minecraftforge/minecraftforge");
                
                if (minecraftForge.exists() && minecraftForge.isDirectory())
                {
                    String maxVersion = "-";
                    
                    for (File versionFolder : minecraftForge.listFiles())
                    {
                        if (versionFolder.isDirectory())
                        {
                            maxVersion = getMaxVersion(maxVersion, versionFolder.getName());
                        }
                    }
                    
                    if (!maxVersion.equals("-"))
                    {
                        this.forgeVersion = maxVersion;
//                        this.isAvailable = true;
                    }
                }
            }
        }
    }
    
    private String getMaxVersion(String maxVersion, String version)
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

    @Override
    public boolean isAvailable()
    {
        return this.isAvailable;
    }
}
