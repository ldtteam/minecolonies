package com.minecolonies.core.tileentities;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import java.util.List;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

public class TileEntityColonyFlag extends BlockEntity
{
    /** The last known flag. Required for when the colony is not available. */
    private ListTag flag = new ListTag();

    /** A list of the default banner patterns, for colonies that have not chosen a flag */
    private ListTag patterns = new ListTag();

    /** The colony of the player that placed this banner */
    public int colonyId = -1;

    public TileEntityColonyFlag(final BlockPos pos, final BlockState state) { super(MinecoloniesTileEntities.COLONY_FLAG.get(), pos, state); }

    @Override
    public void saveAdditional(CompoundTag compound)
    {
        super.saveAdditional(compound);

        compound.put(TAG_FLAG_PATTERNS, this.flag);
        compound.put(TAG_BANNER_PATTERNS, this.patterns);

        compound.putInt(TAG_COLONY_ID, colonyId);
    }

    @Override
    public void load(CompoundTag compound)
    {
        super.load(compound);

        this.flag = compound.getList(TAG_FLAG_PATTERNS, 10);
        this.patterns = compound.getList(TAG_BANNER_PATTERNS, 10);
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
    public CompoundTag getUpdateTag() { return this.saveWithId(); }

    @Override
    public void onDataPacket(final Connection net, final ClientboundBlockEntityDataPacket packet)
    {
        final CompoundTag compound = packet.getTag();
        this.load(compound);
    }

    /**
     * Retrieves the patterns for the renderer
     * @return the list of pattern-color pairs
     */
    @OnlyIn(Dist.CLIENT)
    public List<Pair<Holder<BannerPattern>, DyeColor>> getPatternList()
    {
        // Structurize will cause the second condition to be false
        if (level != null && level.dimension() != null)
        {
            IColonyView colony = IColonyManager.getInstance().getColonyView(this.colonyId, level.dimension());
            if (colony != null && this.flag != colony.getColonyFlag())
            {
                this.flag = colony.getColonyFlag();
                setChanged();
            }
        }

        return BannerBlockEntity.createPatterns(
                DyeColor.WHITE,
                this.flag.size() > 1 ? this.flag : this.patterns
        );
    }

    /**
     * Builds a mutable ItemStack from the information within the tile entity
     * @return the ItemStack representing this banner
     */
    @OnlyIn(Dist.CLIENT)
    public ItemStack getItemClient()
    {
        ItemStack itemstack = new ItemStack(ModBlocks.blockColonyBanner);
        List<Pair<Holder<BannerPattern>, DyeColor>> list = getPatternList();
        ListTag nbt = new ListTag();

        for (Pair<Holder<BannerPattern>, DyeColor> pair : list)
        {
            CompoundTag pairNBT = new CompoundTag();
            pairNBT.putString(TAG_SINGLE_PATTERN, pair.getFirst().value().getHashname());
            pairNBT.putInt(TAG_PATTERN_COLOR, pair.getSecond().getId());
            nbt.add(pairNBT);
        }

        if (!nbt.isEmpty())
            itemstack.getOrCreateTagElement("BlockEntityTag").put(TAG_BANNER_PATTERNS, nbt);

        return itemstack;
    }

    /**
     * Serverside version of the getItem method.
     * @return the classic stack.
     */
    public ItemStack getItemServer()
    {
        return new ItemStack(ModItems.flagBanner);
    }
}
