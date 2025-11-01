package com.minecolonies.core.items;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.core.blocks.MinecoloniesCropBlock;
import com.minecolonies.core.blocks.MinecoloniesFarmland;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A custom item class for crop blocks.
 */
public class ItemCrop extends BlockItem
{
    /**
     * The preferred biome.
     */
    @Nullable
    private final TagKey<Biome> preferredBiome;

    /**
     * Creates a new Crop item.
     *
     * @param cropBlock   the {@link AbstractBlockHut} this item represents.
     * @param builder the item properties to use.
     */
    public ItemCrop(@NotNull final MinecoloniesCropBlock cropBlock, @NotNull final Properties builder, @Nullable final TagKey<Biome> preferredBiome)
    {
        super(cropBlock, builder.food(new FoodProperties.Builder().nutrition(1).saturationMod(0.3F).build()));
        this.preferredBiome = preferredBiome;
    }

    @Override
    protected boolean canPlace(BlockPlaceContext ctx, @NotNull BlockState state)
    {
        Player player = ctx.getPlayer();
        if (!player.isCreative())
        {
            final BlockPos clickedPos = ctx.getClickedPos().below();
            final BlockState worldState = ctx.getLevel().getBlockState(clickedPos);
            if (ctx.getLevel().isClientSide && (worldState.getBlock() instanceof MinecoloniesFarmland || worldState.getBlock() instanceof FarmBlock))
            {
                MessageUtils.format(Component.translatable("com.minecolonies.core.crop.cantplant")).sendTo(player);
            }
            return false;
        }
        CollisionContext collisioncontext = player == null ? CollisionContext.empty() : CollisionContext.of(player);
        return (!this.mustSurvive() || state.canSurvive(ctx.getLevel(), ctx.getClickedPos())) && ctx.getLevel().isUnobstructed(state, ctx.getClickedPos(), collisioncontext);
    }

    @Override
    public void appendHoverText(@NotNull final ItemStack stack, @Nullable final Level worldIn, @NotNull final List<Component> tooltip, @NotNull final TooltipFlag flagIn)
    {
        tooltip.add(Component.translatable(TranslationConstants.CROP_TOOLTIP).withStyle(ChatFormatting.GRAY));
        if (preferredBiome != null && worldIn != null)
        {
            tooltip.add(Component.translatable(TranslationConstants.BIOME_TOOLTIP + "." + preferredBiome.location().getPath()));
            if (worldIn.getBiome(Minecraft.getInstance().player.blockPosition()).is(preferredBiome))
            {
                tooltip.add(Component.translatable("com.minecolonies.core.item.crop.tooltip.biome.match").withStyle(ChatFormatting.GREEN));
            }
            else
            {
                tooltip.add(Component.translatable("com.minecolonies.core.item.crop.tooltip.biome.nomatch").withStyle(ChatFormatting.RED));
            }
        }
        tooltip.add(Component.translatable(TranslationConstants.CROP_TOOLTIP_HOE).withStyle(ChatFormatting.DARK_AQUA).withStyle(ChatFormatting.ITALIC));
    }

    /**
     * Check if this can planted in a given biome.
     * @param biome the biome to check.
     * @return true if so.
     */
    public boolean canBePlantedIn(final Holder<Biome> biome)
    {
        return preferredBiome == null ||  biome.is(preferredBiome);
    }
}
