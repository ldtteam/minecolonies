package com.minecolonies.coremod.util;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structurize.util.BlockInfo;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.entity.ai.Status;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingFlorist;
import com.minecolonies.coremod.entity.ai.citizen.miner.Level;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.tileentities.TileEntityCompostedDirt;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.GlazedTerracottaBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.MoverType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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
    private static final String LEVEL_SIGN_TEXT      = "{\"text\":\"level_placeholder\"}";
    private static final String LEVEL_SIGN_FIRST_ROW = "Text1";

    /**
     * List of tools to test blocks against, used for finding right tool.
     */
    public static List<Tuple<ToolType, ItemStack>> tools;

    private WorkerUtil()
    {
        //Hide default constructor.
    }

    /**
     * Gets or initializes the test tool list.
     *
     * @return the list of possible tools.
     */
    public static List<Tuple<ToolType, ItemStack>> getOrInitTestTools()
    {
        if (tools == null)
        {
            tools = new ArrayList<>();
            tools.add(new Tuple<>(ToolType.HOE, new ItemStack(Items.WOODEN_HOE)));
            tools.add(new Tuple<>(ToolType.SHOVEL, new ItemStack(Items.WOODEN_SHOVEL)));
            tools.add(new Tuple<>(ToolType.AXE, new ItemStack(Items.WOODEN_AXE)));
            tools.add(new Tuple<>(ToolType.PICKAXE, new ItemStack(Items.WOODEN_PICKAXE)));
        }
        return tools;
    }

    /**
     * Checks if a certain block is a pathBlock (roadBlock).
     *
     * @param block the block to analyze.
     * @return true if is so.
     */
    public static boolean isPathBlock(final Block block)
    {
        return ModTags.pathingBlocks.contains(block);
    }

    /**
     * {@link WorkerUtil#isWorkerAtSiteWithMove(AbstractEntityCitizen, int, int, int, int)}.
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
     * Checks if a worker is at his working site. If he isn't, sets it's path to the location.
     *
     * @param worker Worker to check
     * @param x      X-coordinate
     * @param y      Y-coordinate
     * @param z      Z-coordinate
     * @param range  Range to check in
     * @return True if worker is at site, otherwise false.
     */
    public static boolean isWorkerAtSiteWithMove(@NotNull final AbstractEntityCitizen worker, final int x, final int y, final int z, final int range)
    {
        if (!EntityUtils.isLivingAtSiteWithMove(worker, x, y, z, range))
        {
            //If not moving the try setting the point where the entity should move to
            if (worker.getNavigation().isDone() && !EntityUtils.tryMoveLivingToXYZ(worker, x, y, z))
            {
                worker.getCitizenStatusHandler().setStatus(Status.PATHFINDING_ERROR);
            }
            return false;
        }
        return true;
    }

    /**
     * Recalls the citizen, notifies player if not successful.
     *
     * @param spawnPoint the spawnPoint.
     * @param citizen    the citizen.
     * @return true if successful.
     */
    public static boolean setSpawnPoint(@Nullable final BlockPos spawnPoint, @NotNull final AbstractEntityCitizen citizen)
    {
        if (spawnPoint == null)
        {
            return false;
        }

        citizen.moveTo(
          spawnPoint.getX() + MIDDLE_BLOCK_OFFSET,
          spawnPoint.getY(),
          spawnPoint.getZ() + MIDDLE_BLOCK_OFFSET,
          citizen.getRotationYaw(),
          citizen.getRotationPitch());
        citizen.getNavigation().stop();
        return true;
    }

    /**
     * Get a Tooltype for a certain block. We need this because minecraft has a lot of blocks which have strange or no required tool.
     *
     * @param state         the target BlockState.
     * @param blockHardness the hardness.
     * @return the toolType to use.
     */
    public static IToolType getBestToolForBlock(final BlockState state, float blockHardness)
    {
        final net.minecraftforge.common.ToolType forgeTool = state.getHarvestTool();

        String toolName = "";
        if (forgeTool == null)
        {
            if (blockHardness > 0f)
            {
                for (final Tuple<ToolType, ItemStack> tool : getOrInitTestTools())
                {
                    if (tool.getB() != null && tool.getB().getItem() instanceof ToolItem)
                    {
                        final ToolItem toolItem = (ToolItem) tool.getB().getItem();
                        if (tool.getB().getDestroySpeed(state) >= toolItem.getTier().getSpeed())
                        {
                            toolName = tool.getA().getName();
                            break;
                        }
                    }
                }
            }
        }
        else
        {
            toolName = forgeTool.getName();
        }

        final IToolType toolType = ToolType.getToolType(toolName);

        if (toolType == ToolType.NONE && state.getMaterial() == Material.WOOD)
        {
            return ToolType.AXE;
        }
        else if (state.getBlock() instanceof GlazedTerracottaBlock)
        {
            return ToolType.PICKAXE;
        }
        return toolType;
    }

    /**
     * Get the correct havestlevel for a certain block. We need this because minecraft has a lot of blocks which have strange or no required harvestlevel.
     *
     * @param target the target block.
     * @return the required harvestLevel.
     */
    public static int getCorrectHarvestLevelForBlock(final BlockState target)
    {
        final int required = target.getHarvestLevel();

        if ((required < 0 && target.getMaterial() == Material.WOOD)
              || target.getBlock() instanceof GlazedTerracottaBlock)
        {
            return 0;
        }
        return required;
    }

    /**
     * Change the citizens Rotation to look at said block.
     *
     * @param block   the block he should look at.
     * @param citizen the citizen that shall face the block.
     */
    public static void faceBlock(@Nullable final BlockPos block, final AbstractEntityCitizen citizen)
    {
        if (block == null)
        {
            return;
        }

        final double xDifference = block.getX() - citizen.blockPosition().getX();
        final double zDifference = block.getZ() - citizen.blockPosition().getZ();
        final double yDifference = block.getY() - (citizen.blockPosition().getY() + citizen.getEyeHeight());

        final double squareDifference = Math.sqrt(xDifference * xDifference + zDifference * zDifference);
        final double intendedRotationYaw = (Math.atan2(zDifference, xDifference) * 180.0D / Math.PI) - 90.0;
        final double intendedRotationPitch = -(Math.atan2(yDifference, squareDifference) * 180.0D / Math.PI);
        citizen.setOwnRotation((float) EntityUtils.updateRotation(citizen.getRotationYaw(), intendedRotationYaw, ROTATION_MOVEMENT),
          (float) EntityUtils.updateRotation(citizen.getRotationPitch(), intendedRotationPitch, ROTATION_MOVEMENT));

        final double goToX = xDifference > 0 ? MOVE_MINIMAL : -MOVE_MINIMAL;
        final double goToZ = zDifference > 0 ? MOVE_MINIMAL : -MOVE_MINIMAL;

        //Have to move the entity minimally into the direction to render his new rotation.
        citizen.move(MoverType.SELF, new Vector3d((float) goToX, 0, (float) goToZ));
    }

    /**
     * Find the first level in a structure and return it.
     *
     * @param structure the structure to scan.
     * @return the position of the sign.
     */
    @Nullable
    public static BlockPos findFirstLevelSign(final Blueprint structure, final BlockPos pos)
    {
        for (int j = 0; j < structure.getSizeY(); j++)
        {
            for (int k = 0; k < structure.getSizeZ(); k++)
            {
                for (int i = 0; i < structure.getSizeX(); i++)
                {
                    @NotNull final BlockPos localPos = new BlockPos(i, j, k);
                    final BlockInfo te = structure.getBlockInfoAsMap().get(localPos);
                    if (te != null)
                    {
                        final CompoundNBT teData = te.getTileEntityData();
                        if (teData != null && teData.getString(LEVEL_SIGN_FIRST_ROW).equals(LEVEL_SIGN_TEXT))
                        {
                            // try to make an anchor in 0,0,0 instead of the middle of the structure
                            return pos.subtract(structure.getPrimaryBlockOffset()).offset(localPos);
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Updated the level sign of a certain level in the world.
     *
     * @param world   the world.
     * @param level   the level to update.
     * @param levelId the id of the level.
     */
    public static void updateLevelSign(final World world, final Level level, final int levelId)
    {
        @Nullable final BlockPos levelSignPos = level.getLevelSign();

        if (levelSignPos != null)
        {
            final TileEntity te = world.getBlockEntity(levelSignPos);

            if (te instanceof SignTileEntity)
            {
                final BlockState BlockState = world.getBlockState(levelSignPos);
                final SignTileEntity teLevelSign = (SignTileEntity) te;

                teLevelSign.setMessage(0, new StringTextComponent(TextFormatting.stripFormatting(
                  LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.minerMineNode") + ": " + levelId)));
                teLevelSign.setMessage(1, new StringTextComponent(TextFormatting.stripFormatting("Y: " + (level.getDepth() + 1))));
                teLevelSign.setMessage(2, new StringTextComponent(TextFormatting.stripFormatting(
                  LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.minerNode") + ": " + level.getNumberOfBuiltNodes())));
                teLevelSign.setMessage(3, new StringTextComponent(TextFormatting.stripFormatting("")));

                teLevelSign.setChanged();
                world.sendBlockUpdated(levelSignPos, BlockState, BlockState, 3);
            }
        }
    }

    /**
     * Check if there is any already composted land.
     *
     * @param buildingFlorist the building to check.
     * @param world           the world to check it for.
     * @return true if there is any.
     */
    public static boolean isThereCompostedLand(final BuildingFlorist buildingFlorist, final World world)
    {
        for (final BlockPos pos : buildingFlorist.getPlantGround())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final TileEntity entity = world.getBlockEntity(pos);
                if (entity instanceof TileEntityCompostedDirt)
                {
                    if (((TileEntityCompostedDirt) entity).isComposted())
                    {
                        return true;
                    }
                }
                else
                {
                    buildingFlorist.removePlantableGround(pos);
                }
            }
        }
        return false;
    }

    /**
     * Find the last ladder by iterating over the y pos in the world.
     *
     * @param pos   the starting pos.
     * @param world the world.
     * @return the y of the last one.
     */
    public static int getLastLadder(@NotNull final BlockPos pos, final World world)
    {
        if (world.getBlockState(pos).getBlock().isLadder(world.getBlockState(pos), world, pos, null))
        {
            return getLastLadder(pos.below(), world);
        }
        else
        {
            return pos.getY() + 1;
        }
    }
}
