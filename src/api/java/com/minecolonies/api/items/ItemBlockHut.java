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
import org.jetbrains.annotations.NotNull;

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
    public void addInformation(@NotNull ItemStack stack, World worldIn, @NotNull List<ITextComponent> tooltip, @NotNull ITooltipFlag flagIn)
    {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if (block.needsResearch())
        {
            tooltip.add(new TranslationTextComponent(TranslationConstants.HUT_NEEDS_RESEARCH_TOOLTIP_1, block.getTranslatedName(), block.getName()));
            tooltip.add(new TranslationTextComponent(TranslationConstants.HUT_NEEDS_RESEARCH_TOOLTIP_2, block.getTranslatedName(), block.getName()));
        }
    }

    /**
     * Checks which huts need to be researched in this colony.
     * This is a static function, and updates all hut block types as colonyView packets are received.
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
        // The warehouse isn't included for ModBlocks.getHuts(), so check it separately.
        // Not very likely for someone to lock it behind research, but plausible.
        ModBlocks.blockHutWareHouse.checkResearch(colony);
    }

}
