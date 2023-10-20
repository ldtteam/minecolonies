package com.minecolonies.coremod.items;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.colony.workorders.IWorkOrderView;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.TileEntityRack;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.modules.BuildingResourcesModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Class describing the resource scroll item.
 */
public class ItemResourceScroll extends AbstractItemMinecolonies
{
    /**
     * Sets the name, creative tab, and registers the resource scroll item.
     *
     * @param properties the properties.
     */
    public ItemResourceScroll(final Item.Properties properties)
    {
        super("resourcescroll", properties.stacksTo(STACKSIZE));
    }

    /**
     * Opens the scroll window if there is a valid builder linked
     *
     * @param compound the item compound
     * @param player   the player entity opening the window
     */
    private static void openWindow(final CompoundTag compound, final Player player)
    {
        final int colonyId = compound.getInt(TAG_COLONY_ID);
        final BlockPos builderPos = compound.contains(TAG_BUILDER) ? BlockPosUtil.read(compound, TAG_BUILDER) : null;

        final IColonyView colonyView = IColonyManager.getInstance().getColonyView(colonyId, Minecraft.getInstance().level.dimension());
        if (colonyView != null)
        {
            final IBuildingView buildingView = colonyView.getBuilding(builderPos);
            if (buildingView instanceof BuildingBuilder.View builderBuildingView)
            {
                Map<String, Integer> warehouseSnapshot = new HashMap<>();
                if (compound.contains(TAG_WAREHOUSE_SNAPSHOT) && compound.contains(TAG_WAREHOUSE_SNAPSHOT_WO_HASH))
                {
                    final String currentWorkOrderHash = getWorkOrderHash(buildingView);
                    if (currentWorkOrderHash.equals(compound.getString(TAG_WAREHOUSE_SNAPSHOT_WO_HASH)))
                    {
                        final CompoundTag warehouseSnapshotCompound = compound.getCompound(TAG_WAREHOUSE_SNAPSHOT);
                        warehouseSnapshot = warehouseSnapshotCompound.getAllKeys().stream()
                                              .collect(Collectors.toMap(k -> k, warehouseSnapshotCompound::getInt));
                    }
                }

                MineColonies.proxy.openResourceScrollWindow(builderBuildingView, warehouseSnapshot);
            }
            else
            {
                MessageUtils.format(Component.translatable(TranslationConstants.COM_MINECOLONIES_SCROLL_NO_COLONY)).sendTo(player);
            }
        }
        else
        {
            MessageUtils.format(Component.translatable(TranslationConstants.COM_MINECOLONIES_SCROLL_NO_COLONY)).sendTo(player);
        }
    }

    /**
     * Creates a work order hash from a work order.
     *
     * @param building the building instance.
     * @return the work order hash.
     */
    @NotNull
    private static String getWorkOrderHash(final IBuilding building)
    {
        final Optional<IWorkOrder> currentWorkOrder = building.getColony()
                                                        .getWorkManager()
                                                        .getOrderedList(w -> true, building.getID())
                                                        .stream()
                                                        .findFirst();
        if (currentWorkOrder.isEmpty())
        {
            return "";
        }
        long location = currentWorkOrder.get().getLocation().asLong();
        return location + "__" + currentWorkOrder.get().getStructurePack();
    }

    /**
     * Creates a work order hash from a work order view.
     *
     * @param buildingView the building view instance.
     * @return the work order hash.
     */
    @NotNull
    private static String getWorkOrderHash(final IBuildingView buildingView)
    {
        final Optional<IWorkOrderView> currentWorkOrder = buildingView.getColony()
                                                            .getWorkOrders()
                                                            .stream()
                                                            .filter(o -> o.getClaimedBy().equals(buildingView.getID()))
                                                            .max(Comparator.comparingInt(IWorkOrderView::getPriority));
        if (currentWorkOrder.isEmpty())
        {
            return "";
        }

        long location = currentWorkOrder.get().getLocation().asLong();
        return location + "__" + currentWorkOrder.get().getPackName();
    }

