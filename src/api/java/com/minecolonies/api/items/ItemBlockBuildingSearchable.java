package com.minecolonies.api.items;

import com.minecolonies.api.MinecoloniesAPIProxy;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * A custom item class for searchable blocks.
 */
public class ItemBlockBuildingSearchable extends BlockItem
{
    /**
     * Creates a new ItemBlockBuildingSearchable representing the item form of the given {@link Block}.
     *
     * @param block   the {@link Block} this item represents.
     * @param builder the item properties to use.
     */
    public ItemBlockBuildingSearchable(@NotNull final Block block,
                                       @NotNull final Properties builder)
    {
        super(block, builder);
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull final Level level,
                                                  @NotNull final Player player,
                                                  @NotNull final InteractionHand hand)
    {
        if (hand == InteractionHand.MAIN_HAND && level.isClientSide())
        {
            MinecoloniesAPIProxy.getInstance().getBuildingDataManager().openBuildingBrowser(getBlock());

            return InteractionResultHolder.success(player.getItemInHand(hand));
        }

        return super.use(level, player, hand);
    }
}
