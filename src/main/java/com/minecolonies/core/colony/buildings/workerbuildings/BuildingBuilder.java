package com.minecolonies.core.colony.buildings.workerbuildings;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.tools.ModToolTypes;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.huts.WindowHutBuilderModule;
import com.minecolonies.core.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.core.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.core.colony.buildings.modules.settings.BuilderModeSetting;
import com.minecolonies.core.colony.buildings.modules.settings.SettingKey;
import com.minecolonies.core.colony.buildings.modules.settings.StringSetting;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingBuilderView;
import com.minecolonies.core.colony.jobs.JobBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import com.minecolonies.core.colony.workorders.*;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_PURGED_MOBS;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * The builders building.
 */
public class BuildingBuilder extends AbstractBuildingStructureBuilder
{
    /**
     * Settings key for the building mode.
     */
    public static final ISettingKey<StringSetting> MODE = new SettingKey<>(StringSetting.class, new ResourceLocation(Constants.MOD_ID, "mode"));
    public static final ISettingKey<BuilderModeSetting> BUILDING_MODE = new SettingKey<>(BuilderModeSetting.class, new ResourceLocation(Constants.MOD_ID, "buildmode"));

    /**
     * Both setting options.
     */
    public static final String MANUAL_SETTING = "com.minecolonies.core.builder.setting.manual";
    public static final String AUTO_SETTING = "com.minecolonies.core.builder.setting.automatic";

    /**
     * The job description.
     */
    private static final String BUILDER = "builder";

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

        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ModToolTypes.pickaxe.get(), TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ModToolTypes.shovel.get(), TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ModToolTypes.axe.get(), TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ModToolTypes.hoe.get(), TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ModToolTypes.shears.get(), TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
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

    @Override
    public void onWakeUp()
    {
        this.purgedMobsToday = false;
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);
        this.purgedMobsToday = compound.getBoolean(TAG_PURGED_MOBS);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();
        compound.putBoolean(TAG_PURGED_MOBS, this.purgedMobsToday);
        return compound;
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
     * Checks whether the builder should automatically accept build orders.
     *
     * @return false if he should.
     */
    public boolean getManualMode()
    {
        return getSetting(MODE).getValue().equals(MANUAL_SETTING);
    }

    @Override
    public void searchWorkOrder()
    {
        final ICitizenData citizen = getFirstModuleOccurance(WorkerBuildingModule.class).getFirstCitizen();
        if (citizen == null)
        {
            return;
        }

        final List<IWorkOrder> list = getColony().getWorkManager().getOrderedList(wo -> wo.canBeMadeBy(citizen.getJob()), getPosition());
        list.sort((a, b) -> {
            if (a.getWorkOrderType() == WorkOrderType.REMOVE)
            {
                return -1;
            }
            if (b.getWorkOrderType() == WorkOrderType.REMOVE)
            {
                return 1;
            }
            return 0;
        });

        final IWorkOrder order = list.stream().filter(w -> w.getClaimedBy() != null && w.getClaimedBy().equals(getPosition())).findFirst().orElse(null);
        if (order != null)
        {
            citizen.getJob(JobBuilder.class).setWorkOrder(order);
            order.setClaimedBy(citizen);
            return;
        }

        if (getManualMode())
        {
            return;
        }

        for (final IWorkOrder wo : list)
        {
            double distanceToBuilder = Double.MAX_VALUE;

            if (wo instanceof WorkOrderBuilding && wo.getWorkOrderType() != WorkOrderType.REMOVE && !wo.canBuild(citizen))
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

                if (!job.hasWorkOrder() && wo instanceof WorkOrderBuilding && wo.canBuild(otherBuilder))
                {
                    final double distance = otherBuilder.getWorkBuilding().getID().distSqr(wo.getLocation());
                    if (distance < distanceToBuilder)
                    {
                        distanceToBuilder = distance;
                    }
                }
            }

            if (citizen.getWorkBuilding().getID().distSqr(wo.getLocation()) < distanceToBuilder)
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
        final ICitizenData citizen = getFirstModuleOccurance(WorkerBuildingModule.class).getFirstCitizen();
        if (citizen == null)
        {
            return;
        }

        IWorkOrder wo = getColony().getWorkManager().getWorkOrder(orderId);
        if (wo == null || (wo.getClaimedBy() != null && !wo.getClaimedBy().equals(getPosition())))
        {
            return;
        }

        if (citizen.getJob(JobBuilder.class).hasWorkOrder())
        {
            wo.setClaimedBy(citizen);
            getColony().getWorkManager().setDirty(true);
            return;
        }

        if (wo.canBeMadeBy(citizen.getJob()))
        {
            citizen.getJob(JobBuilder.class).setWorkOrder(wo);
            wo.setClaimedBy(citizen);
            getColony().getWorkManager().setDirty(true);
            markDirty();
        }
    }

    @Override
    public boolean canBeBuiltByBuilder(final int newLevel)
    {
        return getBuildingLevel() + 1 == newLevel;
    }

    @Override
    public boolean canAssignCitizens()
    {
        return true;
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
        public BOWindow getWindow()
        {
            return new WindowHutBuilderModule(this);
        }
    }
}
