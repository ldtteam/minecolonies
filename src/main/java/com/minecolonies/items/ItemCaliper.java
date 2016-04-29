package com.minecolonies.items;

import com.minecolonies.util.LanguageHandler;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class ItemCaliper extends ItemMinecolonies
{
    private static final RangedAttribute ATTRIBUTE_CALIPER_USE = new RangedAttribute((IAttribute) null,"player.caliperUse", 0.0, 0.0, 1.0);
    private              BlockPos             startPosition;

    public ItemCaliper()
    {
        super();
        setMaxStackSize(1);
    }

    @Override
    public String getName()
    {
        return "caliper";
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        // if client world, do nothing
        if(worldIn.isRemote) return false;

        // if attribute instance is not known, register it.
        IAttributeInstance attribute = playerIn.getEntityAttribute(ATTRIBUTE_CALIPER_USE);
        if(attribute == null)
        {
            attribute = playerIn.getAttributeMap().registerAttribute(ATTRIBUTE_CALIPER_USE);
        }
        // if the value of the attribute is still 0, set the start values. (first point)
        if(attribute.getAttributeValue() == 0)
        {
            startPosition = pos;
            attribute.setBaseValue(1.0);
            return true;
        }
        attribute.setBaseValue(0.0);
        //Start == end, same location
        if(startPosition.getX() == pos.getX() && startPosition.getY() == pos.getY() && startPosition.getZ() == pos.getZ())
        {
            LanguageHandler.sendPlayerLocalizedMessage(playerIn, "item.caliper.message.same");
            return true;
        }

        //Create the box
        if(startPosition.getX() == pos.getX())
        {
            if(startPosition.getY() == pos.getY())
            {
                int distance = java.lang.Math.abs(pos.getZ() - startPosition.getZ()) + 1;
                LanguageHandler.sendPlayerLocalizedMessage(playerIn, "item.caliper.message.line", distance);
                return true;
            }
            if(startPosition.getZ() == pos.getZ())
            {
                int distance = java.lang.Math.abs(pos.getY() - startPosition.getY()) + 1;
                LanguageHandler.sendPlayerLocalizedMessage(playerIn, "item.caliper.message.line", distance);
                return true;
            }
            int distance1 = java.lang.Math.abs(pos.getY() - startPosition.getY()) + 1;
            int distance2 = java.lang.Math.abs(pos.getZ() - startPosition.getZ()) + 1;

            LanguageHandler.sendPlayerLocalizedMessage(playerIn, "item.caliper.message.square", distance1, distance2);
            return true;
        }
        if(startPosition.getY() == pos.getY())
        {
            if(startPosition.getZ() == pos.getZ())
            {
                int distance = java.lang.Math.abs(pos.getX() - startPosition.getX()) + 1;
                LanguageHandler.sendPlayerLocalizedMessage(playerIn, "item.caliper.message.line", distance);
                return true;
            }
            int distance1 = java.lang.Math.abs(pos.getX() - startPosition.getX()) + 1;
            int distance2 = java.lang.Math.abs(pos.getY() - startPosition.getZ()) + 1;

            LanguageHandler.sendPlayerLocalizedMessage(playerIn, "item.caliper.message.square", distance1, distance2);
            return true;
        }
        if(startPosition.getZ() == pos.getZ())
        {
            int distance1 = java.lang.Math.abs(pos.getX() - startPosition.getX()) + 1;
            int distance2 = java.lang.Math.abs(pos.getY() - startPosition.getY()) + 1;

            LanguageHandler.sendPlayerLocalizedMessage(playerIn, "item.caliper.message.square", distance1, distance2);
            return true;
        }

        int distance1 = java.lang.Math.abs(pos.getX() - startPosition.getX()) + 1;
        int distance2 = java.lang.Math.abs(pos.getY() - startPosition.getY()) + 1;
        int distance3 = java.lang.Math.abs(pos.getZ() - startPosition.getZ()) + 1;

        LanguageHandler.sendPlayerLocalizedMessage(playerIn, "item.caliper.message.cube", distance1, distance2, distance3);
        return true;
    }
}
