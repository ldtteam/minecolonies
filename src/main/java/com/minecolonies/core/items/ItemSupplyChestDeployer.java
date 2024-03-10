package com.minecolonies.core.items;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintTagUtils;
import com.ldtteam.structurize.placement.handlers.placement.PlacementError;
import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.entity.pathfinding.SurfaceType;
import com.minecolonies.api.items.ISupplyItem;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.client.gui.WindowSupplies;
import com.minecolonies.core.client.gui.WindowSupplyStory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_RANDOM_KEY;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_SAW_STORY;
import static com.minecolonies.api.util.constant.TranslationConstants.CANT_PLACE_COLONY_IN_OTHER_DIM;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;

/**
 * Class to handle the placement of the supplychest and with it the supplyship.
 */
public class ItemSupplyChestDeployer extends AbstractItemMinecolonies implements ISupplyItem
{
    /**
     * StructureIterator name and location.
     */
    private static final String SUPPLY_SHIP_STRUCTURE_NAME = "supplyship";

    /**
     * StructureIterator name for nether dimension
     */
    private static final String SUPPLY_SHIP_STRUCTURE_NAME_NETHER = "nethership";

    /**
     * Height to scan in which should be air.
     */
    private static final int SCAN_HEIGHT = 7;

    /**
     * If a schematic lacks a groundlevel tag, we assume it has this many levels of water
     */
    private static final int DEFAULT_WATER_LEVELS = 3;

    /**
     * Creates a new supplychest deployer. The item is not stackable.
     *
     * @param properties the properties.
     */
    public ItemSupplyChestDeployer(final Item.Properties properties)
    {
        super("supplychestdeployer", properties.stacksTo(1));
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
                return InteractionResult.FAIL;
            }
            placeSupplyShip(ctx.getLevel(), ctx.getClickedPos().relative(ctx.getHorizontalDirection(), 10), ctx.getHand(), ctx.getItemInHand());
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
            placeSupplyShip(worldIn, null, hand, stack);
        }

        return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
    }

    /**
     * Places a supply chest on the given position looking to the given direction.
     *
     * @param pos        the position to place the supply chest at.
     * @param hand       the hand that was used to place it.
     * @param itemInHand
     */
    private void placeSupplyShip(Level world, @Nullable final BlockPos pos, final InteractionHand hand, final ItemStack itemInHand)
    {
        final String name = WorldUtil.isNetherType(world)
                              ? SUPPLY_SHIP_STRUCTURE_NAME_NETHER
                              : SUPPLY_SHIP_STRUCTURE_NAME;


        if (!itemInHand.getOrCreateTag().contains(TAG_SAW_STORY))
        {
            new WindowSupplyStory(pos, name, itemInHand, hand).open();
            return;
        }

        if (pos == null)
        {
            new WindowSupplies(pos, name).open();
            return;
        }

        new WindowSupplies(pos, name).open();
    }

    /**
     * Checks if the ship can be placed.
     *
     * @param world              the world.
     * @param pos                the pos.
     * @param ship               the blueprint.
     * @param placementErrorList the list of placement errors.
     * @param placer             the placer.
     * @return true if so.
     */
    public static boolean canShipBePlaced(
            @NotNull final Level world, @NotNull final BlockPos pos, final Blueprint ship, @NotNull final List<PlacementError> placementErrorList, final
    Player placer)
    {
        if (MineColonies.getConfig().getServer().noSupplyPlacementRestrictions.get())
        {
            return true;
        }

        final int sizeX = ship.getSizeX();
        final int sizeZ = ship.getSizeZ();
        final int waterLevel = BlueprintTagUtils.getNumberOfGroundLevels(ship, DEFAULT_WATER_LEVELS);
        final BlockPos zeroPos = pos.subtract(ship.getPrimaryBlockOffset());

        final List<PlacementError> needsAirAbove = new ArrayList<>();
        final List<PlacementError> needsWaterList = new ArrayList<>();

        for (int z = zeroPos.getZ(); z < zeroPos.getZ() + sizeZ; z++)
        {
            for (int x = zeroPos.getX(); x < zeroPos.getX() + sizeX; x++)
            {
                for (int y = zeroPos.getY(); y <= zeroPos.getY() + waterLevel + SCAN_HEIGHT; y++)
                {
                    if (y < zeroPos.getY() + waterLevel)
                    {
                        checkFluidAndNotInColony(world, new BlockPos(x, y, z), needsWaterList, placer);
                    }
                    else if (BlockUtils.isAnySolid(world.getBlockState(new BlockPos(x, y, z))))
                    {
                        needsAirAbove.add(new PlacementError(PlacementError.PlacementErrorType.NEEDS_AIR_ABOVE, new BlockPos(x, y, z)));
                    }
                }
            }
        }

        if (needsAirAbove.size() > sizeX*sizeZ/3 || needsWaterList.size() > sizeX*sizeZ/3)
        {
            placementErrorList.addAll(needsAirAbove);
            placementErrorList.addAll(needsWaterList);
            return false;
        }

        return true;
    }

    /**
     * Check if the there is water at one of three positions.
     *
     * @param world              the world.
     * @param pos                the first position.
     * @param placementErrorList a list of placement errors.
     * @param placer             the player placing the supply camp.
     */
    private static void checkFluidAndNotInColony(final Level world, final BlockPos pos, @NotNull final List<PlacementError> placementErrorList, final Player placer)
    {
        final boolean isOverworld = WorldUtil.isOverworldType(world);
        final boolean isWater = SurfaceType.isWater(world, pos);
        final boolean notInAnyColony = hasPlacePermission(world, pos, placer);
        if (!isWater && isOverworld)
        {
            placementErrorList.add(new PlacementError(PlacementError.PlacementErrorType.NOT_WATER, pos));
        }
        else if (!world.getBlockState(pos).getFluidState().getType().isSame(Fluids.LAVA) && !isOverworld)
        {
            placementErrorList.add(new PlacementError(PlacementError.PlacementErrorType.NOT_WATER, pos));
        }

        if (!notInAnyColony)
        {
            placementErrorList.add(new PlacementError(PlacementError.PlacementErrorType.INSIDE_COLONY, pos));
        }
    }

    /**
     * Check if any of the coordinates is in any colony.
     *
     * @param world  the world to check in.
     * @param pos    the first position.
     * @param placer the placer.
     * @return true if no colony found.
     */
    private static boolean hasPlacePermission(final Level world, final BlockPos pos, final Player placer)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, pos);
        return colony == null || colony.getPermissions().hasPermission(placer, Action.PLACE_BLOCKS);
    }
}
