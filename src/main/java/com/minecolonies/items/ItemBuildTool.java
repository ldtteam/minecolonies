package com.minecolonies.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemBuildTool extends ItemMinecolonies
{
    private String name = "scepterGold";

    public ItemBuildTool()
    {
        super();
        setMaxStackSize(1);
    }

    @Override
    public String getName()
    {
        return name;
    }

    //TODO onItemUse?

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        //TODO
        return stack;
    }
}
