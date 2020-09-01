package com.minecolonies.api.items;

import java.util.List;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.util.constant.TranslationConstants;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A custom item class for hut blocks.
 */
public class ItemBlockHut extends BlockItem
{

	/**
	 * This items block.
	 */
    private AbstractBlockHut<?> block;

    /**
     * Creates a new ItemBlockHut representing the item form of the given {@link AbstractBlockHut}.
     * 
     * @param block   the {@link AbstractBlockHut} this item represents.
     * @param builder the item properties to use.
     */
    public ItemBlockHut(AbstractBlockHut<?> block, Properties builder)
    {
        super(block, builder);
        this.block = block;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if (block.needsResearch())
        {
            tooltip.add(new TranslationTextComponent(TranslationConstants.HUT_NEEDS_RESEARCH_TOOLTIP_1, block.getNameTextComponent()));
            tooltip.add(new TranslationTextComponent(TranslationConstants.HUT_NEEDS_RESEARCH_TOOLTIP_2, block.getNameTextComponent()));
        }
    }

    /**
     * Checks whether this hut is needs to be researched in this colony.
     * 
     * @param colony the colony to check.
     */
    @OnlyIn(Dist.CLIENT)
    public static void checkResearch(final IColonyView colony)
    {
        for(AbstractBlockHut<?> hut : ModBlocks.getHuts())
        {
            hut.checkResearch(colony);
        }
    }

}
