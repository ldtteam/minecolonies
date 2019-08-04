package com.minecolonies.api.entity.ai.citizen.guards;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.IToolType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ShieldItem;
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
     * The min armor level.
     */
    private final int minArmorLevel;

    /**
     * The max armor level.
     */
    private final int maxArmorLevel;

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
    private final EquipmentSlotType type;

    /**
     * Tool type that is needed.
     */
    private final IToolType itemNeeded;

    /**
     * Create a classification for a tool level.
     *
     * @param item               item that is being required.
     * @param type               item type for the required item.
     * @param minArmorLevel      the min armor level.
     * @param maxArmorLevel      the max armor level.
     * @param citizenLevelRange  level range required to demand item.
     * @param buildingLevelRange level range that the item will be required.
     */
    public GuardGear(
      final IToolType item, final EquipmentSlotType type,
      final int minArmorLevel,
      final int maxArmorLevel, final Tuple<Integer, Integer> citizenLevelRange,
      final Tuple<Integer, Integer> buildingLevelRange)
    {
        this.type = type;
        this.itemNeeded = item;
        this.minLevelRequired = citizenLevelRange.getA();
        this.maxLevelRequired = citizenLevelRange.getB();
        this.minArmorLevel = minArmorLevel;
        this.maxArmorLevel = maxArmorLevel;
        this.minBuildingLevelRequired = buildingLevelRange.getA();
        this.maxBuildingLevelRequired = buildingLevelRange.getB();
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
    public EquipmentSlotType getType()
    {
        return type;
    }

    /**
     * @return minimal level required for this tool.
     */
    public int getMinArmorLevel()
    {
        return minArmorLevel;
    }

    /**
     * @return maximal level required for this tool.
     */
    public int getMaxArmorLevel()
    {
        return maxArmorLevel;
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
        return
          (ItemStackUtils.hasToolLevel(stack, itemNeeded, minArmorLevel, maxArmorLevel) && stack.getItem() instanceof ArmorItem && ((ArmorItem) stack.getItem()).getEquipmentSlot() == getType())
            || (stack.getItem() instanceof ShieldItem && getType() == EquipmentSlotType.MAINHAND);
    }
}
