package cpw.mods.fml.installer;

import java.io.File;

import com.google.common.base.Throwables;

public enum InstallerModifier {
    FORGE(ForgeModifier.class);

    private ActionModifier modifier;

    private InstallerModifier(Class<? extends ActionModifier> modifierType)
    {
        try
        {
            this.modifier = modifierType.newInstance();
        }
        catch (Exception e)
        {
            throw Throwables.propagate(e);
        }
    }
    public String getButtonLabel()
    {
        return this.modifier.getLabel();
    }

    public String getTooltip()
    {
        return this.modifier.getTooltip();
    }

    public ActionModifier getModifier()
    {
        return modifier;
    }
    
    public void refresh(boolean valid, File targetDir)
    {
    	this.modifier.refresh(valid, targetDir);
    }
    
    boolean isAvailable()
    {
    	return this.modifier.isAvailable();
    }
}
