package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.colony.workorders.WorkOrderView;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.blocks.BlockMinecoloniesRack;
import com.minecolonies.coremod.client.gui.WindowHutBuilder;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingBuilderView;
import com.minecolonies.coremod.colony.jobs.JobBuilder;
import com.minecolonies.coremod.colony.workorders.*;
import com.minecolonies.coremod.network.messages.server.colony.building.builder.BuilderSetManualModeMessage;
import net.minecraft.block.Block;
import net.minecraft.block.ChestBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_PURGED_MOBS;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_MANUAL_JOB_SELECTION;

/**
 * The builders building.
 */
public class BuildingBuilder extends AbstractBuildingStructureBuilder
{
    /**
     * The job description.
     */
    private static final String BUILDER = "builder";

    /**
     * Check if the builder purged mobs already at this day.
     */
    private boolean purgedMobsToday = false;

    /**
     * Whether the builder should accept build orders automatically.
     */
    private boolean manualMode = false;

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
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.HOE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
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
        if (block instanceof ChestBlock || block instanceof BlockMinecoloniesRack)
        {
            addContainerPosition(pos);
        }
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        this.purgedMobsToday = compound.getBoolean(TAG_PURGED_MOBS);
        this.manualMode = compound.getBoolean(TAG_MANUAL_JOB_SELECTION);
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();
        compound.putBoolean(TAG_PURGED_MOBS, this.purgedMobsToday);
        compound.putBoolean(TAG_MANUAL_JOB_SELECTION, manualMode);

        return compound;
    }

    @Override
    public void serializeToView(@NotNull PacketBuffer buf)
    {
        super.serializeToView(buf);
        buf.writeBoolean(manualMode);

        if (manualMode && getMainCitizen() != null && !getMainCitizen().getJob(JobBuilder.class).hasWorkOrder())
        {
            final List<WorkOrderBuildDecoration> list = new ArrayList<>();
            list.addAll(getColony().getWorkManager().getOrderedList(WorkOrderBuildRemoval.class, getPosition()));
            // WorkOrderBuildDecoration is the superclass of BuildBuilding and thus returns both
            list.addAll(getColony().getWorkManager().getOrderedList(WorkOrderBuildDecoration.class, getPosition()));

            list.removeIf(order -> order instanceof WorkOrderBuildMiner);
            list.removeIf(order -> order.isClaimed() && !order.getClaimedBy().equals(getPosition()));
            list.removeIf(order -> order instanceof WorkOrderBuild && !(order instanceof WorkOrderBuildRemoval) &&
                  !((WorkOrderBuild) order).canBuildIngoringDistance(getMainCitizen()));

            buf.writeInt(list.size());

            for (WorkOrderBuildDecoration order : list)
            {
                order.serializeViewNetworkData(buf);
            }
        }
        else if (manualMode)
        {
            buf.writeInt(0);
        }
    }

    /**
     * Set if mobs have been purged by this builder at his hut already today.
     *
     * @param purgedMobsToday true if so.
     */
    public void setPurgedMobsToday(final boolean purgedMobsToday)
    {
        this.purgedMobsToday = purgedMobsToday;
    }

    /**
     * Check if the builder has purged the mobs already.
     *
     * @return true if so.
     */
    public boolean hasPurgedMobsToday()
    {
        return purgedMobsToday;
    }

    /**
     * Set whether the builder should automatically accept build orders.
     * 
     * @param manualMode false if he should.
     */
    public void setManualMode(boolean manualMode)
    {
        this.manualMode = manualMode;
    }

    /**
     * Checks whether the builder should automatically accept build orders.
     * 
     * @return false if he should.
     */
    public boolean getManualMode()
    {
        return manualMode;
    }

    /**
     * Create the job for the builder.
     *
     * @param citizen the citizen to take the job.
     * @return the new job.
     */
    @NotNull
    @Override
    public IJob<?> createJob(final ICitizenData citizen)
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

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Adaptability;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Athletics;
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

        for (final WorkOrderBuildDecoration wo : list)
        {
            double distanceToBuilder = Double.MAX_VALUE;

            if (wo instanceof WorkOrderBuild && !(wo instanceof WorkOrderBuildRemoval) && !((WorkOrderBuild) wo).canBuild(citizen))
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

    /**
     * Sets the work order with the given id as the work order for this buildings citizen.
     * 
     * @param orderId the id of the work order to select.
     */
    public void setWorkOrder(int orderId)
    {
        final ICitizenData citizen = getMainCitizen();
        if (citizen == null)
        {
            return;
        }

        if (citizen.getJob(JobBuilder.class).hasWorkOrder())
        {
            return;
        }

        IWorkOrder wo = getColony().getWorkManager().getWorkOrder(orderId);
        if (wo == null || (wo.getClaimedBy() != null && !wo.getClaimedBy().equals(getPosition())))
        {
            return;
        }

        if (wo instanceof WorkOrderBuildDecoration)
        {
            WorkOrderBuildDecoration bo = (WorkOrderBuildDecoration) wo;
            citizen.getJob(JobBuilder.class).setWorkOrder(bo);
            wo.setClaimedBy(citizen);
            return;
        }
    }

    @Override
    public boolean canBeBuiltByBuilder(final int newLevel)
    {
        return getBuildingLevel() + 1 == newLevel;
    }

    @Override
    public boolean canEat(final ItemStack stack)
    {
        if (requiresResourceForBuilding(stack))
        {
            return false;
        }
        return super.canEat(stack);
    }

    /**
     * Provides a view of the miner building class.
     */
    public static class View extends AbstractBuildingBuilderView
    {
        /**
         * Whether the manual mode of the builder is enabled.
         */
        private boolean manualMode = false;

        /**
         * The work orders to choose from in manual mode.
         */
        final List<WorkOrderView> orders = new ArrayList<>();

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

        /**
         * Checks whether this builder should only accept build orders specifically created for that builder.
         * 
         * @return true if so.
         */
        public boolean getManualMode()
        {
            return manualMode;
        }

        /**
         * Sets whether this builder should only accept build orders specifically created for that builder.
         * 
         * @param manualMode true if so.
         */
        public void setManualMode(boolean manualMode)
        {
            Network.getNetwork().sendToServer(new BuilderSetManualModeMessage(this, manualMode));
            this.manualMode = manualMode;
        }

        /**
         * Gets the available work orders to choose from.
         * 
         * @return the available work orders to choose from.
         */
        public List<WorkOrderView> getBuildOrders()
        {
            return orders;
        }

        @Override
        public void deserialize(@NotNull PacketBuffer buf)
        {
            super.deserialize(buf);
            manualMode = buf.readBoolean();

            if (manualMode) {
                int ordersNr = buf.readInt();
                orders.clear();

                if (ordersNr > 0)
                {
                    for (int i = 0; i < ordersNr; ++i)
                    {
                        orders.add(AbstractWorkOrder.createWorkOrderView(buf));
                    }
                }
            }
        }
    }
}
