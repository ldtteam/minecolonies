package com.minecolonies.api.entity.ai.citizen.farmer;

import net.minecraftforge.items.ItemStackHandler;

/**
 * ------------ Class not Documented ------------
 */
public interface IScarecrow
{
    /**
     * Getter of the name of the tileEntity.
     *
     * @return the string.
     */
    String getDesc();

    /**
     * Setter for the name.
     *
     * @param name string to set.
     */
    void setName(String name);

    /**
     * Returns the type of the scarecrow (Important for the rendering).
     *
     * @return the enum type.
     */
    ScareCrowType getType();

    /**
     * Get the inventory connected with the scarecrow.
     *
     * @return the inventory field of this scarecrow
     */
    ItemStackHandler getInventoryField();

    /**
     * Set the inventory connected with the scarecrow.
     *
     * @param inventoryField the field to set it to
     */
    void setInventoryField(ItemStackHandler inventoryField);
}
