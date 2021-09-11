package com.minecolonies.api.compatibility.resourcefulbees;

import cy.jdkdigital.productivebees.common.tileentity.AdvancedBeehiveTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ProductiveBeesCompat implements IBeehiveCompat
{
    @Override
    public List<ItemStack> getCombsFromHive(BlockPos pos, World world, int amount)
    {
        List<ItemStack> list = new ArrayList<>();
        if (world.getBlockEntity(pos) instanceof AdvancedBeehiveTileEntity)
        {
            //If we have a productive bees hive, we care about the items inside
            AdvancedBeehiveTileEntity hive = (AdvancedBeehiveTileEntity) world.getBlockEntity(pos);

            hive.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(inv -> {
                list = getItemHandlerAsList(inv);
            });

            return list;
        }
        else
        {
            return IBeehiveCompat.super.getCombsFromHive(pos, world, amount);
        }
    }
}
