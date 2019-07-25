package com.minecolonies.api.entity.ai.citizen.guards;

import com.minecolonies.api.util.constant.ToolType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.Tuple;

import java.util.ArrayList;
import java.util.List;

public final class GuardGearBuilder
{
    /**
     * Private constructor to hide implicit one.
     */
    private GuardGearBuilder()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Build the gear for a certain armor level and level range.
     * @param minArmorLevel the min armor level.
     * @param maxArmorLevel the max armor level.
     * @param levelRange the level range of the guard.
     * @param buildingLevelRange the building level range.
     * @return the list of items.
     */
    public static List<GuardGear> buildGearForLevel(final int minArmorLevel, final int maxArmorLevel, final Tuple<Integer, Integer> levelRange, final Tuple<Integer, Integer> buildingLevelRange)
    {
        final List<GuardGear> armorList = new ArrayList<>();
        armorList.add(new GuardGear(ToolType.BOOTS, EquipmentSlotType.FEET,  minArmorLevel, maxArmorLevel, levelRange, buildingLevelRange));
        armorList.add(new GuardGear(ToolType.CHESTPLATE, EquipmentSlotType.CHEST, minArmorLevel, maxArmorLevel, levelRange, buildingLevelRange));
        armorList.add(new GuardGear(ToolType.HELMET, EquipmentSlotType.HEAD,  minArmorLevel, maxArmorLevel, levelRange, buildingLevelRange));
        armorList.add(new GuardGear(ToolType.LEGGINGS, EquipmentSlotType.LEGS,  minArmorLevel, maxArmorLevel, levelRange, buildingLevelRange));
        return armorList;
    }
}
