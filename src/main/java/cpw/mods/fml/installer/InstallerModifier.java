package cpw.mods.fml.installer;

import java.io.File;

import javax.swing.Icon;

import com.google.common.base.Throwables;
import com.google.common.reflect.Reflection;

public enum InstallerModifier {
    FORGE(ForgeModifier.class);

    private String label;
    private String tooltip;
    private ActionModifier modifier;

    private InstallerModifier(Class<? extends ActionModifier> modifierType)
    {
        try
        {
            this.modifier = modifierType.newInstance();
            this.label = this.modifier.getLabel();
            this.tooltip = this.modifier.getTooltip();
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

    public ActionModifier getModifier()
    {
        return modifier;
    }
}
