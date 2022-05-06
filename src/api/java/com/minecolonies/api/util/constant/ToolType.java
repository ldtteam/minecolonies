package com.minecolonies.api.util.constant;

import com.minecolonies.api.util.constant.translation.ToolTranslationConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.HashMap;
import java.util.Map;

public enum ToolType implements IToolType
{
    /**
     * Note to future coders: You must add these to both: com.minecolonies.api.colony.requestsystem.requestable.Tool.getToolClasses and,
     * com.minecolonies.api.util.ItemStackUtils.isTool to be usable by the RS system
     */
    NONE("", false, new TranslatableComponent(ToolTranslationConstants.TOOL_TYPE_NONE)),
    PICKAXE("pickaxe", true, new TranslatableComponent(ToolTranslationConstants.TOOL_TYPE_PICKAXE)),
    SHOVEL("shovel", true, new TranslatableComponent(ToolTranslationConstants.TOOL_TYPE_SHOVEL)),
    AXE("axe", true, new TranslatableComponent(ToolTranslationConstants.TOOL_TYPE_AXE)),
    HOE("hoe", true, new TranslatableComponent(ToolTranslationConstants.TOOL_TYPE_HOE)),
    SWORD("weapon", true, new TranslatableComponent(ToolTranslationConstants.TOOL_TYPE_SWORD)),
    BOW("bow", false, new TranslatableComponent(ToolTranslationConstants.TOOL_TYPE_BOW)),
    FISHINGROD("rod", false, new TranslatableComponent(ToolTranslationConstants.TOOL_TYPE_FISHING_ROD)),
    SHEARS("shears", false, new TranslatableComponent(ToolTranslationConstants.TOOL_TYPE_SHEARS)),
    SHIELD("shield", false, new TranslatableComponent(ToolTranslationConstants.TOOL_TYPE_SHIELD)),
    HELMET("helmet", false, new TranslatableComponent(ToolTranslationConstants.TOOL_TYPE_HELMET)),
    LEGGINGS("leggings", false, new TranslatableComponent(ToolTranslationConstants.TOOL_TYPE_LEGGINGS)),
    CHESTPLATE("chestplate", false, new TranslatableComponent(ToolTranslationConstants.TOOL_TYPE_CHEST_PLATE)),
    BOOTS("boots", false, new TranslatableComponent(ToolTranslationConstants.TOOL_TYPE_BOOTS)),
    FLINT_N_STEEL("flintandsteel", false, new TranslatableComponent(ToolTranslationConstants.TOOL_TYPE_LIGHTER));

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

