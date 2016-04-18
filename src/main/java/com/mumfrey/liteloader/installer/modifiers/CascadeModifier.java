package com.mumfrey.liteloader.installer.modifiers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import com.mumfrey.liteloader.installer.VersionInfo;

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

    protected Map<String, List<JsonNode>> validVersionLibraries = new HashMap<String, List<JsonNode>>();

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
    
    @Override
    public void modifyJvmArgs(List<InstallationModifier> modifiers, Set<String> jvmArgs)
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
    protected String getLatestLibraryVersion(File targetDir, String libPath, String libName, Map<String, List<JsonNode>> versionLibraries)
    {
        this.validVersions.clear();
        this.validVersionLibraries.clear();

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
                            this.validVersionLibraries.put(libVersion, this.findVersionsWithLibrary(libName, libVersion, versionLibraries));

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

    private List<JsonNode> findVersionsWithLibrary(String libName, String libVersion, Map<String, List<JsonNode>> versionLibraries)
    {
        List<JsonNode> chosenLibs = new ArrayList<JsonNode>();
        int chosenLibConfidence = 0;

        for (Entry<String, List<JsonNode>> versionLibrary : versionLibraries.entrySet())
        {
            String versionName = versionLibrary.getKey().toLowerCase();
            List<JsonNode> libs = versionLibrary.getValue();

            for (JsonNode lib : libs)
            {
                try
                {
                    int confidence = 0;
                    if (!versionName.toLowerCase().contains("lite"))
                    {
                        String name = lib.getStringValue("name");
                        if (name.equals(libName + ":" + libVersion) || name.equals(libName + ":" + this.versionPrefix + libVersion))
                            confidence++;

                        if (versionName.contains(this.getLibHint()))
                            confidence++;
                        if (versionName.contains(this.versionPrefix))
                            confidence++;

                        if (confidence >= chosenLibConfidence)
                        {
                            chosenLibConfidence = confidence;
                            chosenLibs = libs;
                            // System.err.println(this.getClass().getSimpleName()
                            // + " is choosing " + versionName +
                            // " with confidence " + confidence);
                        }
                    }
                }
                catch (Exception ex)
                {
                }
            }
        }

        return chosenLibs;
    }

    protected abstract boolean isValidLibrary(String name, String libVersion);

    protected abstract String getMaxVersion(String maxVersion, String version);

    protected abstract String getLibPath();

    protected abstract String getLibName();

    protected abstract String getLibHint();
}