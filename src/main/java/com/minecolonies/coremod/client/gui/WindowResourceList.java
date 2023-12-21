package com.minecolonies.coremod.client.gui;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Image;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Delivery;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.colony.workorders.IWorkOrderView;
import com.minecolonies.api.tileentities.TileEntityRack;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.moduleviews.BuildingResourcesModuleView;
import com.minecolonies.coremod.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.network.messages.server.ResourceScrollSaveWarehouseSnapshotMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.MarkBuildingDirtyMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_WAREHOUSE_SNAPSHOT;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_WAREHOUSE_SNAPSHOT_WO_HASH;
import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.api.util.constant.translation.ToolTranslationConstants.TOOL_RESOURCE_SCROLL_ERROR;
import static com.minecolonies.api.util.constant.translation.ToolTranslationConstants.TOOL_RESOURCE_SCROLL_NO_BUILDER;
import static com.minecolonies.coremod.client.gui.modules.WindowBuilderResModule.*;
import static com.minecolonies.coremod.colony.buildings.utils.BuildingBuilderResource.RessourceAvailability.*;

/**
 * BOWindow for the resource list item.
 */
public class WindowResourceList extends AbstractWindowSkeleton
{
    /**
     * The position of the builder hut.
     */
    @NotNull
    private final BlockPos buildingPos;

    /**
     * The view.
     */
    @Nullable
    private final BuildingBuilder.View builder;

    /**
     * List of resources needed.
     */
    @NotNull
    private final List<BuildingBuilderResource> resources = new ArrayList<>();

    /**
     * The position of the warehouse clicked on.
     */
    @Nullable
    private final BlockPos warehousePos;

    /**
     * The direct compound of the itemStack. We write the warehouse data to it to update the stack on the client side.
     */
    @Nullable
    private final CompoundTag compound;

    /**
     * The hash of the current work order (if any).
     */
    @NotNull
    private String workOrderHash;

    /**
     * The snapshot of the previously clicked on warehouse.
     */
    @NotNull
    private Map<String, Integer> warehouseSnapshot;

    /**
     * Constructor for the resource scroll window.
     *
     * @param colonyId     the colony id.
     * @param buildingPos  the building position.
     * @param warehousePos the position of the warehouse clicked on (if any).
     * @param compound     the compound data to store the warehouse snapshot to.
     */
    public WindowResourceList(
      final int colonyId,
      final @NotNull BlockPos buildingPos,
      final @Nullable BlockPos warehousePos,
      final @Nullable CompoundTag compound)
    {
        super(Constants.MOD_ID + RESOURCE_SCROLL_RESOURCE_SUFFIX);

        this.buildingPos = buildingPos;
        this.warehousePos = warehousePos;
        this.compound = compound;
        this.warehouseSnapshot = new HashMap<>();
        this.workOrderHash = "";
        loadWarehouseSnapshotData(compound);

        final IColonyView colonyView = IColonyManager.getInstance().getColonyView(colonyId, Minecraft.getInstance().level.dimension());
        if (colonyView != null)
        {
            final IBuildingView buildingView = colonyView.getBuilding(buildingPos);
            if (buildingView instanceof BuildingBuilder.View)
            {
                this.builder = (BuildingBuilder.View) buildingView;
                return;
            }
        }

        this.builder = null;
    }

    /**
     * Checks the resources in the warehouse to check for any resources required by the builder,
     * only does anything when the warehouse position is provided.
     */
    private void pullResourcesFromWarehouse()
    {
        String currentWorkOrderHash = createWorkOrderHash();
        if (!currentWorkOrderHash.equals(workOrderHash))
        {
            workOrderHash = currentWorkOrderHash;
            warehouseSnapshot = new HashMap<>();
        }

        if (warehousePos != null)
        {
            warehouseSnapshot = new HashMap<>();

            final IBuildingView newView = builder.getColony().getBuilding(builder.getID());
            if (newView instanceof BuildingBuilder.View)
            {
                final BuildingResourcesModuleView moduleView = newView.getModuleViewByType(BuildingResourcesModuleView.class);

                List<BlockPos> containers = builder.getColony().getBuilding(warehousePos).getContainerList();
                for (BlockPos container : containers)
                {
                    final BlockEntity rack = Minecraft.getInstance().level.getBlockEntity(container);
                    if (rack instanceof TileEntityRack)
                    {
                        ((TileEntityRack) rack).getAllContent()
                          .forEach((item, amount) -> {
                              final int hashCode = item.getItemStack().hasTag() ? item.getItemStack().getTag().hashCode() : 0;
                              final String key = item.getItemStack().getDescriptionId() + "-" + hashCode;
                              if (!moduleView.getResources().containsKey(key))
                              {
                                  return;
                              }

                              int oldAmount = warehouseSnapshot.getOrDefault(key, 0);
                              warehouseSnapshot.put(key, oldAmount + amount);
                          });
                    }
                }
            }
        }

        saveWarehouseSnapshotData();
    }

