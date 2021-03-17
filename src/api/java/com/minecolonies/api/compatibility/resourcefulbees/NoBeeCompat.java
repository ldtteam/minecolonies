package com.minecolonies.api.compatibility.resourcefulbees;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class NoBeeCompat implements IBeehiveCompat {

    public List<ItemStack> getCombsFromHive(BlockPos pos, World world, int amount)
    {
        List<ItemStack> list = new ArrayList<>();
        list.add(new ItemStack(Items.HONEYCOMB, amount));
        return list;
    }
}
