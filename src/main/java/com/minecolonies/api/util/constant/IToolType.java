package com.minecolonies.api.util.constant;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

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

    /**
     * Whether the item stack works as the tool.
     *
     * @param itemStack to test
     * @return Whether the item stack works as the tool.
     */
    Boolean checkIsTool(ItemStack itemStack);

    /**
     * The level the item stack is.
     *
     * @param itemStack to test
     * @return The level the item stack is.
     */
    int getMiningLevel(ItemStack itemStack);

    class IToolTypeComparator implements Comparator<IToolType> {
        public int compare(IToolType o1, IToolType o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }
}

