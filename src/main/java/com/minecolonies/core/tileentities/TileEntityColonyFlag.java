package com.minecolonies.core.tileentities;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;


import static com.minecolonies.api.util.constant.NbtTagConstants.*;

public class TileEntityColonyFlag extends BlockEntity
{
    /** A list of the default banner patterns, for colonies that have not chosen a flag */
    private BannerPatternLayers patterns = BannerPatternLayers.EMPTY;

    /** The colony of the player that placed this banner */
    public int colonyId = -1;

    public TileEntityColonyFlag(final BlockPos pos, final BlockState state) { super(MinecoloniesTileEntities.COLONY_FLAG.get(), pos, state); }

    public BannerPatternLayers getPatterns() {
        return this.patterns;
    }

    @Override
    public void saveAdditional(CompoundTag compound, @NotNull final HolderLookup.Provider provider)
    {
        super.saveAdditional(compound, provider);

        compound.put(TAG_BANNER_PATTERNS, Utils.serializeCodecMess(BannerPatternLayers.CODEC, provider, this.patterns));

        compound.putInt(TAG_COLONY_ID, colonyId);
    }

    @Override
    public void loadAdditional(CompoundTag compound, @NotNull final HolderLookup.Provider provider)
    {
        super.loadAdditional(compound, provider);

        this.patterns = Utils.deserializeCodecMess(BannerPatternLayers.CODEC, provider, compound.get(TAG_BANNER_PATTERNS));
        this.colonyId = compound.getInt(TAG_COLONY_ID);

        if(this.colonyId == -1 && this.hasLevel())
        {
            IColony colony = IColonyManager.getInstance().getIColony(this.getLevel(), worldPosition);
            if (colony != null)
            {
                this.colonyId = colony.getID();
                this.setChanged();
            }
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(@NotNull final HolderLookup.Provider provider) { return this.saveWithId(provider); }

    @Override
    public void onDataPacket(final Connection net, final ClientboundBlockEntityDataPacket packet, @NotNull final HolderLookup.Provider provider)
    {
        final CompoundTag compound = packet.getTag();
        this.loadAdditional(compound, provider);
    }
}
