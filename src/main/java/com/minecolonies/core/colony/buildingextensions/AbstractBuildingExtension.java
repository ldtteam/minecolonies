package com.minecolonies.core.colony.buildingextensions;

import com.minecolonies.api.colony.buildingextensions.registry.BuildingExtensionRegistries.BuildingExtensionEntry;
import com.minecolonies.api.colony.buildings.modules.IBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IBuildingModuleView;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.buildingextensions.IBuildingExtension;
import com.minecolonies.api.colony.buildingextensions.modules.IBuildingExtensionModule;
import com.minecolonies.api.colony.buildingextensions.registry.BuildingExtensionRegistries;
import com.minecolonies.api.colony.modules.ModuleContainerUtils;
import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_OWNER;

/**
 * Abstract implementation for building extension instances.
 * Contains some basic mandatory logic for building extensions.
 */
public abstract class AbstractBuildingExtension implements IBuildingExtension
{
    /**
     * Set of building extension modules this building extension has.
     */
    private final List<IBuildingExtensionModule> modules = new ArrayList<>();

    /**
     * The type of the building extension.
     */
    private final BuildingExtensionEntry buildingExtensionEntry;

    /**
     * The position of the building extension.
     */
    private final BlockPos position;

    /**
     * Building id of the building owning the building extension.
     */
    @Nullable
    private BlockPos buildingId = null;

    /**
     * Constructor used in NBT deserialization.
     *
     * @param buildingExtensionEntry the type of building extension.
     * @param position  the position of the building extension.
     */
    protected AbstractBuildingExtension(final @NotNull BuildingExtensionRegistries.BuildingExtensionEntry buildingExtensionEntry, final @NotNull BlockPos position)
    {
        this.buildingExtensionEntry = buildingExtensionEntry;
        this.position = position;
    }

    @Override
    public boolean hasModule(final Class<? extends IBuildingExtensionModule> clazz)
    {
        return ModuleContainerUtils.hasModule(modules, clazz);
    }

    @Override
    public boolean hasModule(final BuildingEntry.ModuleProducer<?, ?> producer)
    {
        throw new RuntimeException("Not implemented");
    }

    @NotNull
    @Override
    public <T extends IBuildingExtensionModule> T getFirstModuleOccurance(final Class<T> clazz)
    {
        return ModuleContainerUtils.getFirstModuleOccurance(modules,
          clazz,
          "The module of class: " + clazz.toString() + "should never be null! Building extension:" + getBuildingExtensionType().getRegistryName() + " pos:" + getPosition());
    }

    @Override
    public <M extends IBuildingModule, V extends IBuildingModuleView> @NotNull M getModule(
      final BuildingEntry.ModuleProducer<M, V> producer)
    {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public IBuildingModule getModule(final int id)
    {
        throw new RuntimeException("Not implemented");
    }

    @NotNull
    @Override
    public <T extends IBuildingExtensionModule> List<T> getModulesByType(final Class<T> clazz)
    {
        return ModuleContainerUtils.getModules(modules, clazz);
    }

    @NotNull
    @Override
    public <T extends IBuildingExtensionModule> T getModuleMatching(final Class<T> clazz, final Predicate<? super T> modulePredicate)
    {
        return ModuleContainerUtils.getModuleMatching(modules,
          clazz,
          modulePredicate,
          "no matching module for building extension:" + getBuildingExtensionType().getRegistryName() + " pos:" + getPosition().toShortString());
    }

    @Override
    public void registerModule(@NotNull final IBuildingExtensionModule module)
    {
        this.modules.add(module);
    }

    @Override
    public final @NotNull BuildingExtensionRegistries.BuildingExtensionEntry getBuildingExtensionType()
    {
        return buildingExtensionEntry;
    }

    @Override
    @NotNull
    public final BlockPos getPosition()
    {
        return position;
    }

    @Override
    @Nullable
    public final BlockPos getBuildingId()
    {
        return buildingId;
    }

    @Override
    public final void setBuilding(final BlockPos buildingId)
    {
        this.buildingId = buildingId;
    }

    @Override
    public final void resetOwningBuilding()
    {
        buildingId = null;
    }

    @Override
    public final boolean isTaken()
    {
        return buildingId != null;
    }

    @Override
    public final int getSqDistance(final IBuildingView building)
    {
        return (int) Math.sqrt(BlockPosUtil.getDistanceSquared(position, building.getPosition()));
    }

    @Override
    public @NotNull CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider)
    {
        CompoundTag compound = new CompoundTag();
        if (buildingId != null)
        {
            BlockPosUtil.write(compound, TAG_OWNER, buildingId);
        }
        return compound;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, final @NotNull CompoundTag compound)
    {
        if (compound.contains(TAG_OWNER))
        {
            buildingId = BlockPosUtil.read(compound, TAG_OWNER);
        }
    }

    @Override
    public void serialize(final @NotNull RegistryFriendlyByteBuf buf)
    {
        buf.writeBoolean(buildingId != null);
        if (buildingId != null)
        {
            buf.writeBlockPos(buildingId);
        }
    }

    @Override
    public void deserialize(final @NotNull RegistryFriendlyByteBuf buf)
    {
        if (buf.readBoolean())
        {
            buildingId = buf.readBlockPos();
        }
    }

    @Override
    public int hashCode()
    {
        int result = position.hashCode();
        result = 31 * result + buildingExtensionEntry.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final AbstractBuildingExtension that = (AbstractBuildingExtension) o;

        if (!position.equals(that.position))
        {
            return false;
        }
        return buildingExtensionEntry.equals(that.buildingExtensionEntry);
    }
}
