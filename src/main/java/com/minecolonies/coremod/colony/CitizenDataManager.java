package com.minecolonies.coremod.colony;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataManager;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.Log;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundNBT;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;

public class CitizenDataManager implements ICitizenDataManager
{
    @Override
    public ICitizenData createFromNBT(@NotNull final CompoundNBT compound, final IColony colony)
    {
        final int id = compound.getInt(TAG_ID);
        final @NotNull CitizenData citizen = new CitizenData(id, colony);
        citizen.read(compound);
        return citizen;
    }

    @Override
    public ICitizenDataView createFromNetworkData(@NotNull final int id, @NotNull final ByteBuf networkBuffer)
    {
        CitizenDataView citizenDataView = new CitizenDataView(id);

        try
        {
            citizenDataView.deserialize(networkBuffer);
        }
        catch (final RuntimeException ex)
        {
            Log.getLogger().error(String.format("A CitizenData.View for #%d has thrown an exception during loading, its state cannot be restored. Report this to the mod author",
              citizenDataView.getId()), ex);
            citizenDataView = null;
        }

        return citizenDataView;
    }
}
