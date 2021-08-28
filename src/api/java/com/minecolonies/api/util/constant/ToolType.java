package com.minecolonies.api.util.constant;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.HashMap;
import java.util.Map;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

public enum ToolType implements IToolType
{
    /**
     * Note to future coders: You must add these to both: com.minecolonies.api.colony.requestsystem.requestable.Tool.getToolClasses and,
     * com.minecolonies.api.util.ItemStackUtils.isTool to be usable by the RS system
     */
    NONE("", false, new TranslatableComponent(COM_MINECOLONIES_TOOLTYPE_NONE)),
    PICKAXE("pickaxe", true, new TranslatableComponent(COM_MINECOLONIES_TOOLTYPE_PICKAXE)),
    SHOVEL("shovel", true, new TranslatableComponent(COM_MINECOLONIES_TOOLTYPE_SHOVEL)),
    AXE("axe", true, new TranslatableComponent(COM_MINECOLONIES_TOOLTYPE_AXE)),
    HOE("hoe", true, new TranslatableComponent(COM_MINECOLONIES_TOOLTYPE_HOE)),
    SWORD("weapon", true, new TranslatableComponent(COM_MINECOLONIES_TOOLTYPE_SWORD)),
    BOW("bow", false, new TranslatableComponent(COM_MINECOLONIES_TOOLTYPE_BOW)),
    FISHINGROD("rod", false, new TranslatableComponent(COM_MINECOLONIES_TOOLTYPE_FISHINGROD)),
    SHEARS("shears", false, new TranslatableComponent(COM_MINECOLONIES_TOOLTYPE_SHEARS)),
    SHIELD("shield", false, new TranslatableComponent(COM_MINECOLONIES_TOOLTYPE_SHIELD)),
    HELMET("helmet", false, new TranslatableComponent(COM_MINECOLONIES_TOOLTYPE_HELMET)),
    LEGGINGS("leggings", false, new TranslatableComponent(COM_MINECOLONIES_TOOLTYPE_LEGGINGS)),
    CHESTPLATE("chestplate", false, new TranslatableComponent(COM_MINECOLONIES_TOOLTYPE_CHESTPLATE)),
    BOOTS("boots", false, new TranslatableComponent(COM_MINECOLONIES_TOOLTYPE_BOOTS)),
    FLINT_N_STEEL("flintandsteel", false, new TranslatableComponent(COM_MINECOLONIES_TOOLTYPE_LIGHTER));

    static final private Map<String, IToolType> tools = new HashMap<>();
    static
    {
        for (final ToolType type : values())
        {
            tools.put(type.getName(), type);
        }
    }
    private final String         name;
    private final boolean        variableMaterials;
    private final Component displayName;

    private ToolType(final String name, final boolean variableMaterials, final Component displayName)
    {
        this.name = name;
        this.variableMaterials = variableMaterials;
        this.displayName = displayName;
    }

    public static IToolType getToolType(final String tool)
    {
        if (tools.containsKey(tool))
        {
            return tools.get(tool);
        }
        return NONE;
    }

    public String getName()
    {
        return this.name;
    }

    public boolean hasVariableMaterials()
    {
        return variableMaterials;
    }

    @Override
    public Component getDisplayName()
    {
        return displayName;
    }
}

