package com.minecolonies.api.items;

import com.ldtteam.structurize.api.constants.Constants;
import com.minecolonies.core.items.ItemScanAnalyzer;
import com.minecolonies.core.items.ItemScepterGuard;
import com.minecolonies.core.items.ItemSupplyChestDeployer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents
{
    public static final DeferredRegister.DataComponents REGISTRY = DeferredRegister.createDataComponents(Constants.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemScanAnalyzer.Timestamp>> TIME_COMPONENT =
      savedSynced("timestamp", ItemScanAnalyzer.Timestamp.CODEC, ItemScanAnalyzer.Timestamp.STREAM_CODEC);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemSupplyChestDeployer.SupplyData>> SUPPLY_COMPONENT =
      savedSynced("supplies", ItemSupplyChestDeployer.SupplyData.CODEC, ItemSupplyChestDeployer.SupplyData.STREAM_CODEC);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemScepterGuard.LastPos>> LAST_POS_COMPONENT =
      savedSynced("last_pos", ItemScepterGuard.LastPos.CODEC, ItemScepterGuard.LastPos.STREAM_CODEC);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ColonyId>> COLONY_ID_COMPONENT =
      savedSynced("colonyid", ColonyId.CODEC, ColonyId.STREAM_CODEC);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Pos>> POS_COMPONENT =
      savedSynced("pos", Pos.CODEC, Pos.STREAM_CODEC);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Desc>> DESC_COMPONENT =
      savedSynced("desc", Desc.CODEC, Desc.STREAM_CODEC);

    static
    {
        ItemScanAnalyzer.Timestamp.TYPE = TIME_COMPONENT;
        ItemSupplyChestDeployer.SupplyData.TYPE = SUPPLY_COMPONENT;
        ItemScepterGuard.LastPos.TYPE = LAST_POS_COMPONENT;

        ColonyId.TYPE = COLONY_ID_COMPONENT;
        Pos.TYPE = POS_COMPONENT;
        Desc.TYPE = DESC_COMPONENT;
    }

    private static <D> DeferredHolder<DataComponentType<?>, DataComponentType<D>> savedSynced(final String name,
      final Codec<D> codec,
      final StreamCodec<RegistryFriendlyByteBuf, D> streamCodec)
    {
        return REGISTRY.register(name,
          () -> DataComponentType.<D>builder().persistent(codec).networkSynchronized(streamCodec).build());
    }

    public record ColonyId(int id, ResourceKey<Level> dimension)
    {
        public static       DeferredHolder<DataComponentType<?>, DataComponentType<ColonyId>> TYPE  = null;
        public static final ColonyId EMPTY = new ColonyId(-1, Level.OVERWORLD);

        public static final Codec<ColonyId> CODEC = RecordCodecBuilder.create(
          builder -> builder
                       .group(Codec.INT.fieldOf("colony_id").forGetter(ColonyId::id),
                         Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(ColonyId::dimension))
                       .apply(builder, ColonyId::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ColonyId> STREAM_CODEC =
          StreamCodec.composite(ByteBufCodecs.fromCodec(Codec.INT),
            ColonyId::id, ByteBufCodecs.fromCodec(Level.RESOURCE_KEY_CODEC), ColonyId::dimension,
            ColonyId::new);
    }

    public record Pos(BlockPos pos)
    {
        public static       DeferredHolder<DataComponentType<?>, DataComponentType<Pos>> TYPE  = null;
        public static final Pos EMPTY = new Pos(BlockPos.ZERO);

        public static final Codec<Pos> CODEC = RecordCodecBuilder.create(
          builder -> builder
                       .group(BlockPos.CODEC.fieldOf("pos").forGetter(Pos::pos))
                       .apply(builder, Pos::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Pos> STREAM_CODEC =
          StreamCodec.composite(ByteBufCodecs.fromCodec(BlockPos.CODEC),
            Pos::pos,
            Pos::new);
    }

    public record Desc(String desc)
    {
        public static       DeferredHolder<DataComponentType<?>, DataComponentType<Desc>> TYPE  = null;
        public static final Desc EMPTY = new Desc("");

        public static final Codec<Desc> CODEC = RecordCodecBuilder.create(
          builder -> builder
                       .group(Codec.STRING.fieldOf("desc").forGetter(Desc::desc))
                       .apply(builder, Desc::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Desc> STREAM_CODEC =
          StreamCodec.composite(ByteBufCodecs.fromCodec(Codec.STRING),
            Desc::desc,
            Desc::new);
    }
}
