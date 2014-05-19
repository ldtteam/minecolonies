package com.minecolonies.items;

import com.minecolonies.creativetab.ModCreativeTabs;
import com.minecolonies.lib.Constants;
import com.minecolonies.util.IColony;
import com.minecolonies.util.LanguageHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemCaliper extends Item implements IColony
{
    public        String          name = "caliper";
    public static RangedAttribute use  = new RangedAttribute("player.caliberUse", 0.0, 0.0, 1.0);

    private int startPositionX;
    private int startPositionY;
    private int startPositionZ;

    public ItemCaliper()
    {
        setUnlocalizedName(getName());
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setMaxStackSize(1);
        GameRegistry.registerItem(this, getName());
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void registerIcons(IIconRegister par1IconRegister)
    {
        this.itemIcon = par1IconRegister.registerIcon(Constants.MODID + ":" + getName());
    }

    @Override
    public boolean onItemUse(ItemStack itemstack, EntityPlayer entityPlayer, World world, int i, int j, int k, int l, float par8, float par9, float par10)
    {
        if (world.isRemote) return false;
        IAttributeInstance attribute = entityPlayer.getEntityAttribute(use);
        if(attribute == null)
        {
            entityPlayer.getAttributeMap().registerAttribute(use);
            attribute = entityPlayer.getEntityAttribute(use);
        }
        if (attribute.getAttributeValue() == 0) {
            startPositionX = i;
            startPositionY = j;
            startPositionZ = k;
            attribute.setBaseValue(1.0);
            return false;
        }
        attribute.setBaseValue(0.0);
        if (startPositionX == i && startPositionY == j && startPositionZ == k) {
            LanguageHandler.sendPlayerLocalizedMessage(entityPlayer, "item.caliper.message.same");
            return false;
        }
        if (startPositionX == i) {
            if (startPositionY == j) {
                int distance = java.lang.Math.abs(k - startPositionZ) + 1;
                LanguageHandler.sendPlayerLocalizedMessage(entityPlayer, "item.caliper.message.line", distance);
                return false;
            }
            if (startPositionZ == k) {
                int distance = java.lang.Math.abs(j - startPositionY) + 1;
                LanguageHandler.sendPlayerLocalizedMessage(entityPlayer, "item.caliper.message.line", distance);
                return false;
            }
            int distance1 = java.lang.Math.abs(j - startPositionY) + 1;
            int distance2 = java.lang.Math.abs(k - startPositionZ) + 1;

            LanguageHandler.sendPlayerLocalizedMessage(entityPlayer, "item.caliper.message.square", distance1, distance2);
            return false;
        }
        if (startPositionY == j) {
            if (startPositionZ == k) {
                int distance = java.lang.Math.abs(i - startPositionX) + 1;
                LanguageHandler.sendPlayerLocalizedMessage(entityPlayer, "item.caliper.message.line", distance);
                return false;
            }
            int distance1 = java.lang.Math.abs(i - startPositionX) + 1;
            int distance2 = java.lang.Math.abs(k - startPositionZ) + 1;

            LanguageHandler.sendPlayerLocalizedMessage(entityPlayer, "item.caliper.message.square", distance1, distance2);
            return false;
        }
        if (startPositionZ == k) {
            int distance1 = java.lang.Math.abs(i - startPositionX) + 1;
            int distance2 = java.lang.Math.abs(j - startPositionY) + 1;

            LanguageHandler.sendPlayerLocalizedMessage(entityPlayer, "item.caliper.message.square", distance1, distance2);
            return false;
        }

        int distance1 = java.lang.Math.abs(i - startPositionX) + 1;
        int distance2 = java.lang.Math.abs(j - startPositionY) + 1;
        int distance3 = java.lang.Math.abs(k - startPositionZ) + 1;

        LanguageHandler.sendPlayerLocalizedMessage(entityPlayer, "item.caliper.message.cube", distance1, distance2, distance3);
        return false;
    }
}
