package com.minecolonies.coremod.items;


import net.minecraft.block.Block;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Custom item for cushions. Allows for storing the meta data (colour) for the
 * block in its meta data field
 * 
 * @author Serial Coder Lain (serialcoderlain@gmail.com)
 */
public class ItemCushions extends ItemBlock {
    public ItemCushions(Block block) {
        super(block);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int metadata) {
        return metadata;
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey(stack) + "." + EnumDyeColor.byMetadata(stack.getMetadata()).toString();
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
    }
}