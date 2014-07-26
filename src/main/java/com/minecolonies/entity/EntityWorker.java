package com.minecolonies.entity;

import com.minecolonies.entity.ai.EntityAIGoHome;
import com.minecolonies.entity.ai.EntityAISleep;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

public abstract class EntityWorker extends EntityCitizen
{
    private List<ItemStack> itemsNeeded = new ArrayList<ItemStack>();

    public EntityWorker(World world)
    {
        super(world);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound)
    {
        super.writeEntityToNBT(compound);
        if(!itemsNeeded.isEmpty())
        {
            NBTTagList itemsNeededTag = new NBTTagList();
            for(ItemStack itemstack : itemsNeeded)
            {
                NBTTagCompound itemCompound = new NBTTagCompound();
                itemstack.writeToNBT(itemCompound);
                itemsNeededTag.appendTag(itemCompound);
            }
            compound.setTag("itemsNeeded", itemsNeededTag);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);
        NBTTagList itemsNeededTag = compound.getTagList("itemsNeeded", Constants.NBT.TAG_COMPOUND);
        for(int i = 0; i < itemsNeededTag.tagCount(); i++)
        {
            NBTTagCompound itemCompound = itemsNeededTag.getCompoundTagAt(i);
            itemsNeeded.add(ItemStack.loadItemStackFromNBT(itemCompound));
        }
    }

    public abstract boolean isNeeded();

    public boolean hasItemsNeeded()
    {
        return itemsNeeded.isEmpty();
    }

    public List<ItemStack> getItemsNeeded()
    {
        return itemsNeeded;
    }

    public void addItemNeeded(ItemStack itemstack)
    {
        boolean isAlreadyNeeded = false;
        for(ItemStack neededItem : itemsNeeded)
        {
            if(itemstack.isItemEqual(neededItem))
            {
                for(int i = 0; i < itemstack.stackSize; i++)
                {
                    neededItem.stackSize++;
                }
                isAlreadyNeeded = true;
            }
        }
        if(!isAlreadyNeeded)
        {
            itemsNeeded.add(itemstack);
        }
    }

    public ItemStack removeItemNeeded(ItemStack itemstack)
    {
        ItemStack itemCopy = itemstack.copy();
        for(ItemStack neededItem : itemsNeeded)
        {
            if(itemCopy.isItemEqual(neededItem))
            {
                for(int i = 0; i < itemCopy.stackSize; i++)
                {
                    itemCopy.stackSize--;
                    neededItem.stackSize--;
                    if(neededItem.stackSize == 0)
                    {
                        itemsNeeded.remove(itemsNeeded.indexOf(neededItem));
                        break;
                    }
                }
            }
        }
        return itemCopy.stackSize == 0 ? null : itemstack;
    }
}
