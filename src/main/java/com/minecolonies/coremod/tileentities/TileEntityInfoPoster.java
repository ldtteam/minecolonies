package com.minecolonies.coremod.tileentities;

import net.minecraft.command.CommandResultStats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class TileEntityInfoPoster extends TileEntity
{
    public final ITextComponent[] signText =
            new ITextComponent[] {new TextComponentString(""), new TextComponentString(""), new TextComponentString(""), new TextComponentString("")};

    private EntityPlayer player;
    private final CommandResultStats stats = new CommandResultStats();

    /**
     * Creates an instance of the TileEntityInfoPoster.
     */
    public TileEntityInfoPoster()
    {
        super();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        for (int i = 0; i < signText.length; ++i)
        {
            final String s = ITextComponent.Serializer.componentToJson(this.signText[i]);
            compound.setString("Text" + (i + 1), s);
        }

        this.stats.writeStatsToNBT(compound);
        return compound;
    }

    @Override
    protected void setWorldCreate(World worldIn)
    {
        this.setWorldObj(worldIn);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        for (int i = 0; i < signText.length; ++i)
        {
            final String s = compound.getString("Text" + (i + 1));
            final ITextComponent itextcomponent = ITextComponent.Serializer.jsonToComponent(s);
            this.signText[i] = itextcomponent;
        }

        this.stats.readStatsFromNBT(compound);
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(this.pos, 0x9, this.getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        return this.writeToNBT(new NBTTagCompound());
    }

    /**
     * Return the stats of the poster.
     * @return the stats.
     */
    public CommandResultStats getStats()
    {
        return this.stats;
    }
}
