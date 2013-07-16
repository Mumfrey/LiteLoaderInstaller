package cpw.mods.fml.installer;

import java.io.File;
import java.util.List;

import javax.swing.Icon;

import com.google.common.base.Throwables;
import com.google.common.reflect.Reflection;

public enum InstallerAction {
    CLIENT("Install LiteLoader", "Install a new profile to the Mojang client launcher", ClientInstall.class, true),
//    SERVER("Install server", "Create a new modded server installation", ServerInstall.class, true),
    EXTRACT("Extract LiteLoader", "Extract the contained jar file", ExtractAction.class, false);

    private String label;
    private String tooltip;
    private ActionType action;
    private boolean allowModifiers;

    private InstallerAction(String label, String tooltip, Class<? extends ActionType> action, boolean allowModifiers)
    {
        this.label = label;
        this.tooltip = tooltip;
        this.allowModifiers = allowModifiers;
        try
        {
            this.action = action.newInstance();
        }
        catch (Exception e)
        {
            throw Throwables.propagate(e);
        }
    }
    public String getButtonLabel()
    {
        return label;
    }

    public String getTooltip()
    {
        return tooltip;
    }
    
    public boolean allowsModifiers()
    {
        return this.allowModifiers;
    }

    public boolean run(File path, List<ActionModifier> modifiers)
    {
        return action.run(path, modifiers);
    }
    public boolean isPathValid(File targetDir)
    {
        return action.isPathValid(targetDir);
    }

    public String getFileError(File targetDir)
    {
        return action.getFileError(targetDir);
    }
    public String getSuccessMessage()
    {
        return action.getSuccessMessage();
    }
}
