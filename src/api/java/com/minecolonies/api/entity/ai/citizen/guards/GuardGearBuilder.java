package com.minecolonies.api.entity.ai.citizen.guards;

import com.minecolonies.api.util.constant.ToolType;
import net.minecraft.inventory.EntityEquipmentSlot;
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
     * @param armorLevel the armor level.
     * @param levelRange the level range of the guard.
     * @param buildingLevelRange the building level range.
     * @return the list of items.
     */
    public static List<GuardGear> buildGearForLevel(final int armorLevel, final Tuple<Integer, Integer> levelRange, final Tuple<Integer, Integer> buildingLevelRange)
    {
        final List<GuardGear> armorList = new ArrayList<>();
        armorList.add(new GuardGear(ToolType.BOOTS, EntityEquipmentSlot.FEET,  armorLevel, levelRange, buildingLevelRange));
        armorList.add(new GuardGear(ToolType.CHESTPLATE, EntityEquipmentSlot.CHEST, armorLevel, levelRange, buildingLevelRange));
        armorList.add(new GuardGear(ToolType.HELMET, EntityEquipmentSlot.HEAD,  armorLevel, levelRange, buildingLevelRange));
        armorList.add(new GuardGear(ToolType.LEGGINGS, EntityEquipmentSlot.LEGS,  armorLevel, levelRange, buildingLevelRange));
        return armorList;
    }
}
