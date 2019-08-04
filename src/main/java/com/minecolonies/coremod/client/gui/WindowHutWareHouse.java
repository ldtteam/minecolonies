package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.ButtonImage;
import com.minecolonies.blockout.controls.ItemIcon;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.coremod.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.coremod.network.messages.MarkBuildingDirtyMessage;
import com.minecolonies.coremod.network.messages.SortWarehouseMessage;
import com.minecolonies.coremod.network.messages.UpgradeWarehouseMessage;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.coremod.client.gui.WindowHutBuilder.*;

/**
 * Window for the home building.
 */
public class WindowHutWareHouse extends AbstractWindowBuilding<BuildingWareHouse.View>
{
    /**
     * Required building level for sorting.
     */
    private static final int BUILDING_LEVEL_FOR_SORTING = 3;

    /**
     * Allow more upgrades of the storage.
     */
    private boolean allowMoreStorageUpgrades = false;

    /**
     * Constructor for window warehouse hut.
     *
     * @param building {@link BuildingWareHouse.View}.
     */
    public WindowHutWareHouse(final BuildingWareHouse.View building)
    {
        super(building, Constants.MOD_ID + HUT_WAREHOUSE_RESOURCE_SUFFIX);
        registerButton(RESOURCE_ADD, this::transferItems);
        registerButton(SORT_WAREHOUSE_BUTTON, this::sortWarehouse);
        if (building.isBuildingMaxLevel() && building.canUpgradeStorage())
        {
            allowMoreStorageUpgrades = true;
        }
    }

    @Override
    public void onOpened()
    {
        if (building.getBuildingLevel() < BUILDING_LEVEL_FOR_SORTING)
        {
            findPaneOfTypeByID(SORT_WAREHOUSE_BUTTON, ButtonImage.class).hide();
        }
        super.onOpened();

        updateResourcePane();

        //Make sure we have a fresh view
        MineColonies.getNetwork().sendToServer(new MarkBuildingDirtyMessage(this.building));
    }

    /**
     * Update one row pad with its resource informations.
     */
    private void updateResourcePane()
    {
        final BuildingBuilderResource resource = new BuildingBuilderResource(new ItemStack(Blocks.EMERALD_BLOCK, 1), 1);

        final int amountToSet;
        final PlayerInventory inventory = this.mc.player.inventory;
        final boolean isCreative = this.mc.player.capabilities.isCreativeMode;
        if (isCreative)
        {
            amountToSet = resource.getAmount();
        }
        else
        {
            amountToSet = InventoryUtils.getItemCountInItemHandler(new InvWrapper(inventory), resource.getItem(), resource.getDamageValue());
        }
        resource.setPlayerAmount(amountToSet);

        final Label resourceLabel = findPaneOfTypeByID(RESOURCE_NAME, Label.class);
        final Label resourceMissingLabel = findPaneOfTypeByID(RESOURCE_MISSING, Label.class);
        final Label neededLabel = findPaneOfTypeByID(RESOURCE_AVAILABLE_NEEDED, Label.class);
        final Button addButton = findPaneOfTypeByID(RESOURCE_ADD, Button.class);

        BuildingBuilderResource.RessourceAvailability availability = resource.getAvailabilityStatus();

        if (!allowMoreStorageUpgrades)
        {
            availability = BuildingBuilderResource.RessourceAvailability.NOT_NEEDED;
        }

        switch (availability)
        {
            case DONT_HAVE:
                addButton.disable();
                resourceLabel.setColor(RED, RED);
                resourceMissingLabel.setColor(RED, RED);
                neededLabel.setColor(RED, RED);
                break;
            case NEED_MORE:
                addButton.enable();
                resourceLabel.setColor(RED, RED);
                resourceMissingLabel.setColor(RED, RED);
                neededLabel.setColor(RED, RED);
                break;
            case HAVE_ENOUGH:
                addButton.enable();
                resourceLabel.setColor(DARKGREEN, DARKGREEN);
                resourceMissingLabel.setColor(DARKGREEN, DARKGREEN);
                neededLabel.setColor(DARKGREEN, DARKGREEN);
                break;
            case NOT_NEEDED:
            default:
                addButton.disable();
                resourceLabel.setColor(BLACK, BLACK);
                resourceMissingLabel.setColor(BLACK, BLACK);
                neededLabel.setColor(BLACK, BLACK);
                break;
        }

        //position the addResource Button to the right

        resourceLabel.setLabelText(resource.getName());
        final int missing = resource.getMissingFromPlayer();
        if (missing < 0)
        {
            resourceMissingLabel.setLabelText(Integer.toString(missing));
        }
        else
        {
            resourceMissingLabel.setLabelText("");
        }

        neededLabel.setLabelText(Integer.toString(resource.getAvailable()) + " / " + Integer.toString(resource.getAmount()));
        findPaneOfTypeByID(RESOURCE_QUANTITY_MISSING, Label.class).setLabelText(Integer.toString(resource.getAmount() - resource.getAvailable()));

        findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(new ItemStack(resource.getItem(), 1, resource.getDamageValue()));
    }

    /**
     * On Button click transfer Items.
     */
    private void transferItems()
    {
        MineColonies.getNetwork().sendToServer(new UpgradeWarehouseMessage(this.building));
        allowMoreStorageUpgrades = false;
        this.updateResourcePane();
    }

    /**
     * On button click for warehouse sorting.
     */
    private void sortWarehouse()
    {
        MineColonies.getNetwork().sendToServer(new SortWarehouseMessage(this.building));
    }

    /**
     * Returns the name of a building.
     *
     * @return Name of a building.
     */
    @NotNull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerHuts.buildingWareHouse";
    }
}
