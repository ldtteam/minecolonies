package com.minecolonies.api.util.constant;

import net.minecraft.network.chat.Component;

public interface IToolType
{
    /**
     * Returns the name of the tooltype. Also known as the ToolClass.
     *
     * @return The name of the tool type.
     */
    String getName();

    /**
     * Whether or not the tool use material.
     * <p>
     * such as wood, gold, stone, iron or diamond
     *
     * @return true if using material
     */
    boolean hasVariableMaterials();

    /**
     * Text displayed to the user.
     *
     * @return The text displayed to the user.
     */
    Component getDisplayName();
}

