package com.minecolonies.api.tileentities;

import com.minecolonies.api.util.WorldUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.state.DirectionProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;

public class AbstractTileEntityNamedGrave extends TileEntity
{
    /**
     * The position it faces.
     */
    public static final DirectionProperty FACING       = HorizontalBlock.HORIZONTAL_FACING;

    /**
     * The text displayed on the name plate
     */
    private ArrayList<String> textLines = new ArrayList<>();

    public AbstractTileEntityNamedGrave(TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
        textLines.add("Unknown Citizen");
    }

    public ArrayList<String> getTextLines()
    {
        return textLines;
    }

    public void setTextLines(final ArrayList<String> content)
    {
        this.textLines = content;
        markDirty();
    }

    @Override
    public void read(final BlockState state, final CompoundNBT compound)
    {
        super.read(state, compound);

        textLines.clear();
        if (compound.keySet().contains(TAG_CONTENT))
        {
            final ListNBT lines = compound.getList(TAG_CONTENT, TAG_STRING);
            for (int i = 0; i < lines.size(); i++)
            {
                final String line = lines.getString(i);
                textLines.add(line);
            }
        }
    }

    @NotNull
    @Override
    public CompoundNBT write(final CompoundNBT compound)
    {
        super.write(compound);

        @NotNull final ListNBT lines = new ListNBT();
        for (@NotNull final String line : textLines)
        {
            lines.add(StringNBT.valueOf(line));
        }
        compound.put(TAG_CONTENT, lines);

        return compound;
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        final CompoundNBT compound = new CompoundNBT();
        return new SUpdateTileEntityPacket(this.pos, 0, this.write(compound));
    }

    @NotNull
    @Override
    public CompoundNBT getUpdateTag()
    {
        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SUpdateTileEntityPacket packet)
    {
        this.read(getBlockState(), packet.getNbtCompound());
    }

    @Override
    public void handleUpdateTag(final BlockState state, final CompoundNBT tag)
    {
        this.read(state, tag);
    }

    @Override
    public void markDirty()
    {
        WorldUtil.markChunkDirty(world, pos);
    }
}
