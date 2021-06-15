package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.google.common.collect.ImmutableMap;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.modules.IBuildingModule;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.huts.WindowHutCrusherModule;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingCrafter;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.colony.jobs.JobCrusher;
import com.minecolonies.coremod.network.messages.server.colony.building.crusher.CrusherSetModeMessage;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.research.util.ResearchConstants.CRUSHING_11;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Class of the crusher building.
 */
public class BuildingCrusher extends AbstractBuildingCrafter
{
    /**
     * The multiplier to define the max craft per day.
     */
    private static final double BUILDING_LEVEL_MULTIPLIER = 16;

    /**
     * The crusher string.
     */
    private static final String CRUSHER_DESC = "crusher";

    /**
     * Max building level of the crusher.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Their crusherRecipes.
     */
    private final Map<ItemStorage, IRecipeStorage> crusherRecipes = new HashMap<>();

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
    private ItemStorage crusherMode = null;

    /**
     * If one by one recipes are enabled.
     */
    private boolean oneByOne = false;

    /**
     * Instantiates a new crusher building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingCrusher(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @Override
    public void registerModule(@NotNull final IBuildingModule module)
    {
        super.registerModule(module);

        if (module instanceof CraftingModule)
        {
            loadCrusherMode();
        }
    }

    /**
     * Load the crusher settings.
     */
    private void loadCrusherMode()
    {
        final CraftingModule module =  getFirstModuleOccurance(CraftingModule.class);
        module.clearRecipes();
        module.checkForWorkerSpecificRecipes();

        this.crusherRecipes.clear();

        final ImmutableMap<IToken<?>, IRecipeStorage> recipes = IColonyManager.getInstance().getRecipeManager().getRecipes();
        for (final IToken<?> token : module.getRecipes())
        {
            final IRecipeStorage storage = recipes.get(token);
            if (storage == null) continue; //wat

            final ItemStorage key = storage.getCleanedInput().get(0);
            if (this.crusherMode == null)
            {
                this.crusherMode = key;
            }
            this.crusherRecipes.put(key, storage);
        }
    }

    /**
     * Get the recipe storage of the current mode.
     *
     * @return the storage.
     */
    public IRecipeStorage getCurrentRecipe()
    {
        return this.crusherRecipes.get(this.crusherMode);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return CRUSHER_DESC;
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
        return new JobCrusher(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return CRUSHER_DESC;
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
        return Skill.Stamina;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Strength;
    }

    /**
     * The the current crusher mode with a certain quantity.
     *
     * @param crusherMode   the new mode.
     * @param dailyQuantity the new quantity per dya.
     */
    public void setCrusherMode(final ItemStorage crusherMode, final int dailyQuantity)
    {
        this.crusherMode = crusherMode;
        this.dailyQuantity = dailyQuantity;
    }

    /**
     * Get the current crusher mode.
     *
     * @return the mode and the quantity.
     */
    public Tuple<ItemStorage, Integer> getCrusherMode()
    {
        return new Tuple<>(crusherMode, dailyQuantity);
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

        if (compound.keySet().contains(TAG_CRUSHER_MODE))
        {
            this.crusherMode = new ItemStorage(ItemStack.read(compound.getCompound(TAG_CRUSHER_MODE)));
        }

        this.oneByOne = compound.getBoolean(TAG_CRUSHER_RATIO);

        loadCrusherMode();
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();
        compound.putInt(TAG_DAILY, dailyQuantity);
        compound.putInt(TAG_CURRENT_DAILY, currentDailyQuantity);
        if (crusherMode != null)
        {
            final CompoundNBT crusherModeNBT = new CompoundNBT();
            crusherMode.getItemStack().write(crusherModeNBT);
            compound.put(TAG_CRUSHER_MODE, crusherModeNBT);
        }

        compound.putBoolean(TAG_CRUSHER_RATIO, oneByOne);
        return compound;
    }

    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        super.serializeToView(buf);

        final boolean oneOne = getColony().getResearchManager().getResearchEffects().getEffectStrength(CRUSHING_11) > 0;
        if (crusherRecipes.isEmpty() || oneByOne != oneOne)
        {
            loadCrusherMode();
        }

        if (crusherMode == null)
        {
            buf.writeBoolean(false);
        }
        else
        {
            buf.writeBoolean(true);
            buf.writeItemStack(crusherMode.getItemStack());
        }
        buf.writeInt(dailyQuantity);

        buf.writeInt(crusherRecipes.size());
        for (final ItemStorage storage : crusherRecipes.keySet())
        {
            buf.writeItemStack(storage.getItemStack());
        }
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.crusher;
    }

    /**
     * BuildingCrusher View.
     */
    public static class View extends AbstractBuildingCrafter.View
    {
        /**
         * Daily quantity to produce.
         */
        private int dailyQuantity = 0;

        /**
         * The current production mode.
         */
        private ItemStorage crusherMode;

        /**
         * The current production mode.
         */
        private final List<ItemStorage> crusherModes = new ArrayList<>();

        /**
         * Instantiate the crusher view.
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

            if (buf.readBoolean())
            {
                crusherMode = new ItemStorage(buf.readItemStack());
            }
            dailyQuantity = buf.readInt();
            crusherModes.clear();

            final int size = buf.readInt();
            for (int i = 0; i < size; i++)
            {
                crusherModes.add(new ItemStorage(buf.readItemStack()));
            }
        }

        /**
         * The the current crusher mode with a certain quantity.
         *
         * @param crusherMode   the new mode.
         * @param dailyQuantity the new quantity per dya.
         */
        public void setCrusherMode(final ItemStorage crusherMode, final int dailyQuantity)
        {
            this.crusherMode = crusherMode;
            this.dailyQuantity = dailyQuantity;
            Network.getNetwork().sendToServer(new CrusherSetModeMessage(this, crusherMode, dailyQuantity));
        }

        /**
         * Get the current crusher mode.
         *
         * @return the mode and the quantity.
         */
        public Tuple<ItemStorage, Integer> getCrusherMode()
        {
            return new Tuple<>(crusherMode, dailyQuantity);
        }

        /**
         * Get all the possible crusher modes.
         *
         * @return the modes.
         */
        public List<ItemStorage> getCrusherModes()
        {
            return this.crusherModes;
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutCrusherModule(this);
        }
    }

    public static class CraftingModule extends AbstractCraftingBuildingModule.Custom
    {
        @Nullable
        @Override
        public IJob<?> getCraftingJob()
        {
            return getMainBuildingJob().orElseGet(() -> new JobCrusher(null));
        }
    }
}
