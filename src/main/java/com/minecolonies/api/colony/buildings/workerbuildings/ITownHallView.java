package com.minecolonies.api.colony.buildings.workerbuildings;

import com.mojang.serialization.DataResult;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.colonyEvents.descriptions.IColonyEventDescription;
import com.minecolonies.api.colony.permissions.PermissionEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.List;

public interface ITownHallView extends IBuildingView
{
    /**
     * Get a list of permission events.
     *
     * @return a copy of the list of events.
     */
    List<PermissionEvent> getPermissionEvents();

    /**
     * Gets a list if colony events.
     * 
     * @return a copy of the list of events.
     */
    List<IColonyEventDescription> getColonyEvents();

    /**
     * Check if the player can use the teleport command.
     *
     * @return true if so.
     */
    boolean canPlayerUseTP();

    /**
     * Getter for the mapdata.
     * @return the original list.
     */
    List<MapEntry> getMapDataList();

    public record MapEntry(MapId mapId, MapItemSavedData mapData)
    {
        public static final StreamCodec<RegistryFriendlyByteBuf, MapEntry> STREAM_CODEC =
            StreamCodec.composite(MapId.STREAM_CODEC,
                MapEntry::mapId,
                StreamCodec.of(MapEntry::encodeMapData, MapEntry::decodeMapData),
                MapEntry::mapData,
                MapEntry::new);

        private static CompoundTag encodeMapData(final RegistryFriendlyByteBuf buffer, final MapItemSavedData mapData)
        {
            final DataResult<Tag> result = MapItemSavedData.CODEC.encodeStart(
                buffer.registryAccess().createSerializationContext(NbtOps.INSTANCE), mapData);
            return result.resultOrPartial(error -> {
                throw new IllegalArgumentException("Could not encode map data: " + error);
            }).map(tag -> tag instanceof CompoundTag compound ? compound : new CompoundTag()).orElseThrow();
        }

        private static MapItemSavedData decodeMapData(final RegistryFriendlyByteBuf buffer)
        {
            final CompoundTag nbt = buffer.readNbt();
            if (nbt == null)
            {
                throw new IllegalArgumentException("Missing map data payload");
            }

            return MapItemSavedData.CODEC.parse(
                buffer.registryAccess().createSerializationContext(NbtOps.INSTANCE), nbt)
                .resultOrPartial(error -> {
                    throw new IllegalArgumentException("Could not decode map data: " + error);
                }).orElseThrow();
        }
    }
}
