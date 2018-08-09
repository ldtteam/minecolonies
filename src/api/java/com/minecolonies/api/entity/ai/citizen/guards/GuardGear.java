package com.minecolonies.api.entity.ai.citizen.guards;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.IToolType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;

import java.util.function.Predicate;

/**
 * Class to hold information about required item for the guard.
 */
public class GuardGear implements Predicate<ItemStack>
{
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
     * @param item               item that is being required.
     * @param type               item type for the required item.
     * @param citizenLevelRange  level range required to demand item.
     * @param buildingLevelRange level range that the item will be required.
     */
    public GuardGear(
      final IToolType item, final EntityEquipmentSlot type,
      final int armorLevel,
      final Tuple<Integer, Integer> citizenLevelRange,
      final Tuple<Integer, Integer> buildingLevelRange)
    {
        this.type = type;
        this.itemNeeded = item;
        this.minLevelRequired = citizenLevelRange.getFirst();
        this.maxLevelRequired = citizenLevelRange.getSecond();
        this.armorLevel = armorLevel;
        this.minBuildingLevelRequired = buildingLevelRange.getFirst();
        this.maxBuildingLevelRequired = buildingLevelRange.getSecond();
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

    @Override
    public boolean test(final ItemStack stack)
    {
        return (ItemStackUtils.hasToolLevel(stack, itemNeeded, armorLevel, armorLevel) && stack.getItem() instanceof ItemArmor && ((ItemArmor) stack.getItem()).armorType == getType()) || (stack.getItem() instanceof ItemShield && getType() == EntityEquipmentSlot.MAINHAND);
    }
}
