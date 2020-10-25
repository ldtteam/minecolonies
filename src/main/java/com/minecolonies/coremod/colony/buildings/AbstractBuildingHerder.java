package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.server.colony.building.herder.HerderSetBreedingMessage;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_BREEDING;

/**
 * The abstract class for each herder building.
 */
public abstract class AbstractBuildingHerder extends AbstractBuildingWorker
{
    /**
     * Breed animals or not.
     */
    private boolean breeding = true;

    /**
     * The abstract constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public AbstractBuildingHerder(IColony c, BlockPos l)
    {
        super(c, l);
    }

    @Override
    public void serializeToView(PacketBuffer buf)
    {
        super.serializeToView(buf);
        buf.writeBoolean(breeding);
    }

    @Override
    public void deserializeNBT(CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        if (compound.contains(TAG_BREEDING))
        {
            breeding = compound.getBoolean(TAG_BREEDING);
        }
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();
        compound.putBoolean(TAG_BREEDING, breeding);
        return compound;
    }

    /**
     * Check if the herder should be breeding.
     * @return true if so.
     */
    public boolean isBreeding()
    {
        return breeding;
    }

    /**
     * Set the herder to breed or not.
     * @param breeding true if shall breed.
     */
    public void setBreeding(final boolean breeding)
    {
        this.breeding = breeding;
        markDirty();
    }

    /**
     * ClientSide representation of the building.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        /**
         * Breed animals or not.
         */
        private boolean breeding = true;

        /**
         * Instantiates the view of the building.
         *
         * @param c the colonyView.
         * @param l the location of the block.
         */
        public View(IColonyView c, BlockPos l)
        {
            super(c, l);
        }

        public void setBreeding(final boolean breeding)
        {
            Network.getNetwork().sendToServer(new HerderSetBreedingMessage(this, breeding));
            this.breeding = breeding;
        }

        public boolean isBreeding()
        {
            return breeding;
        }

        @Override
        public void deserialize(PacketBuffer buf)
        {
            super.deserialize(buf);
            breeding = buf.readBoolean();
        }
    }

}
