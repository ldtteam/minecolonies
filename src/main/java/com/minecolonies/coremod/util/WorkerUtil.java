package com.minecolonies.coremod.util;

import com.ldtteam.structures.helpers.Structure;
import com.ldtteam.structurize.util.BlockInfo;
import com.minecolonies.api.entity.ai.Status;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.util.EntityUtils;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.entity.ai.citizen.miner.Level;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.block.Block;
import net.minecraft.block.GlazedTerracottaBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.entity.MoverType;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.CitizenConstants.MOVE_MINIMAL;
import static com.minecolonies.api.util.constant.CitizenConstants.ROTATION_MOVEMENT;

/**
 * Utility methods for BlockPos.
 */
public final class WorkerUtil
{
    /**
     * Default range for moving to something until we stop.
     */
    private static final double MIDDLE_BLOCK_OFFSET = 0.5D;

    /**
     * Placeholder text in a level sign.
     */
    private static final String LEVEL_SIGN_TEXT = "{\"text\":\"level_placeholder\"}";
    private static final String LEVEL_SIGN_FIRST_ROW = "Text1";

    private WorkerUtil()
    {
        //Hide default constructor.
    }

    /**
     * Checks if a certain block is a pathBlock (roadBlock).
     *
     * @param block the block to analyze.
     * @return true if is so.
     */
    public static boolean isPathBlock(final Block block)
    {
        return block == Blocks.GRAVEL || block == Blocks.STONE_BRICKS || block == Blocks.GRASS_PATH;
    }

    /**
     * {@link WorkerUtil#isWorkerAtSiteWithMove(EntityCitizen, int, int, int, int)}.
     *
     * @param worker Worker to check.
     * @param site   Chunk coordinates of site to check.
     * @param range  Range to check in.
     * @return True when within range, otherwise false.
     */
    public static boolean isWorkerAtSiteWithMove(@NotNull final EntityCitizen worker, @NotNull final BlockPos site, final int range)
    {
        return isWorkerAtSiteWithMove(worker, site.getX(), site.getY(), site.getZ(), range);
    }

    /**
     * Checks if a worker is at his working site.
     * If he isn't, sets it's path to the location.
     *
     * @param worker Worker to check
     * @param x      X-coordinate
     * @param y      Y-coordinate
     * @param z      Z-coordinate
     * @param range  Range to check in
     * @return True if worker is at site, otherwise false.
     */
    public static boolean isWorkerAtSiteWithMove(@NotNull final EntityCitizen worker, final int x, final int y, final int z, final int range)
    {
        if (!EntityUtils.isLivingAtSiteWithMove(worker, x, y, z, range))
        {
            //If not moving the try setting the point where the entity should move to
            if (worker.getNavigator().noPath() && !EntityUtils.tryMoveLivingToXYZ(worker, x, y, z))
            {
                worker.getCitizenStatusHandler().setStatus(Status.PATHFINDING_ERROR);
            }
            return false;
        }
        return true;
    }

    /**
     * Attempt to move to XYZ.
     * True when found and destination is set.
     *
     * @param citizen     Citizen to move to XYZ.
     * @param destination Chunk coordinate of the distance.
     * @return True when found, and destination is set, otherwise false.
     */
    public static PathResult moveLivingToXYZ(@NotNull final AbstractEntityCitizen citizen, @NotNull final BlockPos destination)
    {
        return citizen.getNavigator().moveToXYZ(destination.getX(), destination.getY(), destination.getZ(), 1.0);
    }

    /**
     * Recalls the citizen, notifies player if not successful.
     *
     * @param spawnPoint the spawnPoint.
     * @param citizen    the citizen.
     * @return true if succesful.
     */
    public static boolean setSpawnPoint(@Nullable final BlockPos spawnPoint, @NotNull final AbstractEntityCitizen citizen)
    {
        if (spawnPoint == null)
        {
            return false;
        }

        citizen.setLocationAndAngles(
          spawnPoint.getX() + MIDDLE_BLOCK_OFFSET,
          spawnPoint.getY(),
          spawnPoint.getZ() + MIDDLE_BLOCK_OFFSET,
          citizen.getRotationYaw(),
          citizen.getRotationPitch());
        citizen.getNavigator().clearPath();
        return true;
    }

    /**
     * Get a Tooltype for a certain block.
     * We need this because minecraft has a lot of blocks which have strange or no required tool.
     *
     * @param target the target block.
     * @return the toolType to use.
     */
    public static IToolType getBestToolForBlock(final Block target)
    {
        final net.minecraftforge.common.ToolType tool = target.getHarvestTool(target.getDefaultState());
        if (tool == null)
        {
            return ToolType.NONE;
        }
        final IToolType toolType = ToolType.getToolType(tool.getName());

        if (toolType == ToolType.NONE && target.getDefaultState().getMaterial() == Material.WOOD)
        {
            return ToolType.AXE;
        }
        else if (target instanceof GlazedTerracottaBlock)
        {
            return ToolType.PICKAXE;
        }
        return toolType;
    }

