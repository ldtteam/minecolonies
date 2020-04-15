package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.tileentities.AbstractTileEntityRack;
import com.minecolonies.api.tileentities.TileEntityRack;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.Constants;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ButtonImage;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.controls.Label;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.coremod.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.coremod.network.messages.MarkBuildingDirtyMessage;
import com.minecolonies.coremod.network.messages.RemoveMinimumStockFromBuildingMessage;
import com.minecolonies.coremod.network.messages.SortWarehouseMessage;
import com.minecolonies.coremod.network.messages.UpgradeWarehouseMessage;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.coremod.client.gui.WindowHutBuilder.*;

/**
 * Window for the home building.
 */
public class WindowHutWareHouse extends AbstractWindowBuilding<BuildingWareHouse.View>
{
    /**
     * List of all item stacks in the game.
     */
    private final List<Tuple<ITextComponent, Integer>> allItems = new ArrayList<Tuple<ITextComponent, Integer>>();

    /**
     * Resource scrolling list.
     */
    private final ScrollingList stackList;
    /**
     * The filter for the resource list.
     */
    private       String        filter = "";

    public static final String LIST_ALLINVENTORY = "allinventory";

    private final        BuildingWareHouse.View building;
    /**
     * Required building level for sorting.
     */
    private static final int                    BUILDING_LEVEL_FOR_SORTING = 3;

    /**
     * Limit reached label.
     */
    private static final String LABEL_LIMIT_REACHED = "com.minecolonies.coremod.gui.warehouse.limitreached";

    /**
     * Allow more upgrades of the storage.
     */
    private boolean allowMoreStorageUpgrades = false;

    /**
     * Resource scrolling list.
     */
    private final ScrollingList resourceList;

    /**
     * Constructor for window warehouse hut.
     *
     * @param building {@link BuildingWareHouse.View}.
     */
    public WindowHutWareHouse(final BuildingWareHouse.View building)
    {
        super(building, Constants.MOD_ID + HUT_WAREHOUSE_RESOURCE_SUFFIX);
        this.building = building;
        registerButton(RESOURCE_ADD, this::transferItems);
        registerButton(SORT_WAREHOUSE_BUTTON, this::sortWarehouse);
        resourceList = window.findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);
        this.stackList = findPaneOfTypeByID(LIST_ALLINVENTORY, ScrollingList.class);
        if (building.isBuildingMaxLevel() && building.canUpgradeStorage())
        {
            allowMoreStorageUpgrades = true;
        }
        registerButton(STOCK_ADD, this::addStock);
        if (building.hasReachedLimit())
        {
            final ButtonImage button = findPaneOfTypeByID(STOCK_ADD, ButtonImage.class);
            button.setLabel(LanguageHandler.format(LABEL_LIMIT_REACHED));
            button.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/builderhut/builder_button_medium_dark.png"));
        }

