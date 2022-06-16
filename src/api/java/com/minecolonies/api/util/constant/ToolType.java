package com.minecolonies.api.util.constant;

import com.minecolonies.api.util.constant.translation.ToolTranslationConstants;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;

public enum ToolType implements IToolType
{
    /**
     * Note to future coders: You must add these to both: com.minecolonies.api.colony.requestsystem.requestable.Tool.getToolClasses and,
     * com.minecolonies.api.util.ItemStackUtils.isTool to be usable by the RS system
     */
    NONE("", false, Component.translatable(ToolTranslationConstants.TOOL_TYPE_NONE)),
    PICKAXE("pickaxe", true, Component.translatable(ToolTranslationConstants.TOOL_TYPE_PICKAXE)),
    SHOVEL("shovel", true, Component.translatable(ToolTranslationConstants.TOOL_TYPE_SHOVEL)),
    AXE("axe", true, Component.translatable(ToolTranslationConstants.TOOL_TYPE_AXE)),
    HOE("hoe", true, Component.translatable(ToolTranslationConstants.TOOL_TYPE_HOE)),
    SWORD("weapon", true, Component.translatable(ToolTranslationConstants.TOOL_TYPE_SWORD)),
    BOW("bow", false, Component.translatable(ToolTranslationConstants.TOOL_TYPE_BOW)),
    FISHINGROD("rod", false, Component.translatable(ToolTranslationConstants.TOOL_TYPE_FISHING_ROD)),
    SHEARS("shears", false, Component.translatable(ToolTranslationConstants.TOOL_TYPE_SHEARS)),
    SHIELD("shield", false, Component.translatable(ToolTranslationConstants.TOOL_TYPE_SHIELD)),
    HELMET("helmet", false, Component.translatable(ToolTranslationConstants.TOOL_TYPE_HELMET)),
    LEGGINGS("leggings", false, Component.translatable(ToolTranslationConstants.TOOL_TYPE_LEGGINGS)),
    CHESTPLATE("chestplate", false, Component.translatable(ToolTranslationConstants.TOOL_TYPE_CHEST_PLATE)),
    BOOTS("boots", false, Component.translatable(ToolTranslationConstants.TOOL_TYPE_BOOTS)),
    FLINT_N_STEEL("flintandsteel", false, Component.translatable(ToolTranslationConstants.TOOL_TYPE_LIGHTER));

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

