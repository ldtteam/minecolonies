package com.minecolonies.api.colony.buildingextensions;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.buildingextensions.modules.IBuildingExtensionModule;
import com.minecolonies.api.colony.buildingextensions.registry.BuildingExtensionRegistries;
import com.minecolonies.api.colony.modules.IModuleContainer;
import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;

/**
 * Interface for building extension instances.
 */
public interface IBuildingExtension extends IModuleContainer<IBuildingExtensionModule>
{
    /**
     * Return the building extension type for this building extension.
     *
     * @return the building extension registry entry.
     */
    @NotNull BuildingExtensionRegistries.BuildingExtensionEntry getBuildingExtensionType();

    /**
     * Gets the position of the building extension.
     *
     * @return central location of the building extension.
     */
    @NotNull BlockPos getPosition();

    /**
     * Getter for the owning building of the building extension.
     *
     * @return the id or null.
     */
    @Nullable BlockPos getBuildingId();

    /**
     * Sets the owning building of the building extension.
     *
     * @param buildingId id of the building.
     */
    void setBuilding(final BlockPos buildingId);

    /**
     * Resets the ownership of the building extension.
     */
    void resetOwningBuilding();

    /**
     * Has the building extension been taken.
     *
     * @return true if the building extension is not free to use, false after releasing it.
     */
    boolean isTaken();

    /**
     * Get the distance to a building.
     *
     * @param building the building to get the distance to.
     * @return the distance
     */
    int getSqDistance(IBuildingView building);

    /**
     * Stores the NBT data of the building extension.
     */
    @NotNull CompoundTag serializeNBT();

    /**
     * Reconstruct the building extension from the given NBT data.
     *
     * @param compound the compound to read from.
     */
    void deserializeNBT(@NotNull CompoundTag compound);

    /**
     * Serialize a building extension to a buffer.
     *
     * @param buf the buffer to write the building extension data to.
     */
    void serialize(@NotNull FriendlyByteBuf buf);

    /**
     * Deserialize a building extension from a buffer.
     *
     * @param buf the buffer to read the building extension data from.
     */
    void deserialize(@NotNull FriendlyByteBuf buf);

    /**
     * Condition to check whether this building extension instance is currently properly placed down.
     *
     * @param colony the colony this building extension is in.
     * @return true if the building extension is correctly placed at the current position.
     */
    boolean isValidPlacement(IColony colony);

    /**
     * Hashcode implementation for this building extension.
     */
    int hashCode();

    /**
     * Equals implementation for this building extension.
     */
    boolean equals(Object other);

    /**
     * Get the unique extension id.
     * @return the unique id.
     */
    ExtensionId getId();

    /**
     * Unique extension id.
     * @param pos the pos it's at.
     * @param entry it's entry type.
     */
    record ExtensionId(BlockPos pos, BuildingExtensionRegistries.BuildingExtensionEntry entry)
    {
        public Tag serializeNBT()
        {
            final CompoundTag tag = new CompoundTag();
            BlockPosUtil.write(tag, TAG_POS, pos);
            tag.putString(TAG_ID, entry.getRegistryName().toString());
            return tag;
        }

        public static ExtensionId deserializeNBT(final CompoundTag nbt)
        {
            return new ExtensionId(BlockPosUtil.read(nbt, TAG_POS), BuildingExtensionRegistries.getBuildingExtensionRegistry().getValue(ResourceLocation.tryParse(nbt.getString(TAG_ID))));
        }
    }
}
