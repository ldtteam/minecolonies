package com.minecolonies.api.items;

import com.ldtteam.structurize.api.constants.Constants;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.core.items.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

public class ModDataComponents
{
    public static final DeferredRegister.DataComponents REGISTRY = DeferredRegister.createDataComponents(Constants.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemScanAnalyzer.Timestamp>> TIME_COMPONENT =
      savedSynced("timestamp", ItemScanAnalyzer.Timestamp.CODEC, ItemScanAnalyzer.Timestamp.STREAM_CODEC);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemSupplyChestDeployer.SupplyData>> SUPPLY_COMPONENT =
      savedSynced("supplies", ItemSupplyChestDeployer.SupplyData.CODEC, ItemSupplyChestDeployer.SupplyData.STREAM_CODEC);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemScepterGuard.LastPos>> LAST_POS_COMPONENT =
      savedSynced("last_pos", ItemScepterGuard.LastPos.CODEC, ItemScepterGuard.LastPos.STREAM_CODEC);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemResourceScroll.WarehouseSnapshot>> WAREHOUSE_SNAPSHOT_COMPONENT =
      savedSynced("warehouse_snapshot", ItemResourceScroll.WarehouseSnapshot.CODEC, ItemResourceScroll.WarehouseSnapshot.STREAM_CODEC);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemBannerRallyGuards.RallyData>> RALLY_COMPONENT =
      savedSynced("rally", ItemBannerRallyGuards.RallyData.CODEC, ItemBannerRallyGuards.RallyData.STREAM_CODEC);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemAdventureToken.AdventureData>> ADVENTURE_COMPONENT =
      savedSynced("adventure", ItemAdventureToken.AdventureData.CODEC, ItemAdventureToken.AdventureData.STREAM_CODEC);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ColonyId>> COLONY_ID_COMPONENT =
      savedSynced("colonyid", ColonyId.CODEC, ColonyId.STREAM_CODEC);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Pos>> POS_COMPONENT =
      savedSynced("pos", Pos.CODEC, Pos.STREAM_CODEC);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Desc>> DESC_COMPONENT =
      savedSynced("desc", Desc.CODEC, Desc.STREAM_CODEC);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<HutBlockData>> HUT_COMPONENT =
      savedSynced("hut", HutBlockData.CODEC, HutBlockData.STREAM_CODEC);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Bool>> BOOL_COMPONENT =
      savedSynced("bool", Bool.CODEC, Bool.STREAM_CODEC);

    private static <D> DeferredHolder<DataComponentType<?>, DataComponentType<D>> savedSynced(final String name,
      final Codec<D> codec,
      final StreamCodec<RegistryFriendlyByteBuf, D> streamCodec)
    {
        return REGISTRY.register(name,
          () -> DataComponentType.<D>builder().persistent(codec).networkSynchronized(streamCodec).build());
    }

    public record ColonyId(int id, ResourceKey<Level> dimension)
    {
        public static final ColonyId EMPTY = new ColonyId(-1, Level.OVERWORLD);

        public static final Codec<ColonyId> CODEC = RecordCodecBuilder.create(
          builder -> builder
                       .group(Codec.INT.fieldOf("colony_id").forGetter(ColonyId::id),
                         Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(ColonyId::dimension))
                       .apply(builder, ColonyId::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ColonyId> STREAM_CODEC =
          StreamCodec.composite(ByteBufCodecs.VAR_INT,
            ColonyId::id, ResourceKey.streamCodec(Registries.DIMENSION), ColonyId::dimension,
            ColonyId::new);

        public void writeToItemStack(final ItemStack itemStack)
        {
            itemStack.set(ModDataComponents.COLONY_ID_COMPONENT, this);
        }

        public static ColonyId readFromItemStack(final ItemStack itemStack)
        {
            return itemStack.getOrDefault(ModDataComponents.COLONY_ID_COMPONENT, ColonyId.EMPTY);
        }

        @Nullable
        public static IColony readColonyFromItemStack(final ItemStack itemStack)
        {
            final ColonyId colonyId = readFromItemStack(itemStack);
            return colonyId == EMPTY ? null : IColonyManager.getInstance().getColonyByDimension(colonyId.id(), colonyId.dimension());
        }

        @Nullable
        public static IColonyView readColonyViewFromItemStack(final ItemStack itemStack)
        {
            final ColonyId colonyId = readFromItemStack(itemStack);
            return colonyId == EMPTY ? null : IColonyManager.getInstance().getColonyView(colonyId.id(), colonyId.dimension());
        }

        public static void updateItemStack(final ItemStack itemStack, final UnaryOperator<ColonyId> updater)
        {
            updater.apply(readFromItemStack(itemStack)).writeToItemStack(itemStack);
        }
    }

    public record Pos(BlockPos pos)
    {
        public static final Pos EMPTY = new Pos(BlockPos.ZERO);

        public static final Codec<Pos> CODEC = RecordCodecBuilder.create(
          builder -> builder
                       .group(BlockPos.CODEC.fieldOf("pos").forGetter(Pos::pos))
                       .apply(builder, Pos::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Pos> STREAM_CODEC =
          StreamCodec.composite(BlockPos.STREAM_CODEC,
            Pos::pos,
            Pos::new);

        public void writeToItemStack(final ItemStack itemStack)
        {
            itemStack.set(ModDataComponents.POS_COMPONENT, this);
        }

        public static Pos readFromItemStack(final ItemStack itemStack)
        {
            return itemStack.getOrDefault(ModDataComponents.POS_COMPONENT, Pos.EMPTY);
        }

        public static void updateItemStack(final ItemStack itemStack, final UnaryOperator<Pos> updater)
        {
            updater.apply(readFromItemStack(itemStack)).writeToItemStack(itemStack);
        }
    }

    public record Desc(String desc)
    {
        public static final Desc EMPTY = new Desc("");

        public static final Codec<Desc> CODEC = RecordCodecBuilder.create(
          builder -> builder
                       .group(Codec.STRING.fieldOf("desc").forGetter(Desc::desc))
                       .apply(builder, Desc::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Desc> STREAM_CODEC =
          StreamCodec.composite(ByteBufCodecs.STRING_UTF8,
            Desc::desc,
            Desc::new);

        public void writeToItemStack(final ItemStack itemStack)
        {
            itemStack.set(ModDataComponents.DESC_COMPONENT, this);
        }

        public static Desc readFromItemStack(final ItemStack itemStack)
        {
            return itemStack.getOrDefault(ModDataComponents.DESC_COMPONENT, Desc.EMPTY);
        }

        public static void updateItemStack(final ItemStack itemStack, final UnaryOperator<Desc> updater)
        {
            updater.apply(readFromItemStack(itemStack)).writeToItemStack(itemStack);
        }
    }

    public record Bool(boolean does)
    {
        public static final Bool EMPTY = new Bool(false);

        public static final Codec<Bool> CODEC = RecordCodecBuilder.create(
          builder -> builder
                       .group(Codec.BOOL.fieldOf("bool").forGetter(Bool::does))
                       .apply(builder, Bool::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Bool> STREAM_CODEC =
          StreamCodec.composite(ByteBufCodecs.BOOL,
            Bool::does,
            Bool::new);

        public void writeToItemStack(final ItemStack itemStack)
        {
            itemStack.set(ModDataComponents.BOOL_COMPONENT, this);
        }

        public static Bool readFromItemStack(final ItemStack itemStack)
        {
            return itemStack.getOrDefault(ModDataComponents.BOOL_COMPONENT, Bool.EMPTY);
        }

        public static void updateItemStack(final ItemStack itemStack, final UnaryOperator<Bool> updater)
        {
            updater.apply(readFromItemStack(itemStack)).writeToItemStack(itemStack);
        }
    }

    public record HutBlockData(int level, boolean pastable)
    {
        public static final Codec<HutBlockData> CODEC = RecordCodecBuilder.create(
          builder -> builder
                       .group(Codec.INT.fieldOf("level").forGetter(HutBlockData::level),
                         Codec.BOOL.fieldOf("pastable").forGetter(HutBlockData::pastable))
                       .apply(builder, HutBlockData::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, HutBlockData> STREAM_CODEC =
          StreamCodec.composite(ByteBufCodecs.VAR_INT,
            HutBlockData::level, ByteBufCodecs.BOOL, HutBlockData::pastable,
            HutBlockData::new);

        public void writeToItemStack(final ItemStack itemStack)
        {
            itemStack.set(ModDataComponents.HUT_COMPONENT, this);
        }

        @Nullable
        public static HutBlockData readFromItemStack(final ItemStack itemStack)
        {
            return itemStack.get(ModDataComponents.HUT_COMPONENT);
        }

        public static void updateItemStack(final ItemStack itemStack, final UnaryOperator<HutBlockData> updater)
        {
            updater.apply(readFromItemStack(itemStack)).writeToItemStack(itemStack);
        }
    }
}
