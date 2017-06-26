package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.client.gui.WindowHutLumberjack;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobLumberjack;
import com.minecolonies.coremod.entity.ai.item.handling.ItemStorage;
import com.minecolonies.coremod.network.messages.LumberjackSaplingSelectorMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
     * The maximum upgrade of the building.
     */
    private static final int    MAX_BUILDING_LEVEL = 5;
    /**
     * The job description.
     */
    private static final String LUMBERJACK         = "Lumberjack";

    /**
     * Sets the amount of saplings the lumberjack should keep.
     */
    private static final int SAPLINGS_TO_KEEP = 32;

    /**
     * List of the items the lumberjack has to keep.
     */
    private final Map<ItemStorage, Integer> keepX = new HashMap<>();

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

        final ItemStack stack = new ItemStack(Blocks.SAPLING);
        keepX.put(new ItemStorage(stack, false), SAPLINGS_TO_KEEP);
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

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        if(treesToFell.isEmpty())
        {
            super.readFromNBT(compound);
            treesToFell.clear();

            final NBTTagList saplingTagList = compound.getTagList(TAG_SAPLINGS, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < saplingTagList.tagCount(); ++i)
            {
                final NBTTagCompound saplingCompound = saplingTagList.getCompoundTagAt(i);
                final ItemStack stack = ItemStack.loadItemStackFromNBT(saplingCompound);
                final boolean cut = saplingCompound.getBoolean(TAG_CUT);
                treesToFell.put(new ItemStorage(stack), cut);
            }
        }
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
            this.getColony().triggerAchievement(ModAchievements.achievementBuildingLumberjack);
        }
        if (newLevel >= this.getMaxBuildingLevel())
        {
            this.getColony().triggerAchievement(ModAchievements.achievementUpgradeLumberjackMax);
        }
    }

    /**
     * Override this method if you want to keep an amount of items in inventory.
     * When the inventory is full, everything get's dumped into the building chest.
     * But you can use this method to hold some stacks back.
     *
     * @return a list of objects which should be kept.
     */
    @Override
    public Map<ItemStorage, Integer> getRequiredItemsAndAmount()
    {
        return keepX;
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

    /**
     * Override this method if you want to keep some items in inventory.
     * When the inventory is full, everything get's dumped into the building chest.
     * But you can use this method to hold some stacks back.
     *
     * @param stack the stack to decide on
     * @return true if the stack should remain in inventory
     */
    @Override
    public boolean neededForWorker(@Nullable final ItemStack stack)
    {
        return ItemStackUtils.hasToolLevel(stack, ToolType.AXE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel());
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
                final boolean cut = buf.readBoolean();
                treesToFell.put(new ItemStorage(stack), cut);
            }

            if(treesToFell.isEmpty())
            {
                final List<ItemStack> saplings = new ArrayList<>();
                final int[] saplingId = OreDictionary.getOreIDs(new ItemStack(Blocks.SAPLING));

                for (final int i : saplingId)
                {
                    saplings.addAll(OreDictionary.getOres(OreDictionary.getOreName(i)));
                }
                treesToFell.putAll(calcSaplings(saplings));

                for(final Map.Entry<ItemStorage, Boolean> entry : treesToFell.entrySet())
                {
                    MineColonies.getNetwork().sendToServer(new LumberjackSaplingSelectorMessage(this, entry.getKey().getItemStack(), entry.getValue()));
                }
            }
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutLumberjack(this);
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

        /**
         * Calculates all saplings ingame and return an itemStorage map of it.
         * @param saplings the saplings.
         * @return the itemStorage map.
         */
        public static Map<ItemStorage, Boolean> calcSaplings(final List<ItemStack> saplings)
        {
            final Map<ItemStorage, Boolean> finalSaplings = new LinkedHashMap<>();
            for (final ItemStack saps : saplings)
            {
                if (saps.getHasSubtypes())
                {
                    final List<ItemStack> list = new ArrayList<>();
                    saps.getItem().getSubItems(saps.getItem(), null, list);

                    for (final ItemStack stack : list)
                    {
                        finalSaplings.put(new ItemStorage(stack), true);
                    }
                }
            }
            return finalSaplings;
        }
    }
}
