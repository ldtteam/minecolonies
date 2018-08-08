package com.minecolonies.api.entity.ai.citizen.guards;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.IToolType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

/**
 * Class to hold information about required item for the guard.
 */
public class GuardItems
{
    /**
     * Quantity required on the required.
     */
    private final int quantity;

    /**
     * Min level the citizen has to be to required the item.
     */
    private final int minLevelRequired;

    /**
     * Max level the citizen can be to required the item.
     */
    private final int maxLevelRequired;

    /**
     * The armor level.
     */
    private final int armorLevel;

    /**
     * Minimal building level.
     */
    private final int minBuildingLevelRequired;

    /**
     * Maximum building level.
     */
    private final int maxBuildingLevelRequired;


    /**
     * Item type that is required.
     */
    private final EntityEquipmentSlot type;

    /**
     * Tool type that is needed.
     */
    private final IToolType itemNeeded;

    /**
     * Create a classification for a tool level.
     *
     * @param item        item that is being required.
     * @param type        item type for the required item.
     * @param quantity    quantity required for the item.
     * @param min         min level required to demand item.
     * @param max         max level that the item will be required.
     * @param minBuilding the minimum building level required.
     * @param maxBuilding the maximum building level for this.
     */
    public GuardItems(
      final IToolType item, final EntityEquipmentSlot type,
      final int armorLevel,
      final int quantity,
      final int min, final int max,
      final int minBuilding, final int maxBuilding)
    {
        this.type = type;
        this.itemNeeded = item;
        this.minLevelRequired = min;
        this.maxLevelRequired = max;
        this.quantity = quantity;
        this.armorLevel = armorLevel;
        this.minBuildingLevelRequired = minBuilding;
        this.maxBuildingLevelRequired = maxBuilding;
    }

    /**
     * @return min level for this item to be required
     */
    public int getMinLevelRequired()
    {
        return minLevelRequired;
    }

    /**
     * @return max level for this item to be require
     */
    public int getMaxLevelRequired()
    {
        return maxLevelRequired;
    }

    /**
     * @return type of the item
     */
    public EntityEquipmentSlot getType()
    {
        return type;
    }

    /**
     * @return number of items required.
     */
    public int getQuantity()
    {
        return quantity;
    }

    /**
     * @return minimal level required for this tool.
     */
    public int getArmorLevel()
    {
        return armorLevel;
    }

    /**
     * @return return the tool type that is needed
     */
    public IToolType getItemNeeded()
    {
        return itemNeeded;
    }

    /**
     * @return the min building level for this armor.
     */
    public int getMinBuildingLevelRequired()
    {
        return minBuildingLevelRequired;
    }

    /**
     * @return the max building level for this armor.
     */
    public int getMaxBuildingLevelRequired()
    {
        return maxBuildingLevelRequired;
    }

    /**
     * Check if it matches with an itemStack.
     * @param stack the stack to check.
     * @return true if so.
     */
    public boolean doesMatchItemStack(final ItemStack stack)
    {
        return ItemStackUtils.hasToolLevel(stack, itemNeeded, armorLevel, armorLevel);
    }
}
