package cpw.mods.fml.installer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import argo.jdom.JsonField;
import argo.jdom.JsonNode;
import argo.jdom.JsonNodeFactories;
import argo.jdom.JsonRootNode;
import argo.jdom.JsonStringNode;

public abstract class CascadeModifier implements ActionModifier
{
    protected static Pattern versionPattern = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)$");
    
    private static String tweaks = null;
    
    private static String originalArgs = null;
    
    private static Set<String> addedLibraries = new HashSet<String>();
    
    private static Set<String> addedTweaks = new HashSet<String>();
    
    @Override
    public JsonRootNode modifyVersion(JsonRootNode versionJson)
    {
        String tweakClass = this.getTweakClass();
        if (tweakClass != null && !addedTweaks.contains(tweakClass))
        {
            addedTweaks.add(tweakClass);
            tweaks = tweaks == null ? tweakClass : String.format("%s,%s", tweaks, tweakClass);
        }
        
        try
        {
            List<JsonField> copyFields = new ArrayList<JsonField>();
            
            for (JsonField field : versionJson.getFieldList())
            {
                JsonStringNode fieldName = field.getName();
                
                if ("libraries".equals(fieldName.getText()))
                {
                    List<JsonNode> libraries = new ArrayList<JsonNode>();

                    List<JsonNode> librariesToAdd = new ArrayList<JsonNode>();
                    this.addLibraries(librariesToAdd);
                    
                    for (JsonNode library : librariesToAdd)
                    {
                        String libraryName = library.getStringValue("name");
                        if (!addedLibraries.contains(libraryName))
                        {
                            addedLibraries.add(libraryName);
                            libraries.add(library);
                        }
                    }                    

                    libraries.addAll(field.getValue().getArrayNode());
                    copyFields.add(new JsonField(fieldName, JsonNodeFactories.array(libraries)));
                }
                else if ("minecraftArguments".equals(fieldName.getText()))
                {
                    if (originalArgs == null) originalArgs = field.getValue().getText();
                    copyFields.add(new JsonField(fieldName, JsonNodeFactories.string(originalArgs + " --cascadedTweaks " + tweaks)));
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
            JOptionPane.showMessageDialog(null, this.getFailureMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return versionJson;
    }

    protected abstract String getFailureMessage();

    protected abstract String getTweakClass();

    protected abstract void addLibraries(List<JsonNode> libraries);

    /**
     * @param targetDir
     * @param libPath
     * @return
     */
    protected String getLatestLibraryVersion(File targetDir, String libPath)
    {
        File libraries = new File(targetDir, "libraries");
        
        if (libraries.exists() && libraries.isDirectory())
        {
            File libFolder = new File(libraries, libPath);
            
            if (libFolder.exists() && libFolder.isDirectory())
            {
                String maxVersion = "-";
                
                for (File versionFolder : libFolder.listFiles())
                {
                    if (versionFolder.isDirectory())
                    {
                        maxVersion = getMaxVersion(maxVersion, versionFolder.getName());
                    }
                }
                
                if (!maxVersion.equals("-"))
                {
                    return maxVersion;
                }
            }
        }
        
        return null;
    }

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