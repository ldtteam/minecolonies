package com.minecolonies.core.client.gui.modules;

import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.Text;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import com.minecolonies.core.client.gui.generic.ResourceItem;
import com.minecolonies.core.client.gui.generic.ResourceItem.Resource;
import com.minecolonies.core.client.gui.generic.ResourceItem.ResourceAvailability;
import com.minecolonies.core.colony.buildings.moduleviews.WarehouseOptionsModuleView;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.core.network.messages.server.colony.building.MarkBuildingDirtyMessage;
import com.minecolonies.core.network.messages.server.colony.building.warehouse.SortWarehouseMessage;
import com.minecolonies.core.network.messages.server.colony.building.warehouse.UpgradeWarehouseMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.LABEL_X_OF_Z;
import static com.minecolonies.api.util.constant.TranslationConstants.WAREHOUSE_SORTED;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * BOWindow for the warehouse options.
 */
public class WarehouseOptionsModuleWindow extends AbstractModuleWindow
{
    /**
     * Required building level for sorting.
     */
    private static final int BUILDING_LEVEL_FOR_SORTING = 3;

    /**
     * The respective module.
     */
    private final WarehouseOptionsModuleView module;

    /**
     * If further upgrades should be locked.
     */
    private boolean lockUpgrade = false;

    /**
     * Constructor for window warehouse hut.
     *
     * @param module   the module belonging to it.
     * @param building {@link BuildingWareHouse.View}.
     */
    public WarehouseOptionsModuleWindow(final IBuildingView building, final WarehouseOptionsModuleView module)
    {
        super(building, Constants.MOD_ID + HUT_WAREHOUSE_RESOURCE_SUFFIX);
        registerButton(RESOURCE_ADD, this::transferItems);
        registerButton(SORT_WAREHOUSE_BUTTON, this::sortWarehouse);
        this.module = module;
    }

    @Override
    public void onOpened()
    {
        if (buildingView.getBuildingLevel() < BUILDING_LEVEL_FOR_SORTING)
        {
            final ButtonImage sortButton = findPaneOfTypeByID(SORT_WAREHOUSE_BUTTON, ButtonImage.class);
            PaneBuilders.tooltipBuilder()
              .append(Component.translatable("com.minecolonies.coremod.gui.warehouse.sort.disabled.1", BUILDING_LEVEL_FOR_SORTING))
              .appendNL(Component.translatable("com.minecolonies.coremod.gui.warehouse.sort.disabled.2", BUILDING_LEVEL_FOR_SORTING))
              .hoverPane(sortButton)
              .build();
            sortButton.disable();
        }

        super.onOpened();

        updateResourcePane();
        //Make sure we have a fresh view
        Network.getNetwork().sendToServer(new MarkBuildingDirtyMessage(this.buildingView));
    }

    /**
     * Update one row pad with its resource information.
     */
    private void updateResourcePane()
    {
        final EmeraldBlockResource resource = new EmeraldBlockResource();

        findPaneOfTypeByID(UPGRADE_PROGRESS_LABEL, Text.class).setText(Component.translatable(LABEL_X_OF_Z,
          module.getStorageUpgradeLevel(),
          BuildingWareHouse.MAX_STORAGE_UPGRADE));

        ResourceItem.updateResourcePane(resource, 0, this);

        if (buildingView.getBuildingLevel() < buildingView.getBuildingMaxLevel())
        {
            PaneBuilders.tooltipBuilder()
              .append(Component.translatable("com.minecolonies.coremod.gui.warehouse.upgrade.disabled.1", buildingView.getBuildingMaxLevel()))
              .appendNL(Component.translatable("com.minecolonies.coremod.gui.warehouse.upgrade.disabled.2", buildingView.getBuildingMaxLevel()))
              .hoverPane(findPaneByID(RESOURCE_ADD))
              .build();
        }
    }

    /**
     * On Button click transfer Items.
     */
    private void transferItems()
    {
        Network.getNetwork().sendToServer(new UpgradeWarehouseMessage(this.buildingView));
        module.incrementStorageUpgrade();
        lockUpgrade = true;
        this.updateResourcePane();
    }

    /**
     * On button click for warehouse sorting.
     */
    private void sortWarehouse()
    {
        if (buildingView.getBuildingLevel() >= BUILDING_LEVEL_FOR_SORTING)
        {
            Network.getNetwork().sendToServer(new SortWarehouseMessage(this.buildingView));
            MessageUtils.format(WAREHOUSE_SORTED).sendTo(Minecraft.getInstance().player);
        }
    }

    /**
     * Simple container class for the emerald block resource.
     */
    private class EmeraldBlockResource implements Resource
    {
        @Override
        public Component getName()
        {
            return Blocks.EMERALD_BLOCK.getName();
        }

        @Override
        public List<ItemStack> getIcon()
        {
            return null;
        }

        @Override
        public ResourceAvailability getAvailabilityStatus()
        {
            if (module.getStorageUpgradeLevel() >= BuildingWareHouse.MAX_STORAGE_UPGRADE || buildingView.getBuildingLevel() < buildingView.getBuildingMaxLevel() || lockUpgrade)
            {
                return ResourceAvailability.NOT_NEEDED;
            }
            return Resource.super.getAvailabilityStatus();
        }

        @Override
        public int getAmount()
        {
            return 1;
        }

        @Override
        public int getAmountAvailable()
        {
            return 0;
        }

        @Override
        public int getAmountPlayer()
        {
            if (mc.player.isCreative())
            {
                return 1;
            }
            else
            {
                return InventoryUtils.getItemCountInItemHandler(new InvWrapper(mc.player.getInventory()), Blocks.EMERALD_BLOCK.asItem());
            }
        }

        @Override
        public int getAmountInDelivery()
        {
            return 0;
        }
    }
}
