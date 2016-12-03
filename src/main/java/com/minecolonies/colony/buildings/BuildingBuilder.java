package com.minecolonies.colony.buildings;

import com.minecolonies.achievements.ModAchievements;
import com.minecolonies.client.gui.WindowHutBuilder;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.AbstractJob;
import com.minecolonies.colony.jobs.JobBuilder;
import com.minecolonies.entity.ai.citizen.miner.Level;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * The builders building.
 */
public class BuildingBuilder extends AbstractBuildingWorker
{
    /**
     * The maximum upgrade of the building.
     */
    public static final int    MAX_BUILDING_LEVEL = 5;
    /**
     * The job description.
     */
    private static final String BUILDER           = "Builder";

    /**
     * Tags to store the needed resourced to nbt.
     */
    private static final String TAG_RESOURCE_LIST = "resources";
    private static final String TAG_AMOUNT        = "amount";

    /**
     * Contains all resources needed for a certain build.
     */
    private HashMap<Block, Integer> neededResources = new HashMap<>();

    /**
     * Public constructor of the building, creates an object of the building.
     *
     * @param c the colony.
     * @param l the position.
     */
    public BuildingBuilder(final Colony c, final BlockPos l)
    {
        super(c, l);
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
            this.getColony().triggerAchievement(ModAchievements.achievementBuildingBuilder);
        }
        if (newLevel >= this.getMaxBuildingLevel())
        {
            this.getColony().triggerAchievement(ModAchievements.achievementUpgradeBuilderMax);
        }
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
     * Get the needed resources for the current build.
     * @return a new Hashmap.
     */
    public Map<Block, Integer> getNeededResources()
    {
        return new HashMap<>(neededResources);
    }

    /**
     * Add a new resource to the needed list.
     * @param res the resource.
     * @param amount the amount.
     */
    public void addNeededResource(Block res, int amount)
    {
        int preAmount = 0;
        if(this.neededResources.containsKey(res))
        {
            preAmount = this.neededResources.get(res);
        }
        this.neededResources.put(res, preAmount + amount);
        this.markDirty();
    }

    /**
     * Reduce a resource of the needed list.
     * @param res the resource.
     * @param amount the amount.
     */
    public void reduceNeededResource(Block res, int amount)
    {
        int preAmount = 0;
        if(this.neededResources.containsKey(res))
        {
            preAmount = this.neededResources.get(res);
        }

        if(preAmount - amount <= 0)
        {
            this.neededResources.remove(res);
        }
        else
        {
            this.neededResources.put(res, preAmount - amount);
        }
        this.markDirty();
    }

    /**
     * Resets the needed resources completely.
     */
    public void resetNeededResources()
    {
        neededResources = new HashMap<>();
        this.markDirty();
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        final NBTTagList neededResTagList = compound.getTagList(TAG_RESOURCE_LIST, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < neededResTagList.tagCount(); ++i)
        {
            final NBTTagCompound neededRes = neededResTagList.getCompoundTagAt(i);
            final IBlockState state = NBTUtil.readBlockState(neededRes);
            final int amount = neededRes.getInteger(TAG_AMOUNT);
            neededResources.put(state.getBlock(), amount);
        }
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        @NotNull final NBTTagList neededResTagList = new NBTTagList();
        for (@NotNull final Map.Entry<Block, Integer> entry : neededResources.entrySet())
        {
            @NotNull final NBTTagCompound neededRes = new NBTTagCompound();
            NBTUtil.writeBlockState(neededRes, entry.getKey().getDefaultState());
            neededRes.setInteger(TAG_AMOUNT, entry.getValue());

            neededResTagList.appendTag(neededRes);
        }
        compound.setTag(TAG_RESOURCE_LIST, neededResTagList);
    }

    /**
     * Method to serialize data to send it to the view.
     *
     * @param buf the used ByteBuffer.
     */
    @Override
    public void serializeToView(@NotNull ByteBuf buf)
    {
        super.serializeToView(buf);

        buf.writeInt(neededResources.size());

        for (@NotNull final Map.Entry<Block, Integer> entry : neededResources.entrySet())
        {
            ByteBufUtils.writeUTF8String(buf, entry.getKey().getLocalizedName());
            buf.writeInt(entry.getValue());
        }
    }

    /**
     * Provides a view of the builder building class.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        private HashMap<String, Integer> neededResources;
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

        /**
         * Gets the blockOut Window.
         *
         * @return the window of the builder building.
         */
        @NotNull
        @Override
        public com.blockout.views.Window getWindow()
        {
            return new WindowHutBuilder(this);
        }

        @Override
        public void deserialize(@NotNull ByteBuf buf)
        {
            super.deserialize(buf);

            final int size = buf.readInt();
            neededResources = new HashMap<>();

            for (int i = 0; i < size; i++)
            {
                final String block = ByteBufUtils.readUTF8String(buf);
                final int amount = buf.readInt();
                neededResources.put(block, amount);
            }
        }

        /**
         * Getter for the needed resources.
         * @return a copy of the HashMap(String, Object).
         */
        public Map<String, Integer> getNeededResources()
        {
            return new HashMap<>(neededResources);
        }
    }
}
