package com.minecolonies.api.tileentities;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.INameable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

import static net.minecraft.tileentity.BannerTileEntity.func_230138_a_;

public class TileEntityColonyFlag extends TileEntity
{
    /** A list of all the banner patterns. */
    @Nullable
    private ListNBT patterns;

    private int colonyId;

    public TileEntityColonyFlag () { super(MinecoloniesTileEntities.COLONY_FLAG); }

    public void setColonyId(int colonyId)
    {
        this.colonyId = colonyId;
        markDirty();
    }

    public CompoundNBT write(CompoundNBT compound)
    {
        super.write(compound);

        if (this.patterns != null)
            compound.put("Patterns", this.patterns);

        compound.putInt("Colony", this.colonyId);

        return compound;
    }

    public void read(CompoundNBT compound)
    {
        super.read(compound);

        this.patterns = compound.getList("Patterns", 10);
        this.colonyId = compound.getInt("Colony");
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 6, this.getUpdateTag());
    }

    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @OnlyIn(Dist.CLIENT)
    public List<Pair<BannerPattern, DyeColor>> getPatternList()
    {
        int dim = getWorld().dimension.getType().getId();
        IColonyView colony = IColonyManager.getInstance().getColonyView(colonyId, dim);

        boolean hasFlag = colony != null && !colony.getColonyFlag().isEmpty();

        return func_230138_a_(
                DyeColor.WHITE,
                hasFlag ? colony.getColonyFlag() : this.patterns
        );
    }

    @OnlyIn(Dist.CLIENT)
    public ItemStack getItem()
    {
        ItemStack itemstack = new ItemStack(ModBlocks.blockColonyBanner);
        List<Pair<BannerPattern, DyeColor>> list = getPatternList();
        ListNBT nbt = new ListNBT();

        for (Pair<BannerPattern, DyeColor> pair : list)
        {
            CompoundNBT pairNBT = new CompoundNBT();
            pairNBT.putString("Pattern", pair.getFirst().getHashname());
            pairNBT.putInt("Color", pair.getSecond().getId());
            nbt.add(pairNBT);
        }

        if (!nbt.isEmpty())
            itemstack.getOrCreateChildTag("BlockEntityTag").put("Patterns", nbt);

        return itemstack;
    }
}
