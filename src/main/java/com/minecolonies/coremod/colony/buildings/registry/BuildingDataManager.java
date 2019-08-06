package com.minecolonies.coremod.colony.buildings.registry;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.registry.IBuildingDataManager;
import com.minecolonies.api.colony.buildings.registry.IBuildingRegistry;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import net.minecraft.network.PacketBuffer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_BUILDING_TYPE;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_LOCATION;

public class BuildingDataManager implements IBuildingDataManager
{
    @Override
    public IBuilding createFrom(final IColony colony, final CompoundNBT compound)
    {
        final ResourceLocation type = new ResourceLocation(compound.getString(TAG_BUILDING_TYPE));
        final BlockPos pos = BlockPosUtil.read(compound, TAG_LOCATION);

        IBuilding building = this.createFrom(colony, pos, type);

        try
        {
            building.deserializeNBT(compound);
        }
        catch (final RuntimeException ex)
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
        final BuildingEntry entry = IBuildingRegistry.getInstance().getValue(buildingName);

        if (entry == null)
        {
            Log.getLogger().error(String.format("Unknown building type '%s'.", buildingName));
            return null;
        }

        return entry.getBuildingProducer().apply(colony, position);
    }

    @Override
    public IBuildingView createViewFrom(
      final IColonyView colony, final BlockPos position, final PacketBuffer networkBuffer)
    {
        final ResourceLocation buildingName = new ResourceLocation(networkBuffer.readString());
        final BuildingEntry entry = IBuildingRegistry.getInstance().getValue(buildingName);

        if (entry == null)
        {
            Log.getLogger().error(String.format("Unknown building type '%s'.", buildingName));
            return null;
        }

        final IBuildingView view = entry.getBuildingViewProducer().apply(colony, position);

        if (view != null)
        {
            view.deserialize(networkBuffer);
        }

        return view;
    }
}