        registerButton(STOCK_REMOVE, this::removeStock);
    }

    /**
     * Remove the stock.
     *
     * @param button the button.
     */
    private void removeStock(final Button button)
    {
        final int row = resourceList.getListElementIndexByPane(button);
        final Tuple<ItemStorage, Integer> tuple = building.getStock().get(row);
        building.getStock().remove(row);
        Network.getNetwork().sendToServer(new RemoveMinimumStockFromBuildingMessage(tuple.getA().getItemStack(), building.getColony().getID(), building.getID()));
        updateStockList();
    }

    /**
     * Add the stock.
     */
    private void addStock()
    {
        if (!building.hasReachedLimit())
        {
            new WindowSelectRes(this, building.getColony().getID(), building.getID()).open();
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
        updateStockList();
        updateResources();
        //Make sure we have a fresh view
        Network.getNetwork().sendToServer(new MarkBuildingDirtyMessage(this.building));
    }

    /**
     * Update one row pad with its resource informations.
     */
    private void updateResourcePane()
    {
        final BuildingBuilderResource resource = new BuildingBuilderResource(new ItemStack(Blocks.EMERALD_BLOCK, 1), 1);

        final int amountToSet;
        final PlayerInventory inventory = this.mc.player.inventory;
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

        neededLabel.setLabelText(resource.getAvailable() + " / " + resource.getAmount());
        findPaneOfTypeByID(RESOURCE_QUANTITY_MISSING, Label.class).setLabelText(Integer.toString(resource.getAmount() - resource.getAvailable()));

        findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(new ItemStack(resource.getItem(), 1));
    }

    /**
     * Updates the resource list in the GUI with the info we need.
     */
    private void updateStockList()
    {
        resourceList.enable();
        resourceList.show();
        final List<Tuple<ItemStorage, Integer>> tempRes = new ArrayList<>(building.getStock());

        //Creates a dataProvider for the unemployed resourceList.
        resourceList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return tempRes.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final ItemStack resource = tempRes.get(index).getA().getItemStack().copy();
                resource.setCount(resource.getMaxStackSize());

                final Label resourceLabel = rowPane.findPaneOfTypeByID(RESOURCE_NAME, Label.class);
                resourceLabel.setLabelText(resource.getDisplayName().getFormattedText());

                final Label quantityLabel = rowPane.findPaneOfTypeByID(QUANTITY_LABEL, Label.class);
                quantityLabel.setLabelText(String.valueOf(tempRes.get(index).getB()));

                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(resource);
            }
        });
    }

    /**
     * Update the item list.
     */
    private void updateResources()
    {
        final List<Tuple<ItemStorage, Integer>> tempRes = new ArrayList<>(building.getStock());
        List<Tuple<ITextComponent, Integer>> transfer = new ArrayList<>();

        final List<BlockPos> containerList = building.getContainerList();
        List<ItemStack> items = new ArrayList<>();
        int count = containerList.size();
        World world = building.getColony().getWorld();

        for (int s = 0; s < count; s++)
        {
            final TileEntity rack = world.getTileEntity(containerList.get(s));
            if (rack instanceof TileEntityRack)
            {

                int allSlots = ((AbstractTileEntityRack) rack).getInventory().getSlots();
                for (int i = 0; i < allSlots; i++)
                {
                    if (!((AbstractTileEntityRack) rack).getInventory().getStackInSlot(i).isEmpty())
                    {
                        items.add(((AbstractTileEntityRack) rack).getInventory().getStackInSlot(i));
                    }
                }
            }
        }

        final Map<ITextComponent, Integer> map = new HashMap<>();
        if (items != null)
        {
            for (int r = 0; r < items.size(); r++)
            {
                final ItemStack storage = items.get(r);
                int amount = storage.getCount();
                if (map.containsKey(storage.getDisplayName()))
                {
                    amount += map.remove(storage.getDisplayName());
                }
                map.put(storage.getDisplayName(), amount);
            }
            for (Map.Entry<ITextComponent, Integer> entry : map.entrySet())
            {
                transfer.add(new Tuple<>(entry.getKey(), entry.getValue()));
            }
        }

        allItems.clear();
        allItems.addAll(transfer);
        updateResourceList();
    }

    /**
     * Updates the resource list in the GUI with the info we need.
     */
    private void updateResourceList()
    {
        stackList.enable();

        //Creates a dataProvider for the unemployed stackList.
        stackList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return allItems.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final Tuple<ITextComponent, Integer> resource = allItems.get(index);
                final Label resourceLabel = rowPane.findPaneOfTypeByID("ressName", Label.class);
                resourceLabel.setLabelText(resource.getA().getFormattedText());
                final Label qtys = rowPane.findPaneOfTypeByID("qtys", Label.class);
                qtys.setLabelText(resource.getB().toString());
            }
        });
    }

    /**
     * Get the list of blocks which should be added.
     *
     * @param filterPredicate the predicate to filter all blocks for.
     * @return an immutable list of blocks.
     */
    private Collection<? extends ItemStack> getBlockList(final Predicate<ItemStack> filterPredicate)
    {
        if (filter.isEmpty())
        {
            return IColonyManager.getInstance().getCompatibilityManager().getBlockList();
        }
        return IColonyManager.getInstance().getCompatibilityManager().getBlockList().stream().filter(filterPredicate).collect(Collectors.toList());
    }

    /**
     * On Button click transfer Items.
     */
    private void transferItems()
    {
        Network.getNetwork().sendToServer(new UpgradeWarehouseMessage(this.building));
        allowMoreStorageUpgrades = false;
        this.updateResourcePane();
    }

    /**
     * On button click for warehouse sorting.
     */
    private void sortWarehouse()
    {
        Network.getNetwork().sendToServer(new SortWarehouseMessage(this.building));
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
        return "com.minecolonies.coremod.gui.workerhuts.buildingWareHouse";
    }
}
