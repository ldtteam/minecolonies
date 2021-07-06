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
    public static final DirectionProperty FACING       = HorizontalBlock.FACING;

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
        setChanged();
    }

    @Override
    public void load(final BlockState state, final CompoundNBT compound)
    {
        super.load(state, compound);

        textLines.clear();
        if (compound.getAllKeys().contains(TAG_CONTENT))
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
    public CompoundNBT save(final CompoundNBT compound)
    {
        super.save(compound);

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
        return new SUpdateTileEntityPacket(this.worldPosition, 0, this.save(compound));
    }

    @NotNull
    @Override
    public CompoundNBT getUpdateTag()
    {
        return this.save(new CompoundNBT());
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SUpdateTileEntityPacket packet)
    {
        this.load(getBlockState(), packet.getTag());
    }

    @Override
    public void handleUpdateTag(final BlockState state, final CompoundNBT tag)
    {
        this.load(state, tag);
    }

    @Override
    public void setChanged()
    {
        WorldUtil.markChunkDirty(level, worldPosition);
    }
}
