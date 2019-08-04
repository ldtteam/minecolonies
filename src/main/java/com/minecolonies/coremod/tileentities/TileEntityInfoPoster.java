package com.minecolonies.coremod.tileentities;

import net.minecraft.command.CommandResultStats;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class TileEntityInfoPoster extends TileEntity
{
    public final ITextComponent[] signText =
      new ITextComponent[] {new StringTextComponent(""), new StringTextComponent(""), new StringTextComponent(""), new StringTextComponent("")};

    private final CommandResultStats stats = new CommandResultStats();

    @Override
    public void read(final CompoundNBT compound)
    {
        super.read(compound);

        for (int i = 0; i < signText.length; ++i)
        {
            final String s = compound.getString("Text" + (i + 1));
            final ITextComponent itextcomponent = ITextComponent.Serializer.jsonToComponent(s);
            this.signText[i] = itextcomponent;
        }

        this.stats.readStatsFromNBT(compound);
    }

    @Override
    public CompoundNBT write(final CompoundNBT compound)
    {
        super.write(compound);

        for (int i = 0; i < signText.length; ++i)
        {
            final String s = ITextComponent.Serializer.componentToJson(this.signText[i]);
            compound.putString("Text" + (i + 1), s);
        }

        this.stats.writeStatsToNBT(compound);
        return compound;
    }

    @Override
    protected void setWorldCreate(final World worldIn)
    {
        this.setWorld(worldIn);
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        return new SUpdateTileEntityPacket(this.pos, 0x9, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        return this.write(new CompoundNBT());
    }

    /**
     * Return the stats of the poster.
     *
     * @return the stats.
     */
    public CommandResultStats getStats()
    {
        return this.stats;
    }
}
