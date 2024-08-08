package com.minecolonies.core.items;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.workorders.IWorkOrderView;
import com.minecolonies.api.items.ModDataComponents;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.core.client.gui.WindowResourceList;
import com.minecolonies.core.tileentities.TileEntityRack;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.core.colony.buildings.moduleviews.BuildingResourcesModuleView;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.core.network.messages.server.ResourceScrollSaveWarehouseSnapshotMessage;
import com.minecolonies.core.tileentities.TileEntityWareHouse;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.registries.DeferredHolder;
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
     * @param stack the item compound
     * @param player   the player entity opening the window
     */
    private static void openWindow(final ItemStack stack, final Player player)
    {
        final ModDataComponents.ColonyId colonyIdComponent = stack.get(ModDataComponents.COLONY_ID_COMPONENT);
        if (colonyIdComponent == null)
        {
            MessageUtils.format(Component.translatableEscape(TranslationConstants.COM_MINECOLONIES_SCROLL_NO_COLONY)).sendTo(player);
        }

        final ModDataComponents.Pos posComponent = stack.get(ModDataComponents.POS_COMPONENT);
        if (posComponent == null)
        {
            MessageUtils.format(Component.translatableEscape(TranslationConstants.COM_MINECOLONIES_SCROLL_NO_COLONY)).sendTo(player);
        }

        final IColonyView colonyView = IColonyManager.getInstance().getColonyView(colonyIdComponent.id(), colonyIdComponent.dimension());
        if (colonyView != null)
        {
            final IBuildingView buildingView = colonyView.getBuilding(posComponent.pos());
            if (buildingView instanceof BuildingBuilder.View builderBuildingView)
            {
                final String currentHash = getWorkOrderHash(buildingView);
                final WarehouseSnapshot warehouseSnapshotComponent = stack.getOrDefault(ModDataComponents.WAREHOUSE_SNAPSHOT_COMPONENT, WarehouseSnapshot.EMPTY);
                final boolean snapshotNeedsUpdate = !Objects.equals(currentHash, warehouseSnapshotComponent.hash);

                Map<String, Integer> warehouseSnapshot = new HashMap<>();
                if (snapshotNeedsUpdate)
                {
                    // If the hashes no longer match one another, the NBT data is out of sync, inform the server to wipe the NBT.
                    new ResourceScrollSaveWarehouseSnapshotMessage(posComponent.pos()).sendToServer();
                }
                else
                {
                    // If the hashes are still up-to-date, load the old snapshot data from the NBT, if any exists.
                    if (!warehouseSnapshotComponent.hash.isEmpty())
                    {
                        warehouseSnapshot = warehouseSnapshotComponent.snapshot;
                    }
                }

                new WindowResourceList(builderBuildingView, warehouseSnapshot).open();
            }
            else
            {
                MessageUtils.format(Component.translatableEscape(TranslationConstants.COM_MINECOLONIES_SCROLL_NO_COLONY)).sendTo(player);
            }
        }
        else
        {
            MessageUtils.format(Component.translatableEscape(TranslationConstants.COM_MINECOLONIES_SCROLL_NO_COLONY)).sendTo(player);
        }
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
     * Updates the warehouse snapshot.
     *
     * @param warehousePos the position of the warehouse.
     * @param stack        the stack.
     * @param player       the player entity who clicked the warehouse.
     */
    private static void updateWarehouseSnapshot(final BlockPos warehousePos, final ItemStack stack, final Player player)
    {
        final ModDataComponents.ColonyId colonyIdComponent = stack.get(ModDataComponents.COLONY_ID_COMPONENT);
        final ModDataComponents.Pos posComponent = stack.get(ModDataComponents.POS_COMPONENT);

        if (colonyIdComponent == null || posComponent == null)
        {
            MessageUtils.format(COM_MINECOLONIES_SCROLL_NO_COLONY).sendTo(player);
            return;
        }

        final IColonyView colonyView = IColonyManager.getInstance().getColonyView(colonyIdComponent.id(), colonyIdComponent.dimension());
        if (colonyView != null)
        {
            final BlockPos builderPos = posComponent.pos();
            final IBuildingView buildingView = colonyView.getBuilding(builderPos);
            if (buildingView instanceof BuildingBuilder.View)
            {
                final String currentHash = getWorkOrderHash(buildingView);
                final WarehouseSnapshot warehouseSnapshotData = gatherWarehouseSnapshot(buildingView, warehousePos, currentHash, player);

                if (warehouseSnapshotData != null)
                {
                    new ResourceScrollSaveWarehouseSnapshotMessage(builderPos, warehouseSnapshotData.snapshot, warehouseSnapshotData.hash).sendToServer();
                }
                else
                {
                    new ResourceScrollSaveWarehouseSnapshotMessage(builderPos).sendToServer();
                }
            }
        }
    }

    /**
     * Load the map of warehouse items from the given warehouse
     *
     * @param buildingView      the builder building view instance.
     * @param warehouseBlockPos the position of the warehouse.
     * @param hash              the current work order hash.
     * @param player            the player who triggered the resource scroll.
     * @return a map containing the snapshot, or null in case a fault appears.
     */
    @Nullable
    private static ItemResourceScroll.WarehouseSnapshot gatherWarehouseSnapshot(
      final IBuildingView buildingView,
      final BlockPos warehouseBlockPos,
      final String hash,
      final Player player)
    {
        final IBuildingView warehouse = buildingView.getColony().getBuilding(warehouseBlockPos);

        if (warehouse == null)
        {
            MessageUtils.format(COM_MINECOLONIES_SCROLL_WRONG_COLONY).sendTo(player);
            return null;
        }

        if (hash.isBlank())
        {
            return null;
        }

        final BuildingResourcesModuleView resourcesModule = buildingView.getModuleViewByType(BuildingResourcesModuleView.class);

        final Map<String, Integer> items = new HashMap<>();
        for (final BlockPos container : warehouse.getContainerList())
        {
            final BlockEntity blockEntity = warehouse.getColony().getWorld().getBlockEntity(container);
            if (blockEntity instanceof TileEntityRack rack)
            {
                rack.getAllContent().forEach((item, amount) -> {
                    final int hashCode = item.getItemStack().getComponentsPatch().hashCode();
                    final String key = item.getItemStack().getDescriptionId() + "-" + hashCode;
                    if (!resourcesModule.getResources().containsKey(key))
                    {
                        return;
                    }

                    int oldAmount = items.getOrDefault(key, 0);
                    items.put(key, oldAmount + amount);
                });
            }
        }

        return new WarehouseSnapshot(items, hash);
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

        final BlockEntity entity = ctx.getLevel().getBlockEntity(ctx.getClickedPos());

        if (ctx.getLevel().isClientSide)
        {
            if (entity instanceof AbstractTileEntityColonyBuilding buildingEntity)
            {
                if (buildingEntity instanceof TileEntityWareHouse)
                {
                    updateWarehouseSnapshot(buildingEntity.getTilePos(), scroll, ctx.getPlayer());
                }
            }
            else
            {
                openWindow(scroll, ctx.getPlayer());
            }
        }
        else if (entity instanceof AbstractTileEntityColonyBuilding buildingEntity)
        {
            if (buildingEntity.getBuilding() instanceof BuildingBuilder)
            {
                scroll.set(ModDataComponents.COLONY_ID_COMPONENT, new ModDataComponents.ColonyId(buildingEntity.getColonyId(), buildingEntity.getLevel().dimension()));
                scroll.set(ModDataComponents.POS_COMPONENT, new ModDataComponents.Pos(buildingEntity.getPosition()));

                MessageUtils.format(COM_MINECOLONIES_SCROLL_BUILDING_SET, buildingEntity.getColony().getName()).sendTo(ctx.getPlayer());
            }
            else if (buildingEntity.getBuilding() instanceof BuildingWareHouse)
            {
                MessageUtils.format(COM_MINECOLONIES_SCROLL_SNAPSHOT).sendTo(ctx.getPlayer());
            }
            else
            {
                final MutableComponent buildingTypeComponent = MessageUtils.format(buildingEntity.getBuilding().getBuildingType().getTranslationKey()).create();
                MessageUtils.format(COM_MINECOLONIES_SCROLL_WRONG_BUILDING, buildingTypeComponent, buildingEntity.getColony().getName()).sendTo(ctx.getPlayer());
            }
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

        openWindow(clipboard, playerIn);

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, clipboard);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable TooltipContext ctx, List<Component> tooltip, TooltipFlag flagIn)
    {
        super.appendHoverText(stack, ctx, tooltip, flagIn);

        final ModDataComponents.ColonyId colonyIdComponent = stack.get(ModDataComponents.COLONY_ID_COMPONENT);
        final ModDataComponents.Pos posComponent = stack.get(ModDataComponents.POS_COMPONENT);

        if (colonyIdComponent == null || posComponent == null)
        {
            super.appendHoverText(stack, ctx, tooltip, flagIn);
            return;
        }

        final IColonyView colonyView = IColonyManager.getInstance().getColonyView(colonyIdComponent.id(), colonyIdComponent.dimension());
        if (colonyView != null)
        {
            final IBuildingView buildingView = colonyView.getBuilding(posComponent.pos());
            if (buildingView instanceof BuildingBuilder.View builderBuildingView)
            {
                String name = builderBuildingView.getWorkerName();
                tooltip.add(name != null && !name.trim().isEmpty()
                              ? Component.literal(ChatFormatting.DARK_PURPLE + name)
                              : Component.translatableEscape(COM_MINECOLONIES_SCROLL_BUILDING_NO_WORKER));
            }
        }
    }

    /**
     * Container class for warehouse snapshot data.
     *
     * @param snapshot the snapshot data.
     * @param hash     the work order hash for comparison between work orders.
     */
    public record WarehouseSnapshot(Map<String, Integer> snapshot, String hash)
    {
        public static       DeferredHolder<DataComponentType<?>, DataComponentType<WarehouseSnapshot>> TYPE  = null;
        public static final WarehouseSnapshot                                                          EMPTY = new WarehouseSnapshot(new HashMap<>(), "");

        public static final Codec<WarehouseSnapshot> CODEC = RecordCodecBuilder.create(
          builder -> builder
                       .group(Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("snapshot").forGetter(WarehouseSnapshot::snapshot),
                         Codec.STRING.fieldOf("hash").forGetter(WarehouseSnapshot::hash))
                       .apply(builder, WarehouseSnapshot::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, WarehouseSnapshot> STREAM_CODEC =
          StreamCodec.composite(ByteBufCodecs.fromCodec(Codec.unboundedMap(Codec.STRING, Codec.INT)),
            WarehouseSnapshot::snapshot, ByteBufCodecs.fromCodec(Codec.STRING), WarehouseSnapshot::hash,
            WarehouseSnapshot::new);
    }
}
