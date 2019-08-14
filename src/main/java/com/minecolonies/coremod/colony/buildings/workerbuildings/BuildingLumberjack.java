package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.coremod.client.gui.WindowHutLumberjack;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractFilterableListBuilding;
import com.minecolonies.coremod.colony.buildings.views.AbstractFilterableListsView;
import com.minecolonies.coremod.colony.jobs.JobLumberjack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

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
     * Whether or not the LJ should replant saplings
     */
    private boolean replant = true;

    /**
     * The maximum upgrade of the building.
     */
    private static final int    MAX_BUILDING_LEVEL = 5;
    /**
     * The job description.
     */
    private static final String LUMBERJACK         = "lumberjack";

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
            final int invSIze = getMainCitizen().getInventory().getSlots();
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
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);

        if (compound.keySet().contains(TAG_REPLANT))
        {
            replant = compound.getBoolean(TAG_REPLANT);
        }
        else
        {
            replant = true;
        }
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();

        compound.putBoolean(TAG_REPLANT, replant);

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
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        super.serializeToView(buf);
        buf.writeBoolean(replant);
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
     * Provides a view of the lumberjack building class.
     */
    public static class View extends AbstractFilterableListsView
    {
        /**
         * Whether or not the LJ should replant saplings
         */
        public boolean shouldReplant = true;

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
        public void deserialize(@NotNull final PacketBuffer buf)
        {
            super.deserialize(buf);
            shouldReplant = buf.readBoolean();
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
