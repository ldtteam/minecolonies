package com.minecolonies.core.items;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.core.tileentities.TileEntityColonyFlag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * This item represents the colony flag banner, both wall and floor blocks.
 * Allows duplication of other banner pattern lists to its own default
 */
public class ItemColonyFlagBanner extends BannerItem
{
    public ItemColonyFlagBanner(String name, Properties properties)
    {
        this(ModBlocks.blockColonyBanner, ModBlocks.blockColonyWallBanner, properties.stacksTo(1));
    }

    public ItemColonyFlagBanner(Block standingBanner, Block wallBanner, Properties builder)
    {
        super(standingBanner, wallBanner, builder);
    }

    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        // Duplicate the patterns of the banner that was clicked on
        BlockEntity te = context.getLevel().getBlockEntity(context.getClickedPos());
        ItemStack stack = context.getPlayer().getMainHandItem();

        if (te instanceof BannerBlockEntity || te instanceof TileEntityColonyFlag)
        {
            final BannerPatternLayers bannerPatternLayers;
            if (te instanceof BannerBlockEntity)
            {
                bannerPatternLayers = ((BannerBlockEntity) te).getPatterns();
            }
            else
            {
                bannerPatternLayers = ((TileEntityColonyFlag) te).getPatterns();
            }

            stack.set(DataComponents.BANNER_PATTERNS, bannerPatternLayers);
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable TooltipContext ctx, List<Component> tooltip, TooltipFlag flagIn)
    {
        super.appendHoverText(stack, ctx, tooltip, flagIn);

        // Remove the base, as they have no translations (Mojang were lazy. Or maybe saving space?)
        if (tooltip.size() > 1) tooltip.remove(1);
    }
}
