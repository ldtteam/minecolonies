package com.minecolonies.api.compatibility.resourcefulbees;

import com.resourcefulbees.resourcefulbees.tileentity.TieredBeehiveTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ResourcefulBeesCompat implements IBeehiveCompat
{
    @Override
    public List<ItemStack> getCombsFromHive(BlockPos pos, World world, int amount)
    {
        List<ItemStack> list = new ArrayList<>();
        if (world.getBlockEntity(pos) instanceof TieredBeehiveTileEntity)
        {
            //If we have a resourceful bees hive, we care about the items inside
            TieredBeehiveTileEntity hive = (TieredBeehiveTileEntity) world.getBlockEntity(pos);

            while (hive.hasCombs())
            {
                //Take out all combs from hive and return them to the beekeeper
                list.add(hive.getResourceHoneycomb());
            }

            return list;
        }
        else
        {
            return IBeehiveCompat.super.getCombsFromHive(pos, world, amount);
        }
    }
}
