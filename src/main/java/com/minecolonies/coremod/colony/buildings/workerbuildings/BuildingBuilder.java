package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.blocks.BlockMinecoloniesRack;
import com.minecolonies.coremod.client.gui.WindowHutBuilder;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingBuilderView;
import com.minecolonies.coremod.colony.jobs.JobBuilder;
import com.minecolonies.coremod.colony.workorders.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.ColonyConstants.NUM_ACHIEVEMENT_FIRST;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * The builders building.
 */
public class BuildingBuilder extends AbstractBuildingStructureBuilder
{
    /**
     * The job description.
     */
    private static final String BUILDER     = "Builder";

    /**
     * NBT tag to store if mobs already got purged.
     */
    private static final String TAG_PURGE_MOBS = "purgedMobs";

    /**
     * Check if the builder purged mobs already at this day.
     */
    private boolean purgedMobsToday = false;

    /**
     * Public constructor of the building, creates an object of the building.
     *
     * @param c the colony.
     * @param l the position.
     */
    public BuildingBuilder(final IColony c, final BlockPos l)
    {
        super(c, l);

        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.PICKAXE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.SHOVEL, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.AXE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
    }

    /**
     * Getter of the schematic name.
     *
     * @return the schematic name.
     */
    @NotNull
    @Override
    public String getSchematicName()
    {
        return BUILDER;
    }

    /**
     * @see AbstractBuilding#onUpgradeComplete(int)
     */
    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);

        if (newLevel == NUM_ACHIEVEMENT_FIRST)
        {
            this.getColony().getStatsManager().triggerAchievement(ModAchievements.achievementBuildingBuilder);
        }
        if (newLevel >= this.getMaxBuildingLevel())
        {
            this.getColony().getStatsManager().triggerAchievement(ModAchievements.achievementUpgradeBuilderMax);
        }
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.builder;
    }

    @Override
    public void onWakeUp()
    {
        this.purgedMobsToday = false;
    }

    @Override
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        //Only the chests and racks because he shouldn't fill up the furnaces.
        if (block instanceof BlockChest || block instanceof BlockMinecoloniesRack)
        {
            addContainerPosition(pos);
        }
    }

    @Override
    public void deserializeNBT(final NBTTagCompound compound)
    {
        super.deserializeNBT(compound);
        this.purgedMobsToday = compound.getBoolean(TAG_PURGE_MOBS);
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        final NBTTagCompound compound = super.serializeNBT();
        compound.setBoolean(TAG_PURGE_MOBS, this.purgedMobsToday);

        return compound;
    }

    /**
     * Set if mobs have been purged by this builder at his hut already today.
     * @param purgedMobsToday true if so.
     */
    public void setPurgedMobsToday(final boolean purgedMobsToday)
    {
        this.purgedMobsToday = purgedMobsToday;
    }

    /**
     * Check if the builder has purged the mobs already.
     * @return true if so.
     */
    public boolean hasPurgedMobsToday()
    {
        return purgedMobsToday;
    }

    /**
     * Create the job for the builder.
     *
     * @param citizen the citizen to take the job.
     * @return the new job.
     */
    @NotNull
    @Override
    public IJob createJob(final ICitizenData citizen)
    {
        return new JobBuilder(citizen);
    }

    /**
     * Getter of the job description.
     *
     * @return the description of the builder job.
     */
    @NotNull
    @Override
    public String getJobName()
    {
        return BUILDER;
    }

    @Override
    public void searchWorkOrder()
    {
        final ICitizenData citizen = getMainCitizen();
        if (citizen == null)
        {
            return;
        }

        final List<WorkOrderBuildDecoration> list = new ArrayList<>();
        list.addAll(getColony().getWorkManager().getOrderedList(WorkOrderBuildRemoval.class, getPosition()));
        // WorkOrderBuildDecoration is the superclass of BuildBuilding and thus returns both
        list.addAll(getColony().getWorkManager().getOrderedList(WorkOrderBuildDecoration.class, getPosition()));
        list.removeIf(order -> order instanceof WorkOrderBuildMiner);

        final WorkOrderBuildDecoration order = list.stream().filter(w -> w.getClaimedBy() != null && w.getClaimedBy().equals(getPosition())).findFirst().orElse(null);
        if (order != null)
        {
            citizen.getJob(JobBuilder.class).setWorkOrder(order);
            order.setClaimedBy(citizen);
            return;
        }

        for (final WorkOrderBuildDecoration wo: list)
        {
            double distanceToBuilder = Double.MAX_VALUE;

            if (wo instanceof WorkOrderBuild && !((WorkOrderBuild) wo).canBuild(citizen))
            {
                continue;
            }

            for (@NotNull final ICitizenData otherBuilder : getColony().getCitizenManager().getCitizens())
            {
                final JobBuilder job = otherBuilder.getJob(JobBuilder.class);

                if (job == null || otherBuilder.getWorkBuilding() == null || citizen.getId() == otherBuilder.getId())
                {
                    continue;
                }

                if (!job.hasWorkOrder() && wo instanceof WorkOrderBuild && ((WorkOrderBuild) wo).canBuild(otherBuilder))
                {
                    final double distance = otherBuilder.getWorkBuilding().getID().distanceSq(wo.getBuildingLocation());
                    if (distance < distanceToBuilder)
                    {
                        distanceToBuilder = distance;
                    }
                }
            }

            if (citizen.getWorkBuilding().getID().distanceSq(wo.getBuildingLocation()) < distanceToBuilder)
            {
                citizen.getJob(JobBuilder.class).setWorkOrder(wo);
                wo.setClaimedBy(citizen);
                return;
            }
        }
    }

    @Override
    public boolean canBeBuiltByBuilder(final int newLevel)
    {
        return getBuildingLevel() + 1 == newLevel;
    }

    /**
     * Provides a view of the miner building class.
     */
    public static class View extends AbstractBuildingBuilderView
    {
        /**
         * Public constructor of the view, creates an instance of it.
         *
         * @param c the colony.
         * @param l the position.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutBuilder(this);
        }

        @NotNull
        @Override
        public Skill getPrimarySkill()
        {
            return Skill.STRENGTH;
        }

        @NotNull
        @Override
        public Skill getSecondarySkill()
        {
            return Skill.ENDURANCE;
        }
    }
}
