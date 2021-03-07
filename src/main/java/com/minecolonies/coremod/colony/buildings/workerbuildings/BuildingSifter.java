package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.WindowHutSifter;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingCrafter;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.crafting.CustomRecipe;
import com.minecolonies.coremod.colony.crafting.CustomRecipeManager;
import com.minecolonies.coremod.colony.crafting.SifterRecipe;
import com.minecolonies.coremod.colony.jobs.JobSifter;
import com.minecolonies.coremod.network.messages.server.colony.building.sifter.SifterSettingsMessage;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
     * The TAG to store the mesh of the building.
     */
    private static final String TAG_MESH = "mesh";

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
     * The current used mesh.
     */
    private ItemStorage sifterMesh = null;

    /**
     * The chance that the mesh will break after each crafting operation.
     */
    private double meshBreakPercentage = 0;

    /**
     * Instantiates a new sifter building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingSifter(final IColony c, final BlockPos l)
    {
        super(c, l);

        resetMesh();
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
    public boolean isRecipeAlterationAllowed() { return false; }

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
     * Getter for the currently used mesh.
     *
     * @return the ItemStorage.
     */
    public ItemStorage getMesh()
    {
        return this.sifterMesh;
    }

    /**
     * Getter for the percentage chance for the mesh to break.
     *
     * @return A percentage.
     */
    public double getMeshBreakPercentage() { return this.meshBreakPercentage; }

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
     * Reset the mesh to a default value after the other one broke.
     */
    public final void resetMesh()
    {
        CustomRecipeManager.getInstance().getSifterRecipes().stream()
                .min(Comparator.comparing(SifterRecipe::getPower))
                .ifPresent(this::setMesh);
    }

    private void setMesh(@NotNull final SifterRecipe recipe)
    {
        this.sifterMesh = new ItemStorage(recipe.getMesh());
        this.meshBreakPercentage = recipe.getBreakChance();
        markDirty();
    }

    /**
     * Setup the settings to be used by the sifter.
     *
     * @param mesh     the mesh to be used.
     * @param quantity the daily quantity.
     */
    public void setup(final ItemStorage mesh, final int quantity)
    {
        for (final SifterRecipe recipe : CustomRecipeManager.getInstance().getSifterRecipes())
        {
            if (recipe.getMesh().isItemEqual(mesh.getItemStack()))
            {
                setMesh(recipe);
                break;
            }
        }

        this.dailyQuantity = quantity;
        markDirty();
    }

    @Override
    public IRecipeStorage getFirstFullFillableRecipe(final Predicate<ItemStack> stackPredicate, final int count, final boolean considerReservation)
    {
        final List<IRecipeStorage> storages = getValidRecipesForCurrentMesh()
                .map(CustomRecipe::getRecipeStorage)
                .collect(Collectors.toList());

        final IItemHandler[] handlers = getHandlers().toArray(new IItemHandler[0]);
        for (final IRecipeStorage storage : storages)
        {
            if (storage.canFullFillRecipe(count, Collections.emptyMap(), handlers))
            {
                return storage;
            }
        }

        return null;
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

        this.recipes.clear();   // for upgrade purposes; we no longer use building recipes
        this.dailyQuantity = compound.getInt(TAG_DAILY);
        this.currentDailyQuantity = compound.getInt(TAG_CURRENT_DAILY);

        this.sifterMesh = new ItemStorage(ItemStack.read(compound.getCompound(TAG_MESH)));
        this.meshBreakPercentage = compound.getDouble(TAG_MESH_PROB);
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();

        compound.putInt(TAG_DAILY, dailyQuantity);
        compound.putInt(TAG_CURRENT_DAILY, currentDailyQuantity);

        final CompoundNBT meshTAG = new CompoundNBT();
        sifterMesh.getItemStack().write(meshTAG);
        compound.put(TAG_MESH, meshTAG);
        compound.putDouble(TAG_MESH_PROB, meshBreakPercentage);

        return compound;
    }

    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        super.serializeToView(buf);
        buf.writeInt(dailyQuantity);
        buf.writeInt(getMaxDailyQuantity());

        buf.writeItemStack(sifterMesh.getItemStack());

        final Set<SifterRecipe> sifterRecipes = CustomRecipeManager.getInstance().getSifterRecipes();

        final List<ItemStack> meshes = sifterRecipes.stream()
                .sorted(Comparator.comparing(SifterRecipe::getPower))
                .map(SifterRecipe::getMesh)
                .filter(distinctItem())
                .collect(Collectors.toList());
        buf.writeVarInt(meshes.size());
        for (final ItemStack stack : meshes)
        {
            buf.writeItemStack(stack);
        }

        final List<ItemStack> sievable = getValidRecipesForCurrentMesh()
                .map(recipe -> recipe.getRecipeStorage().getCleanedInput().get(0).getItemStack())
                .filter(distinctItem())
                .collect(Collectors.toList());
        buf.writeVarInt(sievable.size());
        for (final ItemStack stack : sievable)
        {
            buf.writeItemStack(stack);
        }
    }

    private Stream<CustomRecipe> getValidRecipesForCurrentMesh()
    {
        return CustomRecipeManager.getInstance().getSifterRecipes().stream()
                .filter(meshRecipe -> meshRecipe.getMesh().isItemEqual(sifterMesh.getItemStack()))
                .flatMap(meshRecipe -> meshRecipe.getInputRecipes().stream())
                .filter(recipe -> recipe.isValidForBuilding(this));
    }

    private static Predicate<ItemStack> distinctItem()
    {
        final Set<Item> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(t.getItem());
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
            
            this.mesh = new ItemStorage(buf.readItemStack());

            meshes.clear();
            final int size2 = buf.readVarInt();
            for (int i = 0; i < size2; ++i)
            {
                meshes.add(new ItemStorage(buf.readItemStack()));
            }

            sievableBlocks.clear();
            final int size3 = buf.readVarInt();
            for (int i = 0; i < size3; ++i)
            {
                sievableBlocks.add(new ItemStorage(buf.readItemStack()));
            }
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
         * Getter for the currently used mesh.
         *
         * @return an ItemStorage.
         */
        public ItemStorage getMesh()
        {
            return mesh;
        }

        /**
         * Get a list of all sievable blocks.
         *
         * @return the list.
         */
        public List<ItemStorage> getSievableBlocks()
        {
            return sievableBlocks;
        }

        /**
         * Get a list of all meshes.
         *
         * @return the list.
         */
        public List<ItemStorage> getMeshes()
        {
            return meshes;
        }

        /**
         * Save the setup.
         *
         * @param mesh          the mesh to use.
         * @param dailyQuantity the daily quantity.
         * @param buy           if buying the mesh is involved.
         */
        public void save(final ItemStorage mesh, final int dailyQuantity, final boolean buy)
        {
            this.mesh = mesh;
            Network.getNetwork().sendToServer(new SifterSettingsMessage(this, mesh, dailyQuantity, buy));
        }


        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutSifter(this);
        }
    }
}
