package com.minecolonies.coremod.colony;

import com.minecolonies.coremod.util.ColonyManagerHelper;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import java.util.List;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_MISSING_CHUNKS;

/**
 *
 * Capability for the colony tag for chunks
 */
public interface IColonyManagerCapability
{
    Colony createColony(@NotNull final World w, @NotNull final BlockPos pos);

    void deleteColony(final int id);

    Colony getColony(final int id);

    List<Colony> getColonies();

    /**
     * The implementation of the colonyTagCapability.
     */
    public class Impl implements IColonyManagerCapability
    {
        /**
         * The list of all colonies.
         */
        @NotNull
        private static final ColonyList<Colony> colonies = new ColonyList<>();

        /**
         * Removed elements of the list of chunks to load.
         */
        private static int missingChunksToLoad = 0;

        @Override
        public Colony createColony(@NotNull final World w, @NotNull final BlockPos pos)
        {
            return colonies.create(w, pos);
        }

        @Override
        public void deleteColony(final int id)
        {
            colonies.remove(id);
        }

        @Override
        public Colony getColony(final int id)
        {
            return colonies.get(id);
        }

        @Override
        public List<Colony> getColonies()
        {
            return colonies.getCopyAsList();
        }
    }

    /**
     * The storage class of the capability.
     */
    public class Storage implements Capability.IStorage<IColonyManagerCapability>
    {
        @Override
        public NBTBase writeNBT(@NotNull final Capability<IColonyManagerCapability> capability, @NotNull final IColonyManagerCapability instance, @Nullable final EnumFacing side)
        {
            final NBTTagCompound compound = new NBTTagCompound();
            compound.setInteger(TAG_MISSING_CHUNKS, missingChunksToLoad);

            return compound;
        }

        @Override
        public void readNBT(@NotNull final Capability<IColonyManagerCapability> capability, @NotNull final IColonyManagerCapability instance,
                @Nullable final EnumFacing side, @NotNull final NBTBase nbt)
        {
            if(nbt instanceof NBTTagCompound)
            {
                missingChunksToLoad = compound.getInteger(TAG_MISSING_CHUNKS);
            }
        }

        /**
         * Write one colony id to nbt.
         * @param id the id.
         * @return the compound of it.
         */
        private static NBTTagCompound write(final int id)
        {
            final NBTTagCompound compound = new NBTTagCompound();
            compound.setInteger(TAG_ID, id);
            return compound;
        }
    }
}
