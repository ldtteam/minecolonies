package com.minecolonies.items;

import com.minecolonies.util.LanguageHandler;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemCaliper extends ItemMinecolonies
{
    private static final RangedAttribute ATTRIBUTE_CALIPER_USE = new RangedAttribute("player.caliperUse", 0.0, 0.0, 1.0);
    private              int             startPositionX;
    private              int             startPositionY;
    private              int             startPositionZ;

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
    public boolean onItemUse(ItemStack itemstack, EntityPlayer entityPlayer, World world, int x, int y, int z, int face, float px, float py, float pz)
    {
        // if client world, do nothing
        if(world.isRemote) return false;

        // if attribute instance is not known, register it.
        IAttributeInstance attribute = entityPlayer.getEntityAttribute(ATTRIBUTE_CALIPER_USE);
        if(attribute == null)
        {
            attribute = entityPlayer.getAttributeMap().registerAttribute(ATTRIBUTE_CALIPER_USE);
        }
        // if the value of the attribute is still 0, set the start values. (first point)
        if(attribute.getAttributeValue() == 0)
        {
            startPositionX = x;
            startPositionY = y;
            startPositionZ = z;
            attribute.setBaseValue(1.0);
            return true;
        }
        attribute.setBaseValue(0.0);
        //Start == end, same location
        if(startPositionX == x && startPositionY == y && startPositionZ == z)
        {
            LanguageHandler.sendPlayerLocalizedMessage(entityPlayer, "item.caliper.message.same");
            return true;
        }

        //Create the box
        if(startPositionX == x)
        {
            if(startPositionY == y)
            {
                int distance = java.lang.Math.abs(z - startPositionZ) + 1;
                LanguageHandler.sendPlayerLocalizedMessage(entityPlayer, "item.caliper.message.line", distance);
                return true;
            }
            if(startPositionZ == z)
            {
                int distance = java.lang.Math.abs(y - startPositionY) + 1;
                LanguageHandler.sendPlayerLocalizedMessage(entityPlayer, "item.caliper.message.line", distance);
                return true;
            }
            int distance1 = java.lang.Math.abs(y - startPositionY) + 1;
            int distance2 = java.lang.Math.abs(z - startPositionZ) + 1;

            LanguageHandler.sendPlayerLocalizedMessage(entityPlayer, "item.caliper.message.square", distance1, distance2);
            return true;
        }
        if(startPositionY == y)
        {
            if(startPositionZ == z)
            {
                int distance = java.lang.Math.abs(x - startPositionX) + 1;
                LanguageHandler.sendPlayerLocalizedMessage(entityPlayer, "item.caliper.message.line", distance);
                return true;
            }
            int distance1 = java.lang.Math.abs(x - startPositionX) + 1;
            int distance2 = java.lang.Math.abs(z - startPositionZ) + 1;

            LanguageHandler.sendPlayerLocalizedMessage(entityPlayer, "item.caliper.message.square", distance1, distance2);
            return true;
        }
        if(startPositionZ == z)
        {
            int distance1 = java.lang.Math.abs(x - startPositionX) + 1;
            int distance2 = java.lang.Math.abs(y - startPositionY) + 1;

            LanguageHandler.sendPlayerLocalizedMessage(entityPlayer, "item.caliper.message.square", distance1, distance2);
            return true;
        }

        int distance1 = java.lang.Math.abs(x - startPositionX) + 1;
        int distance2 = java.lang.Math.abs(y - startPositionY) + 1;
        int distance3 = java.lang.Math.abs(z - startPositionZ) + 1;

        LanguageHandler.sendPlayerLocalizedMessage(entityPlayer, "item.caliper.message.cube", distance1, distance2, distance3);
        return true;
    }
}
