package com.minecolonies.api.colony.fields.plantation;

import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.util.constant.CitizenConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.UnaryOperator;

/**
 * Basic interface that allows to chain the planter AI to the plantation module.
 */
public interface BasicPlanterAI
{
    /**
     * Makes the planter walk to a given block.
     *
     * @param blockPos the block to walk to.
     * @return true whilst the planter is walking, false when he reached the position.
     */
    boolean planterWalkToBlock(final BlockPos blockPos);

    /**
     * Makes the planter walk to a given block.
     *
     * @param blockPos the block to walk to.
     * @param range    the minimum range the planter should to "reach" the position. Defaults to {@link CitizenConstants#DEFAULT_RANGE_FOR_DELAY}
     * @return true whilst the planter is walking, false when he reached the position.
     */
    boolean planterWalkToBlock(final BlockPos blockPos, final int range);

    /**
     * Default block mining action the AI can perform, handles all the underlying logic like requiring tools.
     *
     * @param blockPos  the position to mine.
     * @param isHarvest whether the operation is considered a "harvesting of a plant", and thus if the AI should get XP or not.
     * @return the mining result.
     */
    IPlantationModule.PlanterMineBlockResult planterMineBlock(final BlockPos blockPos, boolean isHarvest);

    /**
     * Default block placement operation, handles all the things like requiring the item.
     *
     * @param blockToPlaceAt  where to place the block.
     * @param item            the item to place down.
     * @param numberToRequest the amount of items to request if the planter no longer has these items available.
     * @return whether the planter could place the plant or not.
     */
    boolean planterPlaceBlock(final BlockPos blockToPlaceAt, final Item item, final int numberToRequest);

    /**
     * Default block placement operation, handles all the things like requiring the item.
     *
     * @param blockToPlaceAt     where to place the block.
     * @param item               the item to place down.
     * @param numberToRequest    the amount of items to request if the planter no longer has these items available.
     * @param blockStateModifier a modifier function that changes the block state for the planted item.
     * @return whether the planter could place the plant or not.
     */
    boolean planterPlaceBlock(final BlockPos blockToPlaceAt, final Item item, final int numberToRequest, UnaryOperator<BlockState> blockStateModifier);

    /**
     * Allows the planter AI to request items needed for operation on this field.
     *
     * @param deliverable any RS compatible deliverable item.
     * @return true if the items are available.
     */
    boolean requestItems(IDeliverable deliverable);
}