    /**
     * Retrieve resources from the building to display in GUI.
     */
    private void pullResourcesFromHut()
    {
        final IBuildingView newView = builder.getColony().getBuilding(builder.getID());
        if (newView instanceof BuildingBuilder.View)
        {
            final BuildingResourcesModuleView moduleView = newView.getModuleViewByType(BuildingResourcesModuleView.class);
            final Inventory inventory = this.mc.player.getInventory();
            final boolean isCreative = this.mc.player.isCreative();

            final List<Delivery> deliveries = new ArrayList<>();
            for (Map.Entry<Integer, Collection<IToken<?>>> entry : builder.getOpenRequestsByCitizen().entrySet())
            {
                addDeliveryRequestsToList(deliveries, ImmutableList.copyOf(entry.getValue()));
            }

            resources.clear();
            resources.addAll(moduleView.getResources().values());

            double supplied = 0;
            double total = 0;
            for (final BuildingBuilderResource resource : resources)
            {
                final int amountToSet;
                if (isCreative)
                {
                    amountToSet = resource.getAmount();
                }
                else
                {
                    amountToSet =
                      InventoryUtils.getItemCountInItemHandler(new InvWrapper(inventory),
                        stack -> !ItemStackUtils.isEmpty(stack) && ItemStackUtils.compareItemStacksIgnoreStackSize(stack, resource.getItemStack()));
                }

                resource.setPlayerAmount(amountToSet);

                resource.setAmountInDelivery(0);
                for (final Delivery delivery : deliveries)
                {
                    if (ItemStackUtils.compareItemStacksIgnoreStackSize(resource.getItemStack(), delivery.getStack(), false, false))
                    {
                        resource.setAmountInDelivery(resource.getAmountInDelivery() + delivery.getStack().getCount());
                    }
                }
                supplied += Math.min(resource.getAvailable(), resource.getAmount());

                total += resource.getAmount();
            }

            if (total > 0)
            {
                findPaneOfTypeByID(LABEL_PROGRESS, Text.class).setText(Component.translatable("com.minecolonies.coremod.gui.progress.res",
                  (int) ((supplied / total) * 100) + "%",
                  moduleView.getProgress() + "%"));
            }

            resources.sort(new BuildingBuilderResource.ResourceComparator(NOT_NEEDED, HAVE_ENOUGH, IN_DELIVERY, NEED_MORE, DONT_HAVE));
        }
    }

    /**
     * Adds the deliveries of the given tokens to the list
     *
     * @param requestList   list to add to
     * @param tokensToCheck tokens to check
     */
    private void addDeliveryRequestsToList(final List<Delivery> requestList, final ImmutableCollection<IToken<?>> tokensToCheck)
    {
        for (final IToken<?> token : tokensToCheck)
        {
            final IRequest<?> request = builder.getColony().getRequestManager().getRequestForToken(token);
            if (request != null)
            {
                if (request.getRequest() instanceof Delivery && ((Delivery) request.getRequest()).getTarget().getInDimensionLocation().equals(builder.getID()))
                {
                    requestList.add((Delivery) request.getRequest());
                }

                if (request.hasChildren())
                {
                    addDeliveryRequestsToList(requestList, request.getChildren());
                }
            }
        }
    }

    /**
     * Load the snapshot data of the compound data.
     *
     * @param compound the compound data.
     */
    private void loadWarehouseSnapshotData(@Nullable CompoundTag compound)
    {
        if (compound != null)
        {
            final CompoundTag warehouseSnapshotCompound = compound.getCompound(TAG_WAREHOUSE_SNAPSHOT);
            this.warehouseSnapshot = warehouseSnapshotCompound.getAllKeys().stream()
                                       .collect(Collectors.toMap(k -> k, warehouseSnapshotCompound::getInt));
            this.workOrderHash = compound.getString(TAG_WAREHOUSE_SNAPSHOT_WO_HASH);
        }
    }

    /**
     * Save the snapshot data to the compound data.
     */
    private void saveWarehouseSnapshotData()
    {
        if (compound != null)
        {
            CompoundTag newData = new CompoundTag();
            warehouseSnapshot.keySet().forEach(f -> newData.putInt(f, warehouseSnapshot.getOrDefault(f, 0)));
            compound.put(TAG_WAREHOUSE_SNAPSHOT, newData);
            compound.putString(TAG_WAREHOUSE_SNAPSHOT_WO_HASH, workOrderHash);

            Network.getNetwork().sendToServer(new ResourceScrollSaveWarehouseSnapshotMessage(buildingPos, warehouseSnapshot, workOrderHash));
        }
    }

    /**
     * Creates a work order hash from the builder it's next work order.
     *
     * @return the work order hash or an empty string if there's no work order.
     */
    @NotNull
    private String createWorkOrderHash()
    {
        if (builder != null)
        {
            final Optional<IWorkOrderView> currentWorkOrder =
              builder.getColony().getWorkOrders().stream().filter(o -> o.getClaimedBy().equals(buildingPos)).max(Comparator.comparingInt(IWorkOrderView::getPriority));
            if (currentWorkOrder.isPresent())
            {
                long location = currentWorkOrder.get().getLocation().asLong();
                return location + "__" + currentWorkOrder.get().getPackName();
            }
        }
        return "";
    }

