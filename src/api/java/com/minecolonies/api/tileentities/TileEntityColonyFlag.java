package com.minecolonies.api.tileentities;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.mojang.datafixers.util.Pair;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import java.util.List;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static net.minecraft.tileentity.BannerTileEntity.func_230138_a_;

public class TileEntityColonyFlag extends TileEntity
{
    /** The last known flag. Required for when the colony is not available. */
    private ListNBT flag = new ListNBT();

    /** A list of the default banner patterns, for colonies that have not chosen a flag */
    private ListNBT patterns = new ListNBT();

    /** The colony of the player that placed this banner */
    public int colonyId = -1;

    public TileEntityColonyFlag () { super(MinecoloniesTileEntities.COLONY_FLAG); }

    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
        super.write(compound);

        compound.put(TAG_FLAG_PATTERNS, this.flag);
        compound.put(TAG_BANNER_PATTERNS, this.patterns);

        compound.putInt(TAG_COLONY_ID, colonyId);

        return compound;
    }

    @Override
    public void read(CompoundNBT compound)
    {
        super.read(compound);

        this.flag = compound.getList(TAG_FLAG_PATTERNS, 10);
        this.patterns = compound.getList(TAG_BANNER_PATTERNS, 10);
        this.colonyId = compound.getInt(TAG_COLONY_ID);
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        return new SUpdateTileEntityPacket(this.pos, 6, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() { return this.write(new CompoundNBT()); }

    @Override
    public void onDataPacket(final NetworkManager net, final SUpdateTileEntityPacket packet)
    {
        final CompoundNBT compound = packet.getNbtCompound();
        this.read(compound);
    }

    /**
     * Retrieves the patterns for the renderer
     * @return the list of pattern-color pairs
     */
    @OnlyIn(Dist.CLIENT)
    public List<Pair<BannerPattern, DyeColor>> getPatternList()
    {
        IColonyView colony = IColonyManager.getInstance().getColonyView(this.colonyId, world.dimension.getType().getId());
        if (colony != null && this.flag != colony.getColonyFlag())
        {
            this.flag = colony.getColonyFlag();
            markDirty();
        }

        return func_230138_a_(
                DyeColor.WHITE,
                this.flag.size() > 1 ? this.flag : this.patterns
        );
    }

    /**
     * Builds a mutable ItemStack from the information within the tile entity
     * @return the ItemStack representing this banner
     */
    @OnlyIn(Dist.CLIENT)
    public ItemStack getItem()
    {
        ItemStack itemstack = new ItemStack(ModBlocks.blockColonyBanner);
        List<Pair<BannerPattern, DyeColor>> list = getPatternList();
        ListNBT nbt = new ListNBT();

        for (Pair<BannerPattern, DyeColor> pair : list)
        {
            CompoundNBT pairNBT = new CompoundNBT();
            pairNBT.putString(TAG_SINGLE_PATTERN, pair.getFirst().getHashname());
            pairNBT.putInt(TAG_PATTERN_COLOR, pair.getSecond().getId());
            nbt.add(pairNBT);
        }

        if (!nbt.isEmpty())
            itemstack.getOrCreateChildTag("BlockEntityTag").put(TAG_BANNER_PATTERNS, nbt);

        return itemstack;
    }
}
