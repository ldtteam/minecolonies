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
import com.minecolonies.coremod.client.gui.WindowHutLumberjack;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractFilterableListBuilding;
import com.minecolonies.coremod.colony.buildings.views.AbstractFilterableListsView;
import com.minecolonies.coremod.colony.jobs.JobLumberjack;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.ColonyConstants.NUM_ACHIEVEMENT_FIRST;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * The lumberjacks building.
 */
public class BuildingLumberjack extends AbstractFilterableListBuilding
{
    /**
     * NBT tag if the lj should replant saplings
     */
    private static final String TAG_REPLANT = "shouldReplant";

    /**
     * NBT tag for lj restriction start
     */
    private static final String TAG_RESTRICT_START = "startRestrictionPosition";

    /**
     * NBT tag for lj restriction end
     */
    private static final String TAG_RESTRICT_END = "endRestrictionPosition";

    /**
     * Nbt tag for restriction setting.
     */
    private static final String TAG_RESTRICT = "restrict";

    /**
     * Whether or not the LJ should replant saplings
     */
    private boolean replant = true;

    /**
     * Whether or not the LJ should be restricted
     */
    private boolean restrict = false;

    /**
     * The start position of the restricted area.
     */
    private BlockPos startRestriction = null;

    /**
     * The end position of the restricted area.
     */
    private BlockPos endRestriction = null;


    /**
     * The maximum upgrade of the building.
     */
    private static final int    MAX_BUILDING_LEVEL = 5;
    /**
     * The job description.
     */
    private static final String LUMBERJACK         = "Lumberjack";

    /**
     * Public constructor of the building, creates an object of the building.
     *
     * @param c the colony.
     * @param l the position.
     */
    public BuildingLumberjack(final IColony c, final BlockPos l)
    {
        super(c, l);

        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.AXE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
    }

    @Override
    public Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> getRequiredItemsAndAmount()
    {
        final Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> toKeep = new HashMap<>(super.getRequiredItemsAndAmount());

        if(getMainCitizen() != null && getMainCitizen().getInventory() != null)
        {
            final int invSIze = getMainCitizen().getInventory().getSizeInventory();
            int keptStacks = 0;
            for (int i = 0; i < invSIze; i++)
            {
                final ItemStack stack = getMainCitizen().getInventory().getStackInSlot(i);

                if (ItemStackUtils.isEmpty(stack) || !ItemStackUtils.isStackSapling(stack))
                {
                    continue;
                }

                boolean isAlreadyInList = false;
                for (final Map.Entry<Predicate<ItemStack>, Tuple<Integer, Boolean>> entry : toKeep.entrySet())
                {
                    if (entry.getKey().test(stack))
                    {
                        isAlreadyInList = true;
                    }
                }

                if (!isAlreadyInList)
                {
                    toKeep.put(stack::isItemEqual, new Tuple<>(com.minecolonies.api.util.constant.Constants.STACKSIZE, true));
                    keptStacks++;

                    if (keptStacks >= getMaxBuildingLevel() * 2)
                    {
                        return toKeep;
                    }
                }
            }
        }

        return toKeep;
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.lumberjack;
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
        return LUMBERJACK;
    }

    /**
     * Getter of the max building level.
     *
     * @return the integer.
     */
    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
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
            this.getColony().getStatsManager().triggerAchievement(ModAchievements.achievementBuildingLumberjack);
        }
        if (newLevel >= this.getMaxBuildingLevel())
        {
            this.getColony().getStatsManager().triggerAchievement(ModAchievements.achievementUpgradeLumberjackMax);
        }
    }

    /**
     * Create the job for the lumberjack.
     *
     * @param citizen the citizen to take the job.
     * @return the new job.
     */
    @NotNull
    @Override
    public IJob createJob(final ICitizenData citizen)
    {
        return new JobLumberjack(citizen);
    }

    @Override
    public void deserializeNBT(final NBTTagCompound compound)
    {
        super.deserializeNBT(compound);

        if (compound.hasKey(TAG_REPLANT))
        {
            replant = compound.getBoolean(TAG_REPLANT);
        }
        else
        {
            replant = true;
        }

        if (compound.hasKey(TAG_RESTRICT))
        {
            restrict = compound.getBoolean(TAG_RESTRICT);
        }

        if (compound.hasKey(TAG_RESTRICT_START))
        {
            startRestriction = NBTUtil.getPosFromTag(compound.getCompoundTag(TAG_RESTRICT_START));
        }
        else
        {
            startRestriction = null;
        }

        if (compound.hasKey(TAG_RESTRICT_END))
        {
            endRestriction = NBTUtil.getPosFromTag(compound.getCompoundTag(TAG_RESTRICT_END));
        }
        else
        {
            endRestriction = null;
        }
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        final NBTTagCompound compound = super.serializeNBT();

        compound.setBoolean(TAG_REPLANT, replant);

        if (startRestriction != null)
        {
            compound.setTag(TAG_RESTRICT_START, NBTUtil.createPosTag(startRestriction));
        }

        if (endRestriction != null)
        {
            compound.setTag(TAG_RESTRICT_END, NBTUtil.createPosTag(endRestriction));
        }

        compound.setBoolean(TAG_RESTRICT, restrict);
        return compound;
    }

    /**
     * Getter of the job description.
     *
     * @return the description of the lumberjacks job.
     */
    @NotNull
    @Override
    public String getJobName()
    {
        return LUMBERJACK;
    }

    @Override
    public void serializeToView(@NotNull final ByteBuf buf)
    {
        super.serializeToView(buf);
        buf.writeBoolean(replant);
        buf.writeBoolean(restrict);
    }

    /**
     * Whether or not the LJ should replant saplings.
     */
    public boolean shouldReplant()
    {
        return replant;
    }

    /**
     * Set whether or not LJ should replant saplings
     * @param shouldReplant whether or not the LJ should replant
     */
    public void setShouldReplant(final boolean shouldReplant)
    {
        this.replant = shouldReplant;
        markDirty();
    }

    /**
     * Whether or not the LJ should be restricted.
     */
    public boolean shouldRestrict()
    {
        if (restrict)
        {
            if (startRestriction == null || endRestriction == null)
            {
                restrict = false;
                markDirty();
            }
        }
        return restrict;
    }

    /**
     * Set whether or not LJ should replant saplings
     * @param shouldRestrict whether or not the LJ should be restricted
     */
    public void setShouldRestrict(final boolean shouldRestrict)
    {
        this.restrict = shouldRestrict;
        markDirty();
    }

    public void setRestrictedArea(final BlockPos startPosition, final BlockPos endPosition)
    {
        this.startRestriction = startPosition;
        this.endRestriction = endPosition;
    }

    public BlockPos getStartRestriction()
    {
        return this.startRestriction;
    }

    public BlockPos getEndRestriction()
    {
        return this.endRestriction;
    }

    /**
     * Provides a view of the lumberjack building class.
     */
    public static class View extends AbstractFilterableListsView
    {
        /**
         * Whether or not the LJ should replant saplings
         */
        public boolean shouldReplant = true;

        /**
         * Whether or not the LJ should be restricted
         */
        public boolean shouldRestrict = false;

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

        @Override
        public void deserialize(@NotNull final ByteBuf buf)
        {
            super.deserialize(buf);
            shouldReplant = buf.readBoolean();
            shouldRestrict = buf.readBoolean();
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
            return Skill.CHARISMA;
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutLumberjack(this);
        }
    }
}
