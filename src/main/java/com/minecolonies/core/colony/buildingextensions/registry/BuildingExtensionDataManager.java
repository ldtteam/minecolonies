package com.minecolonies.core.colony.buildingextensions.registry;

import com.minecolonies.api.colony.buildingextensions.IBuildingExtension;
import com.minecolonies.api.colony.buildingextensions.registry.BuildingExtensionRegistries;
import com.minecolonies.api.colony.buildingextensions.registry.BuildingExtensionRegistries.BuildingExtensionEntry;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * The building extension manager class responsible for creating building extension instances from NBT data, etc.
 */
public final class BuildingExtensionDataManager
{
    private static final String TAG_EXTENSION_NAME     = "name";
    private static final String TAG_EXTENSION_POSITION = "position";
    private static final String TAG_EXTENSION_DATA     = "data";

    private BuildingExtensionDataManager()
    {
    }

    /**
     * Creates a building extension instance from NBT compound data.
     *
     * @param compound the input compound data.
     * @return the created building extension instance.
     */
    public static IBuildingExtension compoundToExtension(@NotNull final HolderLookup.Provider provider, final @NotNull CompoundTag compound)
    {
        final ResourceLocation name = ResourceLocation.parse(compound.getString(TAG_EXTENSION_NAME));
        final BlockPos position = BlockPosUtil.read(compound, TAG_EXTENSION_POSITION);

        final IBuildingExtension extension = resourceLocationToExtension(name, position);
        if (extension != null)
        {
            extension.deserializeNBT(provider, compound.getCompound(TAG_EXTENSION_DATA));
        }
        return extension;
    }

    /**
     * Creates a building extension instance from a building extension type and position.
     *
     * @param registryName the building extension registry entry name.
     * @param position  the position of the building extension.
     * @return the building extension instance.
     */
    public static IBuildingExtension resourceLocationToExtension(final @NotNull ResourceLocation registryName, final @NotNull BlockPos position)
    {
        final BuildingExtensionEntry entry = BuildingExtensionRegistries.getBuildingExtensionRegistry().get(registryName);

        if (entry == null)
        {
            Log.getLogger().error("Unknown building extension type '{}'.", registryName);
            return null;
        }

        return entry.produceExtension(position);
    }

    /**
     * Creates a building extension instance from a complete network buffer.
     *
     * @param buf the buffer, still containing the building extension registry type and position.
     * @return the building extension instance.
     */
    public static IBuildingExtension bufferToExtension(final @NotNull RegistryFriendlyByteBuf buf)
    {
        final BuildingExtensionEntry entry = buf.readById(BuildingExtensionRegistries.getBuildingExtensionRegistry()::byIdOrThrow);
        final BlockPos position = buf.readBlockPos();
        final IBuildingExtension extension = entry.produceExtension(position);
        extension.deserialize(buf);
        return extension;
    }

    /**
     * Creates a network buffer from a building extension instance.
     *
     * @param extension the building extension instance.
     * @return the network buffer.
     */
    public static RegistryFriendlyByteBuf extensionToBuffer(final @NotNull IBuildingExtension extension, @NotNull final RegistryAccess provider)
    {
        final RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), provider);
        buf.writeById(BuildingExtensionRegistries.getBuildingExtensionRegistry()::getIdOrThrow, extension.getBuildingExtensionType());
        buf.writeBlockPos(extension.getPosition());
        extension.serialize(buf);
        return buf;
    }

    /**
     * Creates NBT compound data from a building extension instance.
     *
     * @param extension the building extension instance.
     * @return the NBT compound.
     */
    public static CompoundTag extensionToCompound(@NotNull final HolderLookup.Provider provider, final @NotNull IBuildingExtension extension)
    {
        final CompoundTag compound = new CompoundTag();
        compound.putString(TAG_EXTENSION_NAME, extension.getBuildingExtensionType().getRegistryName().toString());
        BlockPosUtil.write(compound, TAG_EXTENSION_POSITION, extension.getPosition());
        compound.put(TAG_EXTENSION_DATA, extension.serializeNBT(provider));
        return compound;
    }
}
