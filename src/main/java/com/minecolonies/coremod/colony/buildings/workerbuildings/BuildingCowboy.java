package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.coremod.client.gui.huts.WindowHutWorkerModulePlaceholder;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.settings.BoolSetting;
import com.minecolonies.coremod.colony.buildings.modules.settings.SettingKey;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

/**
 * Creates a new building for the Cowboy.
 */
public class BuildingCowboy extends AbstractBuilding
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
    public static class View extends AbstractBuildingView
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
        public BOWindow getWindow()
        {
            return new WindowHutWorkerModulePlaceholder<>(this, HUT_NAME);
        }
    }
}
