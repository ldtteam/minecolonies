package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.client.gui.WindowHutSifter;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingCrafter;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobSifter;
import com.minecolonies.coremod.network.messages.SifterSettingsMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
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
    private static final String SIFTER_DESC = "Sifter";

    /**
     * Max building level of the sifter.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * The TAG to store the mesh of the building.
     */
    private static final String TAG_MESH   = "mesh";

    /**
     * The TAG to store the probability of the mesh to break.
     */
    private static final String TAG_MESH_PROB = "meshProb";

    /**
     * Daily quantity to produce.
     */
    private int dailyQuantity = 0;

    /**
     * The current daily quantity.
     */
    private int currentDailyQuantity = 0;

    /**
     * The current productionmode.
     */
    private ItemStorage sievableBlock = null;

    /**
     * The current used mesh.
     */
    private Tuple<ItemStorage, Double> sifterMesh = null;

    /**
     * Instantiates a new sifter building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingSifter(final Colony c, final BlockPos l)
    {
        super(c, l);

        if (!ColonyManager.getCompatibilityManager().getSievableBlock().isEmpty())
        {
            this.sievableBlock = ColonyManager.getCompatibilityManager().getSievableBlock().get(0);
        }

        if (!ColonyManager.getCompatibilityManager().getMeshes().isEmpty())
        {
            this.sifterMesh = ColonyManager.getCompatibilityManager().getMeshes().get(0);
        }
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
    public AbstractJob createJob(final CitizenData citizen)
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

    /**
     * Get the daily quantity the sifter shall produce.
     * @return the quantity.
     */
    public int getDailyQuantity()
    {
        return this.dailyQuantity;
    }

    /**
     * Getter for the current block which should be sieved.
     * @return the ItemStorage.
     */
    public ItemStorage getSievableBlock()
    {
        return this.sievableBlock;
    }

    /**
     * Getter for the currently used mesh.
     * @return the ItemStorage.
     */
    public Tuple<ItemStorage, Double> getMesh()
    {
        return this.sifterMesh;
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
     * Reset the mesh to a default value after the other one broke.
     */
    public void resetMesh()
    {
        if (!ColonyManager.getCompatibilityManager().getMeshes().isEmpty())
        {
            this.sifterMesh = ColonyManager.getCompatibilityManager().getMeshes().get(0);
        }
    }

    /**
     * Setup the settings to be used by the sifter.
     * @param block the block to be sieved.
     * @param mesh the mesh to be used.
     * @param quantity the daily quantity.
     */
    public void setup(final ItemStorage block, final ItemStorage mesh, final int quantity)
    {
        this.sievableBlock = block;
        for (final Tuple<ItemStorage, Double> tuple : ColonyManager.getCompatibilityManager().getMeshes())
        {
            if (tuple.getA().equals(mesh))
            {
                this.sifterMesh = new Tuple<>(mesh, tuple.getB());
                break;
            }
        }
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
    public void readFromNBT(@NotNull final CompoundNBT compound)
    {
        super.readFromNBT(compound);
        this.dailyQuantity = compound.getInt(TAG_DAILY);
        this.currentDailyQuantity = compound.getInt(TAG_CURRENT_DAILY);

        this.sievableBlock = new ItemStorage(new ItemStack(compound.getCompound(TAG_BLOCK)));
        final ItemStorage mesh = new ItemStorage(new ItemStack(compound.getCompound(TAG_MESH)));
        final double prob = compound.getDouble(TAG_MESH_PROB);
        this.sifterMesh = new Tuple<>(mesh, prob);
    }

    @Override
    public void writeToNBT(@NotNull final CompoundNBT compound)
    {
        super.writeToNBT(compound);
        compound.putInt(TAG_DAILY, dailyQuantity);
        compound.putInt(TAG_CURRENT_DAILY, currentDailyQuantity);

        final CompoundNBT sievableBlockTAG = new CompoundNBT();
        sievableBlock.getItemStack().writeToNBT(sievableBlockTAG);
        compound.put(TAG_BLOCK, sievableBlockTAG);

        final CompoundNBT meshTAG = new CompoundNBT();
        sifterMesh.getA().getItemStack().writeToNBT(meshTAG);
        compound.put(TAG_MESH, meshTAG);
        compound.setDouble(TAG_MESH_PROB, sifterMesh.getB());
    }

    @Override
    public void serializeToView(@NotNull final ByteBuf buf)
    {
        super.serializeToView(buf);
        buf.writeInt(dailyQuantity);

        ByteBufUtils.writeItemStack(buf, sievableBlock.getItemStack());
        ByteBufUtils.writeItemStack(buf, sifterMesh.getA().getItemStack());

        buf.writeInt(ColonyManager.getCompatibilityManager().getSievableBlock().size());
        for (final ItemStorage storage : ColonyManager.getCompatibilityManager().getSievableBlock())
        {
            ByteBufUtils.writeItemStack(buf, storage.getItemStack());
        }

        buf.writeInt(ColonyManager.getCompatibilityManager().getMeshes().size());
        for (final Tuple<ItemStorage, Double> storage : ColonyManager.getCompatibilityManager().getMeshes())
        {
            ByteBufUtils.writeItemStack(buf, storage.getA().getItemStack());
        }
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
         * The current block to be sifted.
         */
        private ItemStorage sifterBlock;

        /**
         * The currently used mesh.
         */
        private ItemStorage mesh;

        /**
         * A list of all possible blocks.
         */
        private List<ItemStorage> sievableBlocks = new ArrayList<>();

        /**
         * A list of all possible meshes.
         */
        private List<ItemStorage> meshes = new ArrayList<>();

        /**
         * Instantiate the sifter view.
         *
         * @param c the colonyview to put it in
         * @param l the positon
         */
        public View(final ColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @Override
        public void deserialize(@NotNull final ByteBuf buf)
        {
            super.deserialize(buf);
            dailyQuantity = buf.readInt();
            this.sifterBlock = new ItemStorage(ByteBufUtils.readItemStack(buf));
            this.mesh = new ItemStorage(ByteBufUtils.readItemStack(buf));

            sievableBlocks.clear();
            meshes.clear();

            final int size = buf.readInt();
            for (int i = 0; i < size; i++)
            {
                sievableBlocks.add(new ItemStorage(ByteBufUtils.readItemStack(buf)));
            }

            final int size2 = buf.readInt();
            for (int i = 0; i < size2; i++)
            {
                meshes.add(new ItemStorage(ByteBufUtils.readItemStack(buf)));
            }
        }

        /**
         * Getter for the current set daily quantity.
         * @return the quantity set.
         */
        public int getDailyQuantity()
        {
            return dailyQuantity;
        }

        /**
         * Getter for the current sifter block.
         * @return an ItemStorage.
         */
        public ItemStorage getSifterBlock()
        {
            return sifterBlock;
        }

        /**
         * Getter for the currently used mesh.
         * @return an ItemStorage.
         */
        public ItemStorage getMesh()
        {
            return mesh;
        }

        /**
         * Get a list of all sievable blocks.
         * @return the list.
         */
        public List<ItemStorage> getSievableBlocks()
        {
            return sievableBlocks;
        }

        /**
         * Get a list of all meshes.
         * @return the list.
         */
        public List<ItemStorage> getMeshes()
        {
            return meshes;
        }

        /**
         * Save the setup.
         * @param sifterBlock the block to sift.
         * @param mesh the mesh to use.
         * @param dailyQuantity the daily quantity.
         * @param buy if buying the mesh is involved.
         */
        public void save(final ItemStorage sifterBlock, final ItemStorage mesh, final int dailyQuantity, final boolean buy)
        {
            this.sifterBlock = sifterBlock;
            this.mesh = mesh;
            MineColonies.getNetwork().sendToServer(new SifterSettingsMessage(this, sifterBlock, mesh, dailyQuantity, buy));
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutSifter(this);
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
