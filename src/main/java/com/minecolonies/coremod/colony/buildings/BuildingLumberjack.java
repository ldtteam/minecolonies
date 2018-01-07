package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.client.gui.WindowHutLumberjack;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobLumberjack;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.api.crafting.ItemStorage;
import io.netty.buffer.ByteBuf;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Constants.SAPLINGS;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * The lumberjacks building.
 */
public class BuildingLumberjack extends AbstractBuildingWorker
{
    /**
     * NBT tag to store the treesToFell map.
     */
    private static final String TAG_SAPLINGS = "saplings";

    /**
     * NBT tag if the lj should cut a certain type of tree.
     */
    private static final String TAG_CUT = "shouldCut";

    /**
     * A default sapling itemStack.
     */
    private static final ItemStack SAPLING_STACK = new ItemStack(Blocks.SAPLING);

    /**
     * The maximum upgrade of the building.
     */
    private static final int    MAX_BUILDING_LEVEL = 5;
    /**
     * The job description.
     */
    private static final String LUMBERJACK         = "Lumberjack";

    /**
     * List of saplings the lumberjack should, or should not fell (true if should, false if should not).
     */
    private final Map<ItemStorage, Boolean> treesToFell = new LinkedHashMap<>();

    /**
     * Public constructor of the building, creates an object of the building.
     *
     * @param c the colony.
     * @param l the position.
     */
    public BuildingLumberjack(final Colony c, final BlockPos l)
    {
        super(c, l);

        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.AXE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), 1);

        checkTreesToFell();
    }

    @Override
    public Map<Predicate<ItemStack>, Integer> getRequiredItemsAndAmount()
    {
        final Map<Predicate<ItemStack>, Integer> tempKeep = super.getRequiredItemsAndAmount();
        final EntityCitizen mainWorker = getMainWorkerEntity();
        if(mainWorker == null)
        {
            return tempKeep;
        }

        final int invSIze = mainWorker.getInventoryCitizen().getSizeInventory();
        int keptStacks = 0;
        for(int i = 0; i < invSIze; i++)
        {
            final ItemStack stack = mainWorker.getInventoryCitizen().getStackInSlot(i);

            if(ItemStackUtils.isEmpty(stack) || stack.getItem() != SAPLING_STACK.getItem())
            {
                continue;
            }

            boolean isAlreadyInList = false;
            for(Map.Entry<Predicate<ItemStack>, Integer> entry : tempKeep.entrySet())
            {
                if(entry.getKey().test(stack))
                {
                    isAlreadyInList = true;
                }
            }

            if(!isAlreadyInList)
            {
                tempKeep.put(stack::isItemEqual, com.minecolonies.api.util.constant.Constants.STACKSIZE);
                keptStacks++;

                if (keptStacks >= getMaxBuildingLevel() * 2)
                {
                    return tempKeep;
                }
            }
        }

        return tempKeep;
    }

    /**
     * Change a tree to be cut or not.
     *
     * @param stack the stack of the sapling.
     * @param cut   should be cut or not.
     */
    public void setTreeToCut(final ItemStack stack, final boolean cut)
    {
        treesToFell.put(new ItemStorage(stack), cut);
    }

    /**
     * Get a list of what kind of trees the lumberjack should or should not cut.
     *
     * @return the map with ItemStack (sapling) and boolean (should or should not cut).
     */
    public Map<ItemStorage, Boolean> getTreesToCut()
    {
        return Collections.unmodifiableMap(treesToFell);
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

        if (newLevel == 1)
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
    public AbstractJob createJob(final CitizenData citizen)
    {
        return new JobLumberjack(citizen);
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        if (treesToFell.isEmpty())
        {
            final NBTTagList saplingTagList = compound.getTagList(TAG_SAPLINGS, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < saplingTagList.tagCount(); ++i)
            {
                final NBTTagCompound saplingCompound = saplingTagList.getCompoundTagAt(i);
                final ItemStack stack = ItemStack.loadItemStackFromNBT(saplingCompound);
                final boolean cut = saplingCompound.getBoolean(TAG_CUT);
                treesToFell.put(new ItemStorage(stack), cut);
            }
        }
        checkTreesToFell();
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        @NotNull final NBTTagList saplingTagList = new NBTTagList();
        for (@NotNull final Map.Entry<ItemStorage, Boolean> entry : treesToFell.entrySet())
        {
            @NotNull final NBTTagCompound saplingCompound = new NBTTagCompound();
            entry.getKey().getItemStack().writeToNBT(saplingCompound);
            saplingCompound.setBoolean(TAG_CUT, entry.getValue());
            saplingTagList.appendTag(saplingCompound);
        }
        compound.setTag(TAG_SAPLINGS, saplingTagList);
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
        buf.writeInt(treesToFell.size());
        for (final Map.Entry<ItemStorage, Boolean> entry : treesToFell.entrySet())
        {
            ByteBufUtils.writeItemStack(buf, entry.getKey().getItemStack());
            buf.writeBoolean(entry.getValue());
        }
    }

    /**
     * Check and update the treesToFell list.
     */
    private void checkTreesToFell()
    {
        if(treesToFell.size() != ColonyManager.getCompatabilityManager().getCopyOfSaplings().size())
        {
            for(final ItemStorage storage : ColonyManager.getCompatabilityManager().getCopyOfSaplings())
            {
                if(!treesToFell.containsKey(storage))
                {
                    treesToFell.put(storage, true);
                }
            }
        }
    }

    /**
     * Provides a view of the lumberjack building class.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        /**
         * List of saplings the lumberjack should, or should not fell (true if should, false if should not).
         */
        public final Map<ItemStorage, Boolean> treesToFell = new LinkedHashMap<>();

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

        @Override
        public void deserialize(@NotNull final ByteBuf buf)
        {
            super.deserialize(buf);
            treesToFell.clear();
            final int size = buf.readInt();
            for (int i = 0; i < size; i++)
            {
                final ItemStack stack = ByteBufUtils.readItemStack(buf);

                if (stack != null && stack.getItem() != null)
                {
                    final boolean cut = buf.readBoolean();
                    treesToFell.put(new ItemStorage(stack), cut);
                }
            }
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