    /**
     * Get the correct havestlevel for a certain block.
     * We need this because minecraft has a lot of blocks which have strange or no required harvestlevel.
     *
     * @param target the target block.
     * @return the required harvestLevel.
     */
    public static int getCorrectHavestLevelForBlock(final Block target)
    {
        final int required = target.getHarvestLevel(target.getDefaultState());

        if ((required == -1 && target.getDefaultState().getMaterial() == Material.WOOD)
              || target instanceof GlazedTerracottaBlock)
        {
            return 0;
        }
        return required;
    }

    /**
     * Returns whether or not a citizen is heading to a specific location.
     *
     * @param citizen Citizen you want to check
     * @param x       X-coordinate
     * @param z       Z-coordinate
     * @return True if citizen heads to (x, z), otherwise false
     */
    public static boolean isPathingTo(@NotNull final AbstractEntityCitizen citizen, final int x, final int z)
    {
        final PathPoint pathpoint = citizen.getNavigator().getPath().getFinalPathPoint();
        return pathpoint != null && pathpoint.x == x && pathpoint.z == z;
    }

    /**
     * Change the citizens Rotation to look at said block.
     *
     * @param block the block he should look at.
     */
    public static void faceBlock(@Nullable final BlockPos block, final AbstractEntityCitizen citizen)
    {
        if (block == null)
        {
            return;
        }

        final double xDifference = block.getX() - citizen.getPosition().getX();
        final double zDifference = block.getZ() - citizen.getPosition().getZ();
        final double yDifference = block.getY() - (citizen.getPosition().getY() + citizen.getEyeHeight());

        final double squareDifference = Math.sqrt(xDifference * xDifference + zDifference * zDifference);
        final double intendedRotationYaw = (Math.atan2(zDifference, xDifference) * 180.0D / Math.PI) - 90.0;
        final double intendedRotationPitch = -(Math.atan2(yDifference, squareDifference) * 180.0D / Math.PI);
        citizen.setOwnRotation((float) EntityUtils.updateRotation(citizen.getRotationYaw(), intendedRotationYaw, ROTATION_MOVEMENT),
          (float) EntityUtils.updateRotation(citizen.getRotationPitch(), intendedRotationPitch, ROTATION_MOVEMENT));

        final double goToX = xDifference > 0 ? MOVE_MINIMAL : -MOVE_MINIMAL;
        final double goToZ = zDifference > 0 ? MOVE_MINIMAL : -MOVE_MINIMAL;

        //Have to move the entity minimally into the direction to render his new rotation.
        citizen.move(MoverType.SELF, new Vec3d((float) goToX, 0, (float) goToZ));
    }

    /**
     * Find the first level in a structure and return it.
     * @param structure the structure to scan.
     * @return the position of the sign.
     */
    @Nullable
    public static BlockPos findFirstLevelSign(final Structure structure)
    {
        for (int j = 0; j < structure.getHeight(); j++)
        {
            for (int k = 0; k < structure.getLength(); k++)
            {
                for (int i = 0; i < structure.getWidth(); i++)
                {
                    @NotNull final BlockPos localPos = new BlockPos(i, j, k);
                    final BlockInfo te = structure.getBlockInfo(localPos);
                    if (te != null)
                    {
                        final CompoundNBT teData = te.getTileEntityData();
                        if (teData != null && teData.getString(LEVEL_SIGN_FIRST_ROW).equals(LEVEL_SIGN_TEXT))
                        {
                            // try to make an anchor in 0,0,0 instead of the middle of the structure
                            BlockPos zeroAnchor = structure.getPosition();
                            zeroAnchor = zeroAnchor.add(new BlockPos(-(structure.getWidth() / 2), 0, -(structure.getLength() / 2)));
                            return zeroAnchor.add(localPos);
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Updated the level sign of a certain level in the world.
     * @param world the world.
     * @param level the level to update.
     * @param levelId the id of the level.
     */
    public static void updateLevelSign(final World world, final Level level, final int levelId)
    {
        @Nullable final BlockPos levelSignPos = level.getLevelSign();

        if (levelSignPos != null)
        {
            final TileEntity te = world.getTileEntity(levelSignPos);

            if (te instanceof SignTileEntity)
            {
                final BlockState BlockState = world.getBlockState(levelSignPos);
                final SignTileEntity teLevelSign = (SignTileEntity) te;

                teLevelSign.signText[0] = new StringTextComponent(TextFormatting.getTextWithoutFormattingCodes(
                  LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.minerMineNode") + ": " + levelId));
                teLevelSign.signText[1] = new StringTextComponent(TextFormatting.getTextWithoutFormattingCodes("Y: " + (level.getDepth() + 1)));
                teLevelSign.signText[2] = new StringTextComponent(TextFormatting.getTextWithoutFormattingCodes(LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.minerNode") + ": " + level.getNumberOfBuiltNodes()));
                teLevelSign.signText[3] = new StringTextComponent(TextFormatting.getTextWithoutFormattingCodes(""));

                teLevelSign.markDirty();
                world.notifyBlockUpdate(levelSignPos, BlockState, BlockState, 3);
            }
        }
    }
}
