package com.minecolonies.coremod.client.gui.modules;

import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.AbstractModuleWindow;
import com.minecolonies.coremod.colony.buildings.moduleviews.WarehouseOptionsModuleView;
import com.minecolonies.coremod.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.coremod.network.messages.server.colony.building.MarkBuildingDirtyMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.warehouse.SortWarehouseMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.warehouse.UpgradeWarehouseMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.items.wrapper.InvWrapper;

import static com.minecolonies.api.util.constant.TranslationConstants.LABEL_X_OF_Z;
import static com.minecolonies.api.util.constant.TranslationConstants.WAREHOUSE_SORTED;
import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.coremod.client.gui.modules.WindowBuilderResModule.*;

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
     * @param module the module belonging to it.
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
     * Update one row pad with its resource informations.
     */
    private void updateResourcePane()
    {
        final BuildingBuilderResource resource = new BuildingBuilderResource(new ItemStack(Blocks.EMERALD_BLOCK, 1), 1);

        final int amountToSet;
        final Inventory inventory = this.mc.player.getInventory();
        final boolean isCreative = this.mc.player.isCreative();
        if (isCreative)
        {
            amountToSet = resource.getAmount();
        }
        else
        {
            amountToSet = InventoryUtils.getItemCountInItemHandler(new InvWrapper(inventory), resource.getItem());
        }
        resource.setPlayerAmount(amountToSet);

        final Text resourceLabel = findPaneOfTypeByID(RESOURCE_NAME, Text.class);
        final Text resourceMissingLabel = findPaneOfTypeByID(RESOURCE_MISSING, Text.class);
        final Text neededLabel = findPaneOfTypeByID(RESOURCE_AVAILABLE_NEEDED, Text.class);
        final Button addButton = findPaneOfTypeByID(RESOURCE_ADD, Button.class);

        BuildingBuilderResource.RessourceAvailability availability = resource.getAvailabilityStatus();

        if (module.getStorageUpgradeLevel() >= BuildingWareHouse.MAX_STORAGE_UPGRADE || buildingView.getBuildingLevel() < buildingView.getBuildingMaxLevel() || lockUpgrade)
        {
            availability = BuildingBuilderResource.RessourceAvailability.NOT_NEEDED;
        }

        findPaneOfTypeByID(UPGRADE_PROGRESS_LABEL, Text.class).setText(Component.translatable(LABEL_X_OF_Z,
          module.getStorageUpgradeLevel(),
          BuildingWareHouse.MAX_STORAGE_UPGRADE));

        switch (availability)
        {
            case DONT_HAVE:
                addButton.disable();
                resourceLabel.setColors(RED);
                resourceMissingLabel.setColors(RED);
                neededLabel.setColors(RED);
                break;
            case NEED_MORE:
                addButton.enable();
                resourceLabel.setColors(RED);
                resourceMissingLabel.setColors(RED);
                neededLabel.setColors(RED);
                break;
            case HAVE_ENOUGH:
                addButton.enable();
                resourceLabel.setColors(DARKGREEN);
                resourceMissingLabel.setColors(DARKGREEN);
                neededLabel.setColors(DARKGREEN);
                break;
            case NOT_NEEDED:
            default:
                addButton.disable();
                resourceLabel.setColors(BLACK);
                resourceMissingLabel.setColors(BLACK);
                neededLabel.setColors(BLACK);
                if (buildingView.getBuildingLevel() < buildingView.getBuildingMaxLevel())
                {
                    resourceLabel.hide();
                    resourceMissingLabel.hide();
                    neededLabel.hide();
                    addButton.setText(Component.literal("X").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED)));
                    PaneBuilders.tooltipBuilder()
                        .append(Component.translatable("com.minecolonies.coremod.gui.warehouse.upgrade.disabled.1", buildingView.getBuildingMaxLevel()))
                        .appendNL(Component.translatable("com.minecolonies.coremod.gui.warehouse.upgrade.disabled.2", buildingView.getBuildingMaxLevel()))
                        .hoverPane(addButton)
                        .build();
                }
                break;
        }

        resourceLabel.setText(Component.literal(resource.getName()));
        final int missing = resource.getMissingFromPlayer();
        if (missing < 0)
        {
            resourceMissingLabel.setText(Component.literal(Integer.toString(missing)));
        }
        else
        {
            resourceMissingLabel.clearText();
        }

        neededLabel.setText(Component.literal(resource.getAvailable() + " / " + resource.getAmount()));
        findPaneOfTypeByID(RESOURCE_QUANTITY_MISSING, Text.class).setText(Component.literal(Integer.toString(resource.getAmount() - resource.getAvailable())));

        if(buildingView.getBuildingLevel() >= buildingView.getBuildingMaxLevel())
        {
            final ItemStack resourceStackOfOne = new ItemStack(resource.getItem(), 1);
            resourceStackOfOne.setTag(resource.getItemStack().getTag());
            findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(resourceStackOfOne);
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
}
