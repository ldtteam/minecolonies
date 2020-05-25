package com.minecolonies.coremod.util;

import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.LoadOnlyStructureHandler;
import com.minecolonies.coremod.colony.buildings.AbstractSchematicProvider;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Mirror;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class BuildingUtils
{
    /**
     * Private constructor to hide public one.
     */
    private BuildingUtils()
    {
        /**
         * Intentionally left empty.
         */
    }

    /**
     * Calculate the Size of the building given a world and a building.
     * @param world the world.
     * @param building the building.
     * @return the AxisAlignedBB box.
     */
    public static AxisAlignedBB getTargetAbleArea(final World world, final AbstractSchematicProvider building)
    {
        final BlockPos location = building.getPosition();
        final int x1;
        final int z1;
        final int x2;
        final int z2;
        final int y1 = location.getY() - 10;
        final int y2;

        if(building.getHeight() == 0)
        {
            final StructureName sn =
                    new StructureName(Structures.SCHEMATICS_PREFIX,
                            building.getStyle(),
                            building.getSchematicName() + building.getBuildingLevel());

            final String structureName = sn.toString();

            final LoadOnlyStructureHandler wrapper = new LoadOnlyStructureHandler(world, building.getID(), structureName, new PlacementSettings(), true);
            wrapper.getBluePrint().rotateWithMirror(BlockPosUtil.getRotationFromRotations(building.getRotation()), building.isMirrored() ? Mirror.FRONT_BACK : Mirror.NONE, world);

            final BlockPos zeroPos = location.subtract(wrapper.getBluePrint().getPrimaryBlockOffset());

            x1 = zeroPos.getX();
            z1 = zeroPos.getZ();
            x2 = zeroPos.getX() + wrapper.getBluePrint().getSizeX();
            z2 = zeroPos.getZ() + wrapper.getBluePrint().getSizeZ();
            y2 = location.getY() + wrapper.getBluePrint().getSizeY();

            building.setCorners(x1, x2, z1, z2);
            building.setHeight(wrapper.getBluePrint().getSizeY());
        }
        else
        {
            final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> corners = building.getCorners();
            x1 = corners.getA().getA();
            x2 = corners.getA().getB();
            z1 = corners.getB().getA();
            z2 = corners.getB().getB();
            y2 = location.getY() + building.getHeight();
        }

        return new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
    }

    /**
     * Get the hut from the inventory.
     * @param inventory the inventory to search.
     * @param hut the hut to fetch.
     * @return the stack or if not found empty.
     */
    public static ItemStack getItemStackForHutFromInventory(final PlayerInventory inventory, final String hut)
    {
        final int slot =  InventoryUtils.findFirstSlotInProviderNotEmptyWith(inventory.player,
          item -> item.getItem() instanceof BlockItem && ((BlockItem) item.getItem()).getBlock() instanceof AbstractBlockHut && ((BlockItem) item.getItem()).getBlock().getRegistryName().getPath().contains(hut));

        if (slot != -1)
        {
            return inventory.getStackInSlot(slot);
        }
        return ItemStack.EMPTY;
    }
}
