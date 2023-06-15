package com.minecolonies.coremod.items;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintTagUtils;
import com.ldtteam.structurize.placement.handlers.placement.PlacementError;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.client.gui.WindowSupplies;
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

import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.CANT_PLACE_COLONY_IN_OTHER_DIM;

/**
 * Class to handle the placement of the supplychest and with it the supplycamp.
 */
public class ItemSupplyCampDeployer extends AbstractItemMinecolonies
{
    /**
     * Creates a new supplycamp deployer. The item is not stackable.
     *
     * @param properties the properties.
     */
    public ItemSupplyCampDeployer(final Item.Properties properties)
    {
        super("supplycampdeployer", properties.stacksTo(1).tab(ModCreativeTabs.MINECOLONIES));
    }

    @NotNull
    @Override
    public InteractionResult useOn(final UseOnContext ctx)
    {
        if (ctx.getLevel().isClientSide)
        {
            if (!MineColonies.getConfig().getServer().allowOtherDimColonies.get() && !WorldUtil.isOverworldType(ctx.getLevel()))
            {
                MessageUtils.format(CANT_PLACE_COLONY_IN_OTHER_DIM).sendTo(ctx.getPlayer());
                return InteractionResult.FAIL;
            }
            placeSupplyCamp(ctx.getClickedPos().relative(ctx.getClickedFace()), ctx.getPlayer().getDirection());
        }

        return InteractionResult.FAIL;
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(final Level worldIn, final Player playerIn, final InteractionHand hand)
    {
        final ItemStack stack = playerIn.getItemInHand(hand);
        if (worldIn.isClientSide)
        {
            if (!MineColonies.getConfig().getServer().allowOtherDimColonies.get() && !WorldUtil.isOverworldType(worldIn))
            {
                MessageUtils.format(CANT_PLACE_COLONY_IN_OTHER_DIM).sendTo(playerIn);
                return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
            }
            placeSupplyCamp(null, playerIn.getDirection());
        }

        return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
    }

    /**
     * Places a supply camp on the given position looking to the given direction.
     *
     * @param pos       the position to place the supply camp at.
     * @param direction the direction the supply camp should face.
     */
    private void placeSupplyCamp(@Nullable final BlockPos pos, @NotNull final Direction direction)
    {
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
        final int groundLevel = zeroPos.getY() + BlueprintTagUtils.getNumberOfGroundLevels(blueprint, 1) - 1;

        for (int z = zeroPos.getZ(); z < zeroPos.getZ() + sizeZ; z++)
        {
            for (int x = zeroPos.getX(); x < zeroPos.getX() + sizeX; x++)
            {
                checkIfSolidAndNotInColony(world, new BlockPos(x, groundLevel, z), placementErrorList, placer);

                if (world.getBlockState(new BlockPos(x, groundLevel + 1, z)).getMaterial().isSolid())
                {
                    final PlacementError placementError = new PlacementError(PlacementError.PlacementErrorType.NEEDS_AIR_ABOVE, new BlockPos(x, groundLevel + 1, z));
                    placementErrorList.add(placementError);
                }
            }
        }

        return placementErrorList.isEmpty();
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
        final boolean isSolid = world.getBlockState(pos).getMaterial().isSolid();
        final boolean notInAnyColony = hasPlacePermission(world, pos, placer);
        if (!isSolid)
        {
            final PlacementError placementError = new PlacementError(PlacementError.PlacementErrorType.NOT_SOLID, pos);
            placementErrorList.add(placementError);
        }
        if (!notInAnyColony)
        {
            final PlacementError placementError = new PlacementError(PlacementError.PlacementErrorType.INSIDE_COLONY, pos);
            placementErrorList.add(placementError);
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
