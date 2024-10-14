package com.minecolonies.core.items;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.blocks.MinecoloniesCropBlock;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.biome.Biome;
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
     * Creates a new Crop item.
     *
     * @param cropBlock   the {@link AbstractBlockHut} this item represents.
     * @param builder the item properties to use.
     */
    public ItemCrop(@NotNull final MinecoloniesCropBlock cropBlock, @NotNull final Properties builder)
    {
        super(cropBlock, builder.food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.3F).build()));
    }

    @Override
    protected boolean canPlace(BlockPlaceContext ctx, @NotNull BlockState state)
    {
        Player player = ctx.getPlayer();
        if (!player.isCreative() && MineColonies.getConfig().getServer().limitCropsToFarmers.get())
        {
            if (ctx.getLevel().isClientSide)
            {
                MessageUtils.format(Component.translatable("com.minecolonies.core.crop.cantplant")).sendTo(player);
            }
            return false;
        }
        CollisionContext collisioncontext = player == null ? CollisionContext.empty() : CollisionContext.of(player);
        return (!this.mustSurvive() || state.canSurvive(ctx.getLevel(), ctx.getClickedPos())) && ctx.getLevel().isUnobstructed(state, ctx.getClickedPos(), collisioncontext);
    }

    @Override
    public void appendHoverText(@NotNull final ItemStack stack, @Nullable final TooltipContext ctx, @NotNull final List<Component> tooltip, @NotNull final TooltipFlag flagIn)
    {
        if (MineColonies.getConfig().getServer().limitCropsToFarmers.get())
        {
            tooltip.add(Component.translatable(TranslationConstants.CROP_TOOLTIP));
        }
        TagKey<Biome> preferredBiome = getCropBlock().getPreferredBiome(getBlock().defaultBlockState());
        if (preferredBiome != null)
        {
            tooltip.add(Component.translatable(TranslationConstants.BIOME_TOOLTIP + "." + preferredBiome.location().getPath()));
        }
    }

    /**
     * Check if this can planted in a given biome.
     * @param biome the biome to check.
     * @return true if so.
     */
    public boolean canBePlantedIn(final Holder<Biome> biome)
    {
        TagKey<Biome> preferredBiome = getCropBlock().getPreferredBiome(getBlock().defaultBlockState());
        return preferredBiome == null ||  biome.is(preferredBiome);
    }

    /**
     * Used for accessing some methods on the MinecoloniesCropBlock class.
     * @return The block of this item, cast to MinecoloniesCropBlock.
     */
    private MinecoloniesCropBlock getCropBlock() {
        return (MinecoloniesCropBlock) getBlock();
    }
}
