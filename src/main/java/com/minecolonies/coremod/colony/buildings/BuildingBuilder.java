package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.client.gui.WindowHutBuilder;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingBuilderView;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobBuilder;
import com.minecolonies.coremod.colony.workorders.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
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
    private static final String BUILDER = "Builder";

    /**
     * Public constructor of the building, creates an object of the building.
     *
     * @param c the colony.
     * @param l the position.
     */
    public BuildingBuilder(final Colony c, final BlockPos l)
    {
        super(c, l);

        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.PICKAXE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), 1);
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.SHOVEL, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), 1);
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.AXE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), 1);
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
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        //Only the chests because he shouldn't fill up the furnaces.
        if (block instanceof BlockChest)
        {
            addContainerPosition(pos);
        }
    }

    /**
     * Create the job for the builder.
     *
     * @param citizen the citizen to take the job.
     * @return the new job.
     */
    @NotNull
    @Override
    public AbstractJob createJob(final CitizenData citizen)
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
        final CitizenData citizen = getMainWorker();
        if (citizen == null)
        {
            return;
        }

        final List<WorkOrderBuildDecoration> list = new ArrayList<>();
        list.addAll(getColony().getWorkManager().getOrderedList(WorkOrderBuildRemoval.class));
        list.addAll(getColony().getWorkManager().getOrderedList(WorkOrderBuildBuilding.class));
        list.addAll(getColony().getWorkManager().getOrderedList(WorkOrderBuildDecoration.class));
        list.removeIf(order -> order instanceof WorkOrderBuildMiner);

        for (final WorkOrderBuildDecoration wo: list)
        {
            double distanceToBuilder = Double.MAX_VALUE;

            if (wo instanceof WorkOrderBuild && !((WorkOrderBuild) wo).canBuild(citizen))
            {
                continue;
            }

            for (@NotNull final CitizenData otherBuilder : getColony().getCitizenManager().getCitizens())
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
        public View(final ColonyView c, final BlockPos l)
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
