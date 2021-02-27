package com.minecolonies.coremod.compatibility.resourcefulbees;

import com.minecolonies.coremod.proxy.CommonProxy;
import com.resourcefulbees.resourcefulbees.tileentity.TieredBeehiveTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class ResourcefulBeesCompat
{
    public static Stack<ItemStack> getCombsFromHive(BlockPos pos, World world, int combs)
    {
        Stack<ItemStack> stack = new Stack();
        if (world.getTileEntity(pos) instanceof TieredBeehiveTileEntity) 
        { //If we have a resourceful bees hive, we care about the items inside
            TieredBeehiveTileEntity hive = (TieredBeehiveTileEntity) world.getTileEntity(pos);

            while(hive.hasCombs())
            { //Take out all combs from hive and return them to the beekeeper
                stack.push(hive.getResourceHoneycomb());
            }
        } 
        else 
        { //If it's actually not, behave normally.
            stack.push(new ItemStack(Items.HONEYCOMB, combs));
        }
        
        return stack;
    }
}
