package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.WindowHutSifter;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingCrafter;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.JobSifter;
import com.minecolonies.coremod.network.messages.server.colony.building.sifter.SifterSettingsMessage;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Class of the sifter building.
 */
public class BuildingSifter extends AbstractBuildingWorker
{
    /**
     * The multiplier to define the max craft per day.
     */
    private static final double BUILDING_LEVEL_MULTIPLIER = 64;

    /**
     * The sifter string.
     */
    private static final String SIFTER_DESC = "sifter";

    /**
     * Max building level of the sifter.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Daily quantity to produce.
     */
    private int dailyQuantity = 0;

    /**
     * The current daily quantity.
     */
    private int currentDailyQuantity = 0;

    /**
     * Instantiates a new sifter building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingSifter(final IColony c, final BlockPos l)
    {
        super(c, l);

        keepX.put(stack -> stack.getItem().isIn(ModTags.meshes), new net.minecraft.util.Tuple<>(4, false));
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return SIFTER_DESC;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @NotNull
    @Override
    public IJob<?> createJob(final ICitizenData citizen)
    {
        return new JobSifter(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return SIFTER_DESC;
    }

    @Override
    public boolean canCraftComplexRecipes()
    {
        return false;
    }

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Focus;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Strength;
    }

    /**
     * Get the daily quantity the sifter shall produce.
     *
     * @return the quantity.
     */
    public int getDailyQuantity()
    {
        return this.dailyQuantity;
    }

    /**
     * Set the current daily quantity.
     *
     * @param currentDailyQuantity the current quantity.
     */
    public void setCurrentDailyQuantity(final int currentDailyQuantity)
    {
        this.currentDailyQuantity = currentDailyQuantity;
    }

    /**
     * Calculate the max quantity to be crafted per day.
     *
     * @return the max.
     */
    public int getMaxDailyQuantity()
    {
        if (getBuildingLevel() >= MAX_BUILDING_LEVEL)
        {
            return Integer.MAX_VALUE;
        }

        return (int) (Math.pow(getBuildingLevel(), 2) * BUILDING_LEVEL_MULTIPLIER);
    }

    /**
     * Setup the settings to be used by the sifter.
     *
     * @param block    the block to be sieved.
     * @param quantity the daily quantity.
     */
    public void setup(final int quantity)
    {
        this.dailyQuantity = quantity;
        markDirty();
    }

    @Override
    public void onWakeUp()
    {
        super.onWakeUp();
        this.currentDailyQuantity = 0;
    }

    /**
     * Get the current daily quantity.
     *
     * @return the quantity.
     */
    public int getCurrentDailyQuantity()
    {
        return currentDailyQuantity;
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);

        this.dailyQuantity = compound.getInt(TAG_DAILY);
        this.currentDailyQuantity = compound.getInt(TAG_CURRENT_DAILY);
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();

        compound.putInt(TAG_DAILY, dailyQuantity);
        compound.putInt(TAG_CURRENT_DAILY, currentDailyQuantity);

        return compound;
    }

    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        super.serializeToView(buf);
        buf.writeInt(dailyQuantity);
        buf.writeInt(getMaxDailyQuantity());
        buf.writeInt(getCurrentDailyQuantity());
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.sifter;
    }

    /**
     * BuildingSifter View.
     */
    public static class View extends AbstractBuildingCrafter.View
    {
        /**
         * Daily quantity to produce.
         */
        private int dailyQuantity = 0;

        /**
         * Maximum possible daily quantity
         */
        private int maxDailyQuantity = 0;

        /**
         * Current daily quantity
         */
        private int currentDailyQuantity = 0;

        /**
         * A list of all possible blocks.
         */
        private List<ItemStorage> sievableBlocks = new ArrayList<>();

        /**
         * Instantiate the sifter view.
         *
         * @param c the colonyview to put it in
         * @param l the positon
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @Override
        public void deserialize(@NotNull final PacketBuffer buf)
        {
            super.deserialize(buf);
            dailyQuantity = buf.readInt();
            maxDailyQuantity = buf.readInt();
            currentDailyQuantity = buf.readInt();
        }

        /**
         * Getter for the current set daily quantity.
         *
         * @return the quantity set.
         */
        public int getDailyQuantity()
        {
            return dailyQuantity;
        }

        /**
         * Getter for the current maximum settable daily quantity
         * @return the maximum
         */
        public int getMaxDailyQuantity()
        {
            return maxDailyQuantity;
        }

        /**
         * Getter for the current daily quantity
         * @return the current quantity
         */
        public int getCurrentDailyQuantity()
        {
            return currentDailyQuantity;
        }

        /**
         * Get a list of all sievable blocks.
         *
         * @return the list.
         */
        public List<ItemStorage> getSievableBlocks()
        {
            sievableBlocks.clear();
            for(IRecipeStorage recipe : getRecipes())
            {
                for(ItemStorage item: recipe.getCleanedInput())
                {
                    sievableBlocks.add(item);
                }
            }
            return sievableBlocks;
        }

        /**
         * Save the setup.
         *
         * @param dailyQuantity the daily quantity.
         */
        public void save(final int dailyQuantity)
        {
            Network.getNetwork().sendToServer(new SifterSettingsMessage(this, dailyQuantity));
        }


        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutSifter(this);
        }
    }
}
