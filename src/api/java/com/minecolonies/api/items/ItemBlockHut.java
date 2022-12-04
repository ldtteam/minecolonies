package com.minecolonies.api.items;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.blocks.AbstractBlockHut;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull final Level level,
                                                  @NotNull final Player player,
                                                  @NotNull final InteractionHand hand)
    {
        if (hand == InteractionHand.MAIN_HAND && level.isClientSide())
        {
            MinecoloniesAPIProxy.getInstance().getBuildingDataManager().openBuildingBrowser(block);

            return InteractionResultHolder.success(player.getItemInHand(hand));
        }

        return super.use(level, player, hand);
    }
}
