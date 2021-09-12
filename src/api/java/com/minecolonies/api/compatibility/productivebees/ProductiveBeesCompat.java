package com.minecolonies.api.compatibility.productivebees;

import cy.jdkdigital.productivebees.common.tileentity.AdvancedBeehiveTileEntity;
import com.minecolonies.api.compatibility.resourcefulbees.IBeehiveCompat;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;

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
            //Using shears on a Productive Bees hive also provides vanilla honeycomb
            list.add(new ItemStack(Items.HONEYCOMB, amount));

            //If we have a Productive Bees hive, we care about the items inside
            AdvancedBeehiveTileEntity hive = (AdvancedBeehiveTileEntity) world.getBlockEntity(pos);

            //the actual IItemHandlerModifiable is protected, but this is the public method that
            //Productive Bees uses to access the contents
            hive.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent( inv -> {
                for (int i = 0; i < inv.getSlots(); i++) {
                    ItemStack itemstack = inv.getStackInSlot(i);
                    list.add(inv.extractItem(i, itemstack.getCount(), false));
                }
            });

            return list;
        }
        else
        {
            return IBeehiveCompat.super.getCombsFromHive(pos, world, amount);
        }
    }
}
