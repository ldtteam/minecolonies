package com.minecolonies.entity;

import com.minecolonies.lib.Constants;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

/**
 * NOTE
 * THIS CLASS NEEDS TWEAKING
 * I DO NOT KNOW ALL ABOUT PLAYER PROPERTIES, BUT THIS WAY, IT WORKED FOR ME LAST TIME!
 */
public class PlayerProperties implements IExtendedEntityProperties
{
    private EntityPlayer player;
    boolean hasPlacedTownHall = false;

    public PlayerProperties(EntityPlayer player)
    {
        this.player = player;
    }

    public void register(EntityPlayer player)
    {
        player.registerExtendedProperties(Constants.PlayerPropertyName, new PlayerProperties(player));
    }

    public PlayerProperties getPlayerProperties(EntityPlayer player)
    {
        return (PlayerProperties) player.getExtendedProperties(Constants.PlayerPropertyName);
    }

    @Override
    public void saveNBTData(NBTTagCompound compound)
    {
        compound.setBoolean("hasPlacedTownHall", hasPlacedTownHall);
    }

    @Override
    public void loadNBTData(NBTTagCompound compound)
    {
        this.hasPlacedTownHall = compound.getBoolean("hasPlacedTownHall");
    }

    @Override
    public void init(Entity entity, World world)
    {

    }

    public boolean HasPlacedTownHall()
    {
        return hasPlacedTownHall;
    }

    public void setHasPlacedTownHall(boolean hasPlacedTownHall)
    {
        this.hasPlacedTownHall = hasPlacedTownHall;
    }
}