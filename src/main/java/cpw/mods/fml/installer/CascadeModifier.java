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
    
    private static Set<String> addedLibraries = new HashSet<String>();
    
    private static Set<String> tweaks = new HashSet<String>();
    
    protected String versionPrefix = VersionInfo.getMinecraftVersion() + "-";
    
    protected List<String> validVersions = new ArrayList<String>();
    
    @Override
    public JsonRootNode modifyVersion(JsonRootNode versionJson)
    {
        String tweakClass = this.getTweakClass();
        
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
                else if ("minecraftArguments".equals(fieldName.getText()) && tweakClass != null && !tweaks.contains(tweakClass))
                {
                    copyFields.add(new JsonField(fieldName, JsonNodeFactories.string(field.getValue().getText() + " --tweakClass " + tweakClass)));
                    tweaks.add(tweakClass);
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
    
    @Override
    public void modifyFields(List<JsonField> fields)
    {
    }

    protected abstract String getFailureMessage();

    protected abstract String getTweakClass();

    protected abstract void addLibraries(List<JsonNode> libraries);
    
    public boolean hasMultipleVersions()
    {
        return this.validVersions.size() > 1;        
    }
    
    public List<String> getValidVersions()
    {
        return this.validVersions;
    }
    
    public List<String> getOtherVersions()
    {
        List<String> validVersions = new ArrayList<String>(this.validVersions);
        validVersions.remove(this.getLatestVersion());
        return validVersions;
    }
    
    public abstract String getLatestVersion();
    
    /**
     * @param targetDir
     * @param libPath
     * @return
     */
    protected String getLatestLibraryVersion(File targetDir, String libPath)
    {
        this.validVersions.clear();
        
        File libraries = new File(targetDir, "libraries");
        
        if (libraries.exists() && libraries.isDirectory())
        {
            File libFolder = new File(libraries, libPath);
            
            if (libFolder.exists() && libFolder.isDirectory())
            {
                String maxVersion = "-";
                
                for (File versionFolder : libFolder.listFiles())
                {
                    if (versionFolder.isDirectory() && versionFolder.getName().startsWith(this.versionPrefix))
                    {
                        String libVersion = versionFolder.getName().substring(this.versionPrefix.length());
                        if (this.isValidLibrary(versionFolder.getName(), libVersion))
                        {
                            this.validVersions.add(libVersion);
                            maxVersion = this.getMaxVersion(maxVersion, libVersion);
                        }
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

    protected abstract boolean isValidLibrary(String name, String libVersion);

    protected abstract String getMaxVersion(String maxVersion, String version);
}