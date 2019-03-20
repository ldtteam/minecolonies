package com.minecolonies.coremod.tileentities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

public class TileEntityDecorationController extends TileEntity
{
    /**
     * The schematic name of the placerholder block.
     */
    private String schematicName = "";

    /**
     * The current level.
     */
    private int level = 0;

    /**
     * Geter for the name stored in this.
     * @return String name.
     */
    public String getSchematicName()
    {
        return schematicName;
    }

    /**
     * Setter for the schematic name connected to this.
     * @param schematicName the name to set.
     */
    public void setSchematicName(final String schematicName)
    {
        this.schematicName = schematicName;
        this.markDirty();
    }

    /**
     * Getter for the deco level associated.
     * @return the level.
     */
    public int getLevel()
    {
        return level;
    }

    /**
     * Set the deco level.
     * @param level the max.
     */
    public void setLevel(final int level)
    {
        this.level = level;
        this.markDirty();
    }

    @Override
    public void readFromNBT(final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.schematicName = compound.getString(TAG_NAME);
        this.level = compound.getInteger(TAG_LEVEL);
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setString(TAG_NAME, schematicName);
        compound.setInteger(TAG_LEVEL, level);
        return compound;
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(this.pos, 0x9, this.getUpdateTag());
    }

    @NotNull
    @Override
    public NBTTagCompound getUpdateTag()
    {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SPacketUpdateTileEntity packet)
    {
        final NBTTagCompound compound = packet.getNbtCompound();
        this.readFromNBT(compound);
    }
}
