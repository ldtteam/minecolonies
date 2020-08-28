package com.minecolonies.coremod.items;

import java.util.ArrayList;
import java.util.List;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.constant.TranslationConstants;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

/**
 * A custom item class for hut blocks.
 */
public class ItemBlockHut extends BlockItem
{

    private boolean needsResearch = false;
    private AbstractBlockHut<?> block;
    private static final List<ItemBlockHut> HUTS = new ArrayList<>();

    public ItemBlockHut(AbstractBlockHut<?> block, Properties builder)
    {
        super(block, builder);
        HUTS.add(this);
        this.block = block;
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if (needsResearch)
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
    public static void checkResearch(final IColony colony)
    {
        HUTS.forEach(hut -> {
            if (colony == null) {
                hut.needsResearch = false;
            } else {
                hut.needsResearch = hut.block.checkResearch(colony);
            }
        });
    }

}
