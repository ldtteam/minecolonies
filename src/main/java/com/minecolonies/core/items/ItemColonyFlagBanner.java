package com.minecolonies.core.items;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.core.tileentities.TileEntityColonyFlag;
import com.mojang.datafixers.util.Pair;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * This item represents the colony flag banner, both wall and floor blocks.
 * Allows duplication of other banner pattern lists to its own default
 */
public class ItemColonyFlagBanner extends BannerItem
{
    public ItemColonyFlagBanner(String name, Properties properties)
    {
        this(ModBlocks.blockColonyBanner, ModBlocks.blockColonyWallBanner, properties.stacksTo(16));
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
        ItemStack stack = context.getItemInHand();

        if (te instanceof BannerBlockEntity || te instanceof TileEntityColonyFlag)
        {
            BannerPattern.Builder patternsBuilder = new BannerPattern.Builder();
            List<Pair<Holder<BannerPattern>, DyeColor>> source;

            if (te instanceof BannerBlockEntity)
            {
                source = ((BannerBlockEntity) te).getPatterns();
            }
            else
            {
                source = ((TileEntityColonyFlag) te).getPatterns();
            }

            CompoundTag bannerPattern = new CompoundTag();
            source.forEach((pattern) -> patternsBuilder.addPattern(pattern.getFirst(), pattern.getSecond()));
            bannerPattern.put(TAG_BANNER_PATTERNS, patternsBuilder.toListTag());

            stack.addTagElement("BlockEntityTag", bannerPattern);
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
    {
        CompoundTag tag = BlockItem.getBlockEntityData(stack);
        if (tag != null && tag.contains(TAG_BANNER_PATTERNS))
        {
            super.appendHoverText(stack, worldIn, tooltip, flagIn);
        }
        else
        {
            tooltip.add(Component.translatable("com.minecolonies.coremod.item.colony_banner.tooltipempty"));
        }
    }
}