    /**
     * Used when clicking on block in world.
     *
     * @param ctx the context of use.
     * @return the result
     */
    @Override
    @NotNull
    public InteractionResult useOn(UseOnContext ctx)
    {
        final ItemStack scroll = ctx.getPlayer().getItemInHand(ctx.getHand());

        final CompoundTag compound = scroll.getOrCreateTag();
        final BlockEntity entity = ctx.getLevel().getBlockEntity(ctx.getClickedPos());

        if (entity instanceof AbstractTileEntityColonyBuilding buildingEntity && buildingEntity.getBuilding() != null)
        {
            if (buildingEntity.getBuilding().getBuildingType().equals(ModBuildings.builder.get()))
            {
                compound.putInt(TAG_COLONY_ID, buildingEntity.getColonyId());
                BlockPosUtil.write(compound, TAG_BUILDER, buildingEntity.getPosition());

                if (!ctx.getLevel().isClientSide)
                {
                    MessageUtils.format(COM_MINECOLONIES_SCROLL_BUILDING_SET, buildingEntity.getColony().getName()).sendTo(ctx.getPlayer());
                }
            }
            else if (buildingEntity.getBuilding().getBuildingType().equals(ModBuildings.wareHouse.get()))
            {
                if (!ctx.getLevel().isClientSide)
                {
                    final WorkOrderSnapshot warehouseSnapshot =
                      gatherWarehouseSnapshot(buildingEntity, compound.contains(TAG_BUILDER) ? BlockPosUtil.read(compound, TAG_BUILDER) : null, ctx.getPlayer());

                    if (warehouseSnapshot != null)
                    {
                        CompoundTag snapshotData = new CompoundTag();
                        warehouseSnapshot.snapshot.keySet()
                          .forEach(itemKey -> compound.putInt(itemKey, warehouseSnapshot.snapshot.getOrDefault(itemKey, 0)));
                        compound.put(TAG_WAREHOUSE_SNAPSHOT, snapshotData);
                        compound.putString(TAG_WAREHOUSE_SNAPSHOT_WO_HASH, warehouseSnapshot.hash);
                    }
                    else
                    {
                        compound.remove(TAG_WAREHOUSE_SNAPSHOT);
                        compound.remove(TAG_WAREHOUSE_SNAPSHOT_WO_HASH);
                    }
                }
            }
            else
            {
                if (!ctx.getLevel().isClientSide)
                {
                    final MutableComponent buildingTypeComponent = MessageUtils.format(buildingEntity.getBuilding().getBuildingType().getTranslationKey()).create();
                    MessageUtils.format(COM_MINECOLONIES_SCROLL_WRONG_BUILDING, buildingTypeComponent, buildingEntity.getColony().getName()).sendTo(ctx.getPlayer());
                }
            }
        }
        else if (ctx.getLevel().isClientSide)
        {
            openWindow(compound, ctx.getPlayer());
        }

        return InteractionResult.SUCCESS;
    }

    /**
     * Handles mid air use.
     *
     * @param worldIn  the world
     * @param playerIn the player
     * @param hand     the hand
     * @return the result
     */
    @Override
    @NotNull
    public InteractionResultHolder<ItemStack> use(
      final Level worldIn,
      final Player playerIn,
      final InteractionHand hand)
    {
        final ItemStack clipboard = playerIn.getItemInHand(hand);

        if (!worldIn.isClientSide)
        {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, clipboard);
        }

        openWindow(clipboard.getOrCreateTag(), playerIn);

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, clipboard);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
    {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        if (worldIn == null)
        {
            return;
        }

        final CompoundTag compound = stack.getOrCreateTag();
        final int colonyId = compound.getInt(TAG_COLONY_ID);
        final BlockPos builderPos = BlockPosUtil.read(compound, TAG_BUILDER);

        final IColonyView colonyView = IColonyManager.getInstance().getColonyView(colonyId, worldIn.dimension());
        if (colonyView != null)
        {
            final IBuildingView buildingView = colonyView.getBuilding(builderPos);
            if (buildingView instanceof BuildingBuilder.View builderBuildingView)
            {
                String name = builderBuildingView.getWorkerName();
                tooltip.add(name != null && !name.trim().isEmpty()
                              ? Component.literal(ChatFormatting.DARK_PURPLE + name)
                              : Component.translatable(COM_MINECOLONIES_SCROLL_BUILDING_NO_WORKER));
            }
        }
    }

    /**
     * Load the map of warehouse items from the given warehouse
     *
     * @param warehouseTileEntity the tile entity of the clicked warehouse.
     * @param builderPos          the position of the linked builder.
     * @param player              the player who triggered the resource scroll.
     * @return a map containing the snapshot, or null in case a fault appears.
     */
    @Nullable
    private WorkOrderSnapshot gatherWarehouseSnapshot(final AbstractTileEntityColonyBuilding warehouseTileEntity, @Nullable final BlockPos builderPos, final Player player)
    {
        final IBuilding builder = warehouseTileEntity.getColony().getBuildingManager().getBuilding(builderPos);
        if (builder == null)
        {
            MessageUtils.format(COM_MINECOLONIES_SCROLL_WRONG_COLONY).sendTo(player);
            return null;
        }

        final String hash = getWorkOrderHash(builder);

        if (hash.isBlank())
        {
            return null;
        }

        final BuildingResourcesModule resourcesModule = builder.getFirstModuleOccurance(BuildingResourcesModule.class);
        final IBuilding warehouse = warehouseTileEntity.getColony().getBuildingManager().getBuilding(warehouseTileEntity.getTilePos());

        final Map<String, Integer> items = new HashMap<>();
        for (final BlockPos container : warehouse.getContainers())
        {
            final BlockEntity blockEntity = warehouse.getColony().getWorld().getBlockEntity(container);
            if (blockEntity instanceof TileEntityRack rack)
            {
                rack.getAllContent().forEach((item, amount) -> {
                    final String key = item.getItemStack().getDescriptionId();
                    if (!resourcesModule.getNeededResources().containsKey(key))
                    {
                        return;
                    }

                    int oldAmount = items.getOrDefault(key, 0);
                    items.put(key, oldAmount + amount);
                });
            }
        }

        return new WorkOrderSnapshot(items, hash);
    }

    /**
     * Container class for work order snapshot data.
     *
     * @param snapshot the snapshot data.
     * @param hash     the work order hash for comparison between work orders.
     */
    private record WorkOrderSnapshot(Map<String, Integer> snapshot, String hash)
    {
    }
}
