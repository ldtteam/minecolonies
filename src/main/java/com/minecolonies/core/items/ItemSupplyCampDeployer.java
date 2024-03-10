package com.minecolonies.core.items;

import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintTagUtils;
import com.ldtteam.structurize.placement.handlers.placement.PlacementError;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.items.ISupplyItem;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.client.gui.WindowSupplies;
import com.minecolonies.core.client.gui.WindowSupplyStory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_RANDOM_KEY;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_SAW_STORY;
import static com.minecolonies.api.util.constant.TranslationConstants.CANT_PLACE_COLONY_IN_OTHER_DIM;

/**
 * Class to handle the placement of the supplychest and with it the supplycamp.
 */
public class ItemSupplyCampDeployer extends AbstractItemMinecolonies implements ISupplyItem
{
    /**
     * Creates a new supplycamp deployer. The item is not stackable.
     *
     * @param properties the properties.
     */
    public ItemSupplyCampDeployer(final Item.Properties properties)
    {
        super("supplycampdeployer", properties.stacksTo(1));
    }

    @NotNull
    @Override
    public InteractionResult useOn(final UseOnContext ctx)
    {
        if (!ctx.getItemInHand().getOrCreateTag().contains(TAG_RANDOM_KEY))
        {
            ctx.getItemInHand().getTag().putLong(TAG_RANDOM_KEY, ctx.getClickedPos().asLong());
        }
        if (ctx.getLevel().isClientSide)
        {
            if (!MineColonies.getConfig().getServer().allowOtherDimColonies.get() && !WorldUtil.isOverworldType(ctx.getLevel()))
            {
                MessageUtils.format(CANT_PLACE_COLONY_IN_OTHER_DIM).sendTo(ctx.getPlayer());
                return InteractionResult.FAIL;
            }
            placeSupplyCamp(ctx.getClickedPos().relative(ctx.getHorizontalDirection(), 10).above(), ctx.getPlayer().getDirection(), ctx.getItemInHand(), ctx.getHand());
        }

        return InteractionResult.FAIL;
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(final Level worldIn, final Player playerIn, final InteractionHand hand)
    {
        final ItemStack stack = playerIn.getItemInHand(hand);
        if (!stack.getOrCreateTag().contains(TAG_RANDOM_KEY))
        {
            stack.getTag().putLong(TAG_RANDOM_KEY, playerIn.blockPosition().asLong());
        }

        if (worldIn.isClientSide)
        {
            if (!MineColonies.getConfig().getServer().allowOtherDimColonies.get() && !WorldUtil.isOverworldType(worldIn))
            {
                MessageUtils.format(CANT_PLACE_COLONY_IN_OTHER_DIM).sendTo(playerIn);
                return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
            }
            placeSupplyCamp(null, playerIn.getDirection(), stack, hand);
        }

        return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
    }

    /**
     * Places a supply camp on the given position looking to the given direction.
     *
     * @param pos       the position to place the supply camp at.
     * @param direction the direction the supply camp should face.
     */
    private void placeSupplyCamp(@Nullable final BlockPos pos, @NotNull final Direction direction, final ItemStack itemInHand, final InteractionHand hand)
    {
        if (!itemInHand.getOrCreateTag().contains(TAG_SAW_STORY))
        {
            new WindowSupplyStory(pos, "supplycamp", itemInHand, hand).open();
            return;
        }

        if (pos == null)
        {
            new WindowSupplies(pos, "supplycamp").open();
            return;
        }

        new WindowSupplies(pos, "supplycamp").open();
    }

    /**
     * Checks if the camp can be placed.
     *
     * @param world              the world.
     * @param pos                the position.
     * @param placementErrorList the list of placement errors.
     * @param placer             the placer.
     * @return true if so.
     */
    public static boolean canCampBePlaced(
      @NotNull final Level world,
      @NotNull final BlockPos pos,
      @NotNull final List<PlacementError> placementErrorList,
      final Player placer)
    {
        if (MineColonies.getConfig().getServer().noSupplyPlacementRestrictions.get())
        {
            return true;
        }

        final Blueprint blueprint = RenderingCache.getOrCreateBlueprintPreviewData("supplies").getBlueprint();
        if (blueprint == null)
        {
            return false;
        }

        final BlockPos zeroPos = pos.subtract(blueprint.getPrimaryBlockOffset());
        final int sizeX = blueprint.getSizeX();
        final int sizeZ = blueprint.getSizeZ();
        final int groundHeight = BlueprintTagUtils.getNumberOfGroundLevels(blueprint, 1) - 1;
        final int groundLevel = zeroPos.getY() + groundHeight;

        final List<PlacementError> needsAirAbove = new ArrayList<>();
        final List<PlacementError> needsSolidBelow = new ArrayList<>();

        for (int z = 0; z < sizeZ; z++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                final BlockPos worldPos = new BlockPos(zeroPos.getX() + x, groundLevel, zeroPos.getZ() + z);
                if (blueprint.getBlockState(new BlockPos(x, groundHeight, z)).getBlock() != ModBlocks.blockSubstitution.get())
                {
                    checkIfSolidAndNotInColony(world, worldPos, needsSolidBelow, placer);
                }

                if (BlockUtils.isAnySolid(world.getBlockState(worldPos.above())))
                {
                    needsAirAbove.add(new PlacementError(PlacementError.PlacementErrorType.NEEDS_AIR_ABOVE, worldPos.above()));
                }
            }
        }

        if (needsAirAbove.size() > sizeX*sizeZ/3 || needsSolidBelow.size() > sizeX*sizeZ/3)
        {
            placementErrorList.addAll(needsAirAbove);
            placementErrorList.addAll(needsSolidBelow);
            return false;
        }

        return true;
    }

    /**
     * Check if the there is a solid block at a position and it's not in a colony.
     *
     * @param world              the world.
     * @param pos                the position.
     * @param placementErrorList a list of placement errors.
     * @param placer             the player placing the supply camp.
     */
    private static void checkIfSolidAndNotInColony(final Level world, final BlockPos pos, @NotNull final List<PlacementError> placementErrorList, final Player placer)
    {
        final boolean isSolid = BlockUtils.isAnySolid(world.getBlockState(pos));
        final boolean notInAnyColony = hasPlacePermission(world, pos, placer);
        if (!isSolid)
        {
            placementErrorList.add(new PlacementError(PlacementError.PlacementErrorType.NOT_SOLID, pos));
        }
        if (!notInAnyColony)
        {
            placementErrorList.add(new PlacementError(PlacementError.PlacementErrorType.INSIDE_COLONY, pos));
        }
    }

    /**
     * Check if a coordinate is in any colony.
     *
     * @param world  the world to check in.
     * @param pos    the position.
     * @param placer the placer.
     * @return true if no colony found.
     */
    private static boolean hasPlacePermission(final Level world, final BlockPos pos, final Player placer)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, pos);
        return colony == null || colony.getPermissions().hasPermission(placer, Action.PLACE_BLOCKS);
    }
}
