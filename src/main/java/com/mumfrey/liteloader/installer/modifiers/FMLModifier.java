package com.mumfrey.liteloader.installer.modifiers;


public class FMLModifier extends ForgeModifier
{
    public FMLModifier()
    {
        super("Forge ModLoader", "7.2.18.797");
    }

    @Override
    public String getTooltip()
    {
        return "Chains the LiteLoader tweaker to the Forge ModLoader tweaker";
    }

    @Override
    protected String getFailureMessage()
    {
        return "There was a problem chaining to Forge ModLoader, check the version JSON";
    }

    /**
     * @return
     */
    @Override
    protected String getLibPath()
    {
        return "cpw/mods/fml";
    }

    @Override
    protected String getLibName()
    {
        return "cpw.mods:fml";
    }

    @Override
    protected String getLibHint()
    {
        return "fml";
    }
}
