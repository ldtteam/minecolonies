package com.minecolonies.coremod.items;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structures.lib.BlueprintTagUtils;
import com.ldtteam.structurize.placement.handlers.placement.PlacementError;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.ldtteam.structurize.api.util.constant.Constants.GROUNDSTYLE_LEGACY_SHIP;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.CANT_PLACE_COLONY_IN_OTHER_DIM;

/**
 * Class to handle the placement of the supplychest and with it the supplyship.
 */
public class ItemSupplyChestDeployer extends AbstractItemMinecolonies
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
     * Offset south/west of the supply chest.
     */
    private static final int OFFSET_DISTANCE = 14;

    /**
     * Offset south/east of the supply chest.
     */
    private static final int OFFSET_LEFT = 5;

    /**
     * Offset y of the supply chest.
     */
    private static final int OFFSET_Y = 0;

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
        super("supplychestdeployer", properties.stacksTo(1).tab(ModCreativeTabs.MINECOLONIES));
    }

    @NotNull
    @Override
    public ActionResultType useOn(final ItemUseContext ctx)
    {
        if (ctx.getLevel().isClientSide)
        {
            if (!MineColonies.getConfig().getServer().allowOtherDimColonies.get() && !WorldUtil.isOverworldType(ctx.getLevel()))
            {
                return ActionResultType.FAIL;
            }
            placeSupplyShip(ctx.getLevel(), ctx.getClickedPos().relative(ctx.getClickedFace()), ctx.getPlayer().getDirection());
        }

        return ActionResultType.FAIL;
    }

    @NotNull
    @Override
    public ActionResult<ItemStack> use(final World worldIn, final PlayerEntity playerIn, final Hand hand)
    {
        final ItemStack stack = playerIn.getItemInHand(hand);
        if (worldIn.isClientSide)
        {
            if (!MineColonies.getConfig().getServer().allowOtherDimColonies.get() && !WorldUtil.isOverworldType(worldIn))
            {
                LanguageHandler.sendPlayerMessage(playerIn, CANT_PLACE_COLONY_IN_OTHER_DIM);
                return new ActionResult<>(ActionResultType.FAIL, stack);
            }
            placeSupplyShip(worldIn, null, playerIn.getDirection());
        }

        return new ActionResult<>(ActionResultType.FAIL, stack);
    }

    /**
     * Places a supply chest on the given position looking to the given direction.
     *
     * @param pos       the position to place the supply chest at.
     * @param direction the direction the supply chest should face.
     */
    private void placeSupplyShip(World world, @Nullable final BlockPos pos, @NotNull final Direction direction)
    {
        final String name = WorldUtil.isNetherType(world)
                              ? SUPPLY_SHIP_STRUCTURE_NAME_NETHER
                              : SUPPLY_SHIP_STRUCTURE_NAME;

        if (pos == null)
        {
            MineColonies.proxy.openBuildToolWindow(null, name, 0, GROUNDSTYLE_LEGACY_SHIP);
            return;
        }

        final BlockPos tempPos;
        final int rotations;
        switch (direction)
        {
            case SOUTH:
                tempPos = pos.offset(OFFSET_LEFT, OFFSET_Y, OFFSET_DISTANCE);
                rotations = ROTATE_THREE_TIMES;
                break;
            case NORTH:
                tempPos = pos.offset(-OFFSET_LEFT, OFFSET_Y, -OFFSET_DISTANCE);
                rotations = ROTATE_ONCE;
                break;
            case EAST:
                tempPos = pos.offset(OFFSET_DISTANCE, OFFSET_Y, -OFFSET_LEFT);
                rotations = ROTATE_TWICE;
                break;
            default:
                tempPos = pos.offset(-OFFSET_DISTANCE, OFFSET_Y, OFFSET_LEFT);
                rotations = ROTATE_0_TIMES;
                break;
        }
        MineColonies.proxy.openBuildToolWindow(tempPos, name, rotations, GROUNDSTYLE_LEGACY_SHIP);
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
      @NotNull final World world, @NotNull final BlockPos pos, final Blueprint ship, @NotNull final List<PlacementError> placementErrorList, final
    PlayerEntity placer)
    {
        if (MineColonies.getConfig().getServer().noSupplyPlacementRestrictions.get())
        {
            return true;
        }

        final int sizeX = ship.getSizeX();
        final int sizeZ = ship.getSizeZ();
        final int waterLevel = BlueprintTagUtils.getNumberOfGroundLevels(ship, DEFAULT_WATER_LEVELS);
        final BlockPos zeroPos = pos.subtract(ship.getPrimaryBlockOffset());

        for (int z = zeroPos.getZ(); z < zeroPos.getZ() + sizeZ; z++)
        {
            for (int x = zeroPos.getX(); x < zeroPos.getX() + sizeX; x++)
            {
                for (int y = zeroPos.getY(); y <= zeroPos.getY() + waterLevel + SCAN_HEIGHT; y++)
                {
                    if (y < zeroPos.getY() + waterLevel)
                    {
                        checkFluidAndNotInColony(world, new BlockPos(x, y, z), placementErrorList, placer);
                    }
                    else if (world.getBlockState(new BlockPos(x, y, z)).getMaterial().isSolid())
                    {
                        final PlacementError placementError = new PlacementError(PlacementError.PlacementErrorType.NEEDS_AIR_ABOVE, new BlockPos(x, y, z));
                        placementErrorList.add(placementError);
                    }
                }
            }
        }

        return placementErrorList.isEmpty();
    }

    /**
     * Check if the there is water at one of three positions.
     *
     * @param world              the world.
     * @param pos                the first position.
     * @param placementErrorList a list of placement errors.
     * @param placer             the player placing the supply camp.
     */
    private static void checkFluidAndNotInColony(final World world, final BlockPos pos, @NotNull final List<PlacementError> placementErrorList, final PlayerEntity placer)
    {
        final boolean isOverworld = WorldUtil.isOverworldType(world);
        final boolean isWater = BlockUtils.isWater(world.getBlockState(pos));
        final boolean notInAnyColony = hasPlacePermission(world, pos, placer);
        if (!isWater && isOverworld)
        {
            final PlacementError placementError = new PlacementError(PlacementError.PlacementErrorType.NOT_WATER, pos);
            placementErrorList.add(placementError);
        }
        else if (!world.getBlockState(pos).getFluidState().getType().isSame(Fluids.LAVA) && !isOverworld)
        {
            final PlacementError placementError = new PlacementError(PlacementError.PlacementErrorType.NOT_WATER, pos);
            placementErrorList.add(placementError);
        }

        if (!notInAnyColony)
        {
            final PlacementError placementError = new PlacementError(PlacementError.PlacementErrorType.INSIDE_COLONY, pos);
            placementErrorList.add(placementError);
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
    private static boolean hasPlacePermission(final World world, final BlockPos pos, final PlayerEntity placer)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, pos);
        return colony == null || colony.getPermissions().hasPermission(placer, Action.PLACE_BLOCKS);
    }
}
