package com.minecolonies.core.items;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.core.tileentities.TileEntityColonyFlag;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
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
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        ItemStack stack = context.getPlayer().getMainHandItem();

        if (te instanceof BannerBlockEntity || te instanceof TileEntityColonyFlag)
        {
            CompoundTag source;
            if (te instanceof BannerBlockEntity)
            {
                source = ((BannerBlockEntity) te).getItem()
                           .getTag().getCompound("BlockEntityTag");
            }
            else
            {
                source = (context.getLevel().isClientSide ? ((TileEntityColonyFlag) te).getItemClient() : ((TileEntityColonyFlag) te).getItemServer())
                           .getTag().getCompound("BlockEntityTag");
            }

            ListTag patternList = source.getList(TAG_BANNER_PATTERNS, 10);

            // Set the base pattern, if there wasn't one set.
            // This saves us attempting to alter the item itself to change the base color.
            if (!patternList.getCompound(0).getString(TAG_SINGLE_PATTERN).equals(BannerPatterns.BASE.location().toString()))
            {
                CompoundTag nbt = new CompoundTag();
                nbt.putString(TAG_SINGLE_PATTERN, BannerPatterns.BASE.location().toString());
                nbt.putInt(TAG_PATTERN_COLOR, ((AbstractBannerBlock) state.getBlock()).getColor().getId());
                patternList.add(0, nbt);
            }

            CompoundTag tag = stack.getOrCreateTagElement("BlockEntityTag");
            tag.put(TAG_BANNER_PATTERNS, patternList);

            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
    {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        // Remove the base, as they have no translations (Mojang were lazy. Or maybe saving space?)
        if (tooltip.size() > 1) tooltip.remove(1);
    }
}
