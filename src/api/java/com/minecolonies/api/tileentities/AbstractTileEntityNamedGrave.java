package com.minecolonies.api.tileentities;

import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.state.DirectionProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;

public class AbstractTileEntityNamedGrave extends TileEntity
{
    /**
     * The position it faces.
     */
    public static final DirectionProperty FACING       = HorizontalBlock.HORIZONTAL_FACING;

    private List<String> textLines = new ArrayList<>();

    public AbstractTileEntityNamedGrave(TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

    public List<String> getTextLines()
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
}
