package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.coremod.client.gui.huts.WindowHutWorkerModulePlaceholder;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.modules.settings.BoolSetting;
import com.minecolonies.coremod.colony.buildings.modules.settings.SettingKey;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingWorkerView;
import com.minecolonies.coremod.colony.jobs.JobCowboy;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Creates a new building for the Cowboy.
 */
public class BuildingCowboy extends AbstractBuildingWorker
{
    /**
     * Description of the job executed in the hut.
     */
    private static final String COWBOY = "cowboy";

    /**
     * The hut name, used for the lang string in the GUI
     */
    private static final String HUT_NAME = "cowboyhut";

    /**
     * Max building level of the hut.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Milking setting.
     */
    public static final ISettingKey<BoolSetting> MILKING  = new SettingKey<>(BoolSetting.class, new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, "milking"));

    /**
     * Instantiates the building.
     *
     * @param c the colony.
     * @param l the location.
     */
    public BuildingCowboy(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return COWBOY;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return COWBOY;
    }

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Athletics;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Stamina;
    }

    /**
     * The abstract method which creates a job for the building.
     *
     * @param citizen the citizen to take the job.
     * @return the Job.
     */
    @NotNull
    @Override
    public IJob<?> createJob(final ICitizenData citizen)
    {
        return new JobCowboy(citizen);
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.cowboy;
    }

    @Override
    public boolean canEat(final ItemStack stack)
    {
        if (stack.getItem() == Items.WHEAT)
        {
            return false;
        }
        return super.canEat(stack);
    }

    /**
     * ClientSide representation of the building.
     */
    public static class View extends AbstractBuildingWorkerView
    {
        /**
         * Instantiates the view of the building.
         *
         * @param c the colonyView.
         * @param l the location of the block.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutWorkerModulePlaceholder<>(this, HUT_NAME);
        }
    }
}
