package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingFurnaceUser;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobSmelter;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.Suppression.MAGIC_NUMBERS_SHOULD_NOT_BE_USED;
import static com.minecolonies.api.util.constant.Suppression.OVERRIDE_EQUALS;

/**
 * Class of the smeltery building.
 */
@SuppressWarnings(OVERRIDE_EQUALS)
public class BuildingSmeltery extends AbstractBuildingFurnaceUser
{
    /**
     * The cook string.
     */
    private static final String SMELTERY_DESC = "Smeltery";

    /**
     * Max building level of the cook.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Amount of swords and armor to keep at the worker.
     */
    private static final int STUFF_TO_KEEP = 10;

    /**
     * Instantiates a new cook building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingSmeltery(final Colony c, final BlockPos l)
    {
        super(c, l);
        keepX.put(ColonyManager.getCompatabilityManager()::isOre, Integer.MAX_VALUE);
        keepX.put(TileEntityFurnace::isItemFuel, Integer.MAX_VALUE);
        keepX.put(stack -> !ItemStackUtils.isEmpty(stack)
                && (stack.getItem() instanceof ItemSword || stack.getItem() instanceof ItemTool || stack.getItem() instanceof ItemArmor)
                , STUFF_TO_KEEP);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return SMELTERY_DESC;
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
        return new JobSmelter(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return SMELTERY_DESC;
    }

    @SuppressWarnings(MAGIC_NUMBERS_SHOULD_NOT_BE_USED)
    public int ingotMultiplier(final int citizenLevel, final Random random)
    {
        switch(getBuildingLevel())
        {
            case 1:
                return random.nextInt(ONE_HUNDRED_PERCENT - citizenLevel) == 0 ? DOUBLE : 1;
            case 2:
                return random.nextInt(ONE_HUNDRED_PERCENT - citizenLevel * DOUBLE) == DOUBLE ? 2 : 1;
            case 3:
                return 2;
            case 4:
            case 5:
                return random.nextInt(ONE_HUNDRED_PERCENT - citizenLevel) == 0 ? TRIPLE : DOUBLE;
            default:
                return 1;
        }
    }

    /**
     * BuildingCook View.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        /**
         * Instantiate the smeltery view.
         *
         * @param c the colonyview to put it in
         * @param l the positon
         */
        public View(final ColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutWorkerPlaceholder<>(this, SMELTERY_DESC);
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
            return Skill.INTELLIGENCE;
        }
    }
}