    @Override
    public void onOpened()
    {
        final Player player = Minecraft.getInstance().player;
        if (this.builder == null)
        {
            MessageUtils.format(TOOL_RESOURCE_SCROLL_NO_BUILDER).sendTo(player);
            close();
            return;
        }
        super.onOpened();

        pullResourcesFromHut();
        pullResourcesFromWarehouse();

        final ScrollingList resourceList = findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);
        if (resourceList == null)
        {
            MessageUtils.format(TOOL_RESOURCE_SCROLL_ERROR).sendTo(player);
            close();
            return;
        }
        resourceList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return resources.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                updateResourcePane(index, rowPane);
            }
        });

        final BuildingResourcesModuleView moduleView = builder.getModuleViewByType(BuildingResourcesModuleView.class);

        //Make sure we have a fresh view
        Network.getNetwork().sendToServer(new MarkBuildingDirtyMessage(builder));

        findPaneOfTypeByID(LABEL_WORKERNAME, Text.class).setText(Component.literal(builder.getWorkerName()));
        findPaneOfTypeByID(LABEL_CONSTRUCTION_NAME, Text.class).setText(moduleView.getConstructionName());
    }

    /**
     * Update one row pad with its resource informations.
     *
     * @param index   index in the list of resources.
     * @param rowPane The Pane to use to display the information.
     */
    private void updateResourcePane(final int index, @NotNull final Pane rowPane)
    {
        final BuildingBuilderResource resource = resources.get(index);
        final Text resourceLabel = rowPane.findPaneOfTypeByID(RESOURCE_NAME, Text.class);
        final Text resourceMissingLabel = rowPane.findPaneOfTypeByID(RESOURCE_MISSING, Text.class);
        final Text neededLabel = rowPane.findPaneOfTypeByID(RESOURCE_AVAILABLE_NEEDED, Text.class);

        rowPane.findPaneOfTypeByID(IN_DELIVERY_ICON, Image.class).setVisible(false);
        rowPane.findPaneOfTypeByID(IN_DELIVERY_AMOUNT, Text.class).clearText();
        rowPane.findPaneOfTypeByID(IN_WAREHOUSE_ICON, Image.class).setVisible(false);
        rowPane.findPaneOfTypeByID(IN_WAREHOUSE_AMOUNT, Text.class).clearText();

        int resourceHashcode = resource.getItemStack().hasTag() ? resource.getItemStack().getTag().hashCode() : 0;
        int warehouseAmount = warehouseSnapshot.getOrDefault(resource.getItem().getDescriptionId() + "-" + resourceHashcode, 0);

        if (resource.getAmountInDelivery() > 0)
        {
            rowPane.findPaneOfTypeByID(IN_DELIVERY_ICON, Image.class).setVisible(true);
            rowPane.findPaneOfTypeByID(IN_DELIVERY_AMOUNT, Text.class).setText(Component.literal(String.valueOf(resource.getAmountInDelivery())));
        }
        else if (warehouseAmount > 0)
        {
            rowPane.findPaneOfTypeByID(IN_WAREHOUSE_ICON, Image.class).setVisible(true);
            rowPane.findPaneOfTypeByID(IN_DELIVERY_AMOUNT, Text.class).setText(Component.literal(String.valueOf(warehouseAmount)));
        }

        switch (resource.getAvailabilityStatus())
        {
            case DONT_HAVE:
                resourceLabel.setColors(RED);
                resourceMissingLabel.setColors(RED);
                neededLabel.setColors(RED);
                break;
            case NEED_MORE:
                resourceLabel.setColors(ORANGE);
                resourceMissingLabel.setColors(ORANGE);
                neededLabel.setColors(ORANGE);
                break;
            case HAVE_ENOUGH:
                resourceLabel.setColors(DARKGREEN);
                resourceMissingLabel.setColors(DARKGREEN);
                neededLabel.setColors(DARKGREEN);
                break;
            case NOT_NEEDED:
            default:
                resourceLabel.setColors(BLACK);
                resourceMissingLabel.setColors(BLACK);
                neededLabel.setColors(BLACK);
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
        rowPane.findPaneOfTypeByID(RESOURCE_ID, Text.class).setText(Component.literal(Integer.toString(index)));
        rowPane.findPaneOfTypeByID(RESOURCE_QUANTITY_MISSING, Text.class).setText(Component.literal(Integer.toString(resource.getAmount() - resource.getAvailable())));

        final ItemStack stack = new ItemStack(resource.getItem(), 1);
        stack.setTag(resource.getItemStack().getTag());
        rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(stack);
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        pullResourcesFromHut();
        window.findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class).refreshElementPanes();
    }
}
