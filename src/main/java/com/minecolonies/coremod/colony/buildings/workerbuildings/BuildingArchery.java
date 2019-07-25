package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobArcherTraining;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Building class for the Archery.
 */
public class BuildingArchery extends AbstractBuildingWorker
{
    /**
     * The Schematic name.
     */
    private static final String SCHEMATIC_NAME = "Archery";

    /**
     * The Schematic name.
     */
    private static final String DESC = "Archery";

    /**
     * List of shooting stands in the building.
     */
    private final List<BlockPos> shootingStands = new ArrayList<>();

    /**
     * List of shooting targets in the building.
     */
    private final List<BlockPos> shootingTargets = new ArrayList<>();

    /**
     * The abstract constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public BuildingArchery(@NotNull final Colony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public AbstractJob createJob(final CitizenData citizen)
    {
        return new JobArcherTraining(citizen);
    }

    @Override
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        if (block == Blocks.HAY_BLOCK && world.getBlockState(pos.down()).getBlock() instanceof BlockFence)
        {
            shootingTargets.add(pos);
        }
        else if (block == Blocks.GLOWSTONE)
        {
            shootingStands.add(pos);
        }
        super.registerBlockPosition(block, pos, world);
    }

    @Override
    public void readFromNBT(@NotNull final CompoundNBT compound)
    {
        super.readFromNBT(compound);

        shootingTargets.clear();
        shootingStands.clear();

        final ListNBT targetList = compound.getList(TAG_ARCHERY_TARGETS, Constants.NBT.TAG_COMPOUND);
        shootingTargets.addAll(NBTUtils.streamCompound(targetList).map(targetCompound -> BlockPosUtil.readFromNBT(targetCompound, TAG_TARGET)).collect(Collectors.toList()));

        final ListNBT standTagList = compound.getList(TAG_ARCHERY_STANDS, Constants.NBT.TAG_COMPOUND);
        shootingStands.addAll(NBTUtils.streamCompound(standTagList).map(targetCompound -> BlockPosUtil.readFromNBT(targetCompound, TAG_STAND)).collect(Collectors.toList()));
    }

    @Override
    public void writeToNBT(@NotNull final CompoundNBT compound)
    {
        super.writeToNBT(compound);

        final ListNBT targetList = shootingTargets.stream().map(target -> BlockPosUtil.writeToNBT(new CompoundNBT(), TAG_TARGET, target)).collect(NBTUtils.toListNBT());
        compound.put(TAG_ARCHERY_TARGETS, targetList);

        final ListNBT standTagList = shootingStands.stream().map(target -> BlockPosUtil.writeToNBT(new CompoundNBT(), TAG_STAND, target)).collect(NBTUtils.toListNBT());
        compound.put(TAG_ARCHERY_STANDS, standTagList);
    }

    @Override
    public String getSchematicName()
    {
        return SCHEMATIC_NAME;
    }

    @SuppressWarnings("squid:S109")
    @Override
    public int getMaxBuildingLevel()
    {
        return 5;
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return "archer";
    }

    @Override
    public int getMaxInhabitants()
    {
        return getBuildingLevel();
    }

    /**
     * Get a random position to shoot from.
     *
     * @param random the random obj.
     * @return a random shooting stand position.
     */
    public BlockPos getRandomShootingStandPosition(final Random random)
    {
        if (!shootingStands.isEmpty())
        {
            return shootingStands.get(random.nextInt(shootingStands.size()));
        }
        return null;
    }

    /**
     * Get a random position to shoot at.
     *
     * @param random the random obj.
     * @return a random shooting target position.
     */
    public BlockPos getRandomShootingTarget(final Random random)
    {
        if (!shootingTargets.isEmpty())
        {
            return shootingTargets.get(random.nextInt(shootingTargets.size()));
        }
        return null;
    }

    /**
     * The client view for the baker building.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        /**
         * The client view constructor for the AbstractGuardBuilding.
         *
         * @param c the colony.
         * @param l the location.
         */
        public View(final ColonyView c, @NotNull final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutWorkerPlaceholder<>(this, DESC);
        }

        /**
         * Check if it has enough workers.
         *
         * @return true if so.
         */
        @Override
        public boolean hasEnoughWorkers()
        {
            return getWorkerId().size() >= getBuildingLevel();
        }

        @NotNull
        @Override
        public Skill getPrimarySkill()
        {
            return Skill.INTELLIGENCE;
        }

        @NotNull
        @Override
        public Skill getSecondarySkill()
        {
            return Skill.STRENGTH;
        }
    }
}
