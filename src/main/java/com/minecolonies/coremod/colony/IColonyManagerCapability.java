package com.minecolonies.coremod.colony;

import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.util.NBTUtils;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import java.util.List;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

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

    void addColony(Colony colony);

    void setMissingChunksToLoad(int integer);

    int getMissingChunksToLoad();

    int getTopID();

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

        @Override
        public void addColony(final Colony colony)
        {
            colonies.add(colony);
        }

        @Override
        public void setMissingChunksToLoad(final int chunksToLoad)
        {
            missingChunksToLoad = chunksToLoad;
        }

        @Override
        public int getMissingChunksToLoad()
        {
            return missingChunksToLoad;
        }

        @Override
        public int getTopID()
        {
            return colonies.getTopID();
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
            compound.setTag(TAG_COLONIES, instance.getColonies().stream().map(colony -> colony.writeToNBT(new NBTTagCompound())).collect(NBTUtils.toNBTTagList()));
            compound.setInteger(TAG_MISSING_CHUNKS, instance.getMissingChunksToLoad());
            return compound;
        }

        @Override
        public void readNBT(@NotNull final Capability<IColonyManagerCapability> capability, @NotNull final IColonyManagerCapability instance,
                @Nullable final EnumFacing side, @NotNull final NBTBase nbt)
        {
            if(nbt instanceof NBTTagCompound)
            {
                final NBTTagCompound compound = (NBTTagCompound) nbt;
                NBTUtils.streamCompound(((NBTTagCompound) nbt).getTagList(TAG_COLONIES, Constants.NBT.TAG_COMPOUND))
                  .map(colonyCompound -> Colony.loadColony(colonyCompound, FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(compound.getInteger(TAG_DIMENSION)))).forEach(instance::addColony);
                instance.setMissingChunksToLoad(compound.getInteger(TAG_MISSING_CHUNKS));
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
