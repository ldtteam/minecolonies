package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.colony.buildings.modules.settings.IntSetting;
import com.minecolonies.coremod.colony.buildings.modules.settings.RecipeSetting;
import com.minecolonies.coremod.colony.buildings.modules.settings.SettingKey;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_CRUSHER_RATIO;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_CURRENT_DAILY;

/**
 * Class of the crusher building.
 */
public class BuildingCrusher extends AbstractBuilding
{
    /**
     * Settings key for the building mode.
     */
    public static final ISettingKey<RecipeSetting> MODE        = new SettingKey<>(RecipeSetting.class, new ResourceLocation(Constants.MOD_ID, "crushermode"));
    public static final ISettingKey<IntSetting>    DAILY_LIMIT = new SettingKey<>(IntSetting.class, new ResourceLocation(Constants.MOD_ID, "dailylimit"));

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
     * The current daily quantity.
     */
    private int currentDailyQuantity = 0;

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

    @NotNull
    @Override
    public String getSchematicName()
    {
        return CRUSHER_DESC;
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
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);
        this.currentDailyQuantity = compound.getInt(TAG_CURRENT_DAILY);
        this.oneByOne = compound.getBoolean(TAG_CRUSHER_RATIO);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();
        compound.putInt(TAG_CURRENT_DAILY, currentDailyQuantity);
        compound.putBoolean(TAG_CRUSHER_RATIO, oneByOne);
        return compound;
    }

    public static class CraftingModule extends AbstractCraftingBuildingModule.Custom
    {

        /**
         * Create a new module.
         *
         * @param jobEntry the entry of the job.
         */
        public CraftingModule(final JobEntry jobEntry)
        {
            super(jobEntry);
        }
    }
}
