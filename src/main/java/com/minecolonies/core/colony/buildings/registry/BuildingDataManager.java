package com.minecolonies.core.colony.buildings.registry;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.registry.IBuildingDataManager;
import com.minecolonies.api.colony.buildings.registry.IBuildingRegistry;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.client.gui.WindowBuildingBrowser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_BUILDING_TYPE;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_LOCATION;

public class BuildingDataManager implements IBuildingDataManager
{
    @Override
    public IBuilding createFrom(final IColony colony, final CompoundTag compound, @NotNull final HolderLookup.Provider provider)
    {
        final ResourceLocation type = ResourceLocation.parse(compound.getString(TAG_BUILDING_TYPE));
        final BlockPos pos = BlockPosUtil.read(compound, TAG_LOCATION);

        IBuilding building = this.createFrom(colony, pos, type);

        try
        {
            building.deserializeNBT(provider, compound);
        }
        catch (final Exception ex)
        {
            Log.getLogger().error(String.format("A Building %s(%s) has thrown an exception during loading, its state cannot be restored. Report this to the mod author",
              type, building.getClass().getName()), ex);
            building = null;
        }

        return building;
    }

    @Override
    public IBuilding createFrom(final IColony colony, final AbstractTileEntityColonyBuilding tileEntityColonyBuilding)
    {
        return this.createFrom(colony, tileEntityColonyBuilding.getPosition(), tileEntityColonyBuilding.getBuildingName());
    }

    @Override
    public IBuilding createFrom(final IColony colony, final BlockPos position, final ResourceLocation buildingName)
    {
        final BuildingEntry entry = IBuildingRegistry.getInstance().get(buildingName);
        if (entry == null)
        {
            if (buildingName.getPath().equals("home"))
            {
                return ModBuildings.home.get().produceBuilding(position, colony);
            }
            Log.getLogger().error(String.format("Unknown building type '%s'.", buildingName), new Exception());
            return null;
        }
        return entry.produceBuilding(position, colony);
    }

    @Override
    public IBuildingView createViewFrom(final IColonyView colony, final BlockPos position, final RegistryFriendlyByteBuf networkBuffer)
    {
        final ResourceLocation buildingName = ResourceLocation.parse(networkBuffer.readUtf(32767));
        final BuildingEntry entry = IBuildingRegistry.getInstance().get(buildingName);

        if (entry == null)
        {
            Log.getLogger().error(String.format("Unknown building type '%s'.", buildingName), new Exception());
            return null;
        }

        final IBuildingView view = entry.produceBuildingView(position, colony);
        if (view != null)
        {
            view.deserialize(networkBuffer);
        }

        return view;
    }

    @Override
    public void openBuildingBrowser(@NotNull final Block block)
    {
        new WindowBuildingBrowser(block).open();
    }
}
