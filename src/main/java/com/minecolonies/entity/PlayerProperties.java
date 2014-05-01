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
    private final EntityPlayer player;
    boolean hasPlacedTownHall    = false;
    boolean hasPlacedSupplyChest = false;

    public PlayerProperties(EntityPlayer player)
    {
        this.player = player;
    }

    /**
     * Registers player property. Should be checked if already exists, and called in onEntityConstruct event
     */
    public void register()
    {
        player.registerExtendedProperties(Constants.PlayerPropertyName, this);
    }

    /**
     * Gets the player properties for a player
     *
     * @return PlayerProperties for the player.
     */
    public PlayerProperties getPlayerProperties()
    {
        return (PlayerProperties) player.getExtendedProperties(Constants.PlayerPropertyName);
    }

    @Override
    public void saveNBTData(NBTTagCompound compound)
    {
        NBTTagCompound properties = new NBTTagCompound();

        properties.setBoolean("hasPlacedTownHall", hasPlacedTownHall);
        properties.setBoolean("hasPlacedSupplyChest", hasPlacedSupplyChest);

        compound.setTag(Constants.PlayerPropertyName, properties);
    }

    @Override
    public void loadNBTData(NBTTagCompound compound)
    {
        NBTTagCompound properties = (NBTTagCompound) compound.getTag(Constants.PlayerPropertyName);

        this.hasPlacedTownHall = properties.getBoolean("hasPlacedTownHall");
        this.hasPlacedSupplyChest = properties.getBoolean("hasPlacedSupplyChest");
    }

    @Override
    public void init(Entity entity, World world)
    {

    }

    /**
     * Gets the property whether the player has placed a townhall
     *
     * @return whether the player has placed a townhall
     */
    public boolean hasPlacedTownHall()
    {
        return hasPlacedTownHall;
    }

    /**
     * Sets whether the player has placed a townhall
     *
     * @param hasPlacedTownHall boolean
     */
    public void setHasPlacedTownHall(boolean hasPlacedTownHall)
    {
        this.hasPlacedTownHall = hasPlacedTownHall;
    }

    /**
     * Gets the property whether the player has placed a supply chest
     *
     * @return whether the player has placed a supply chest.
     */
    public boolean hasPlacedSupplyChest()
    {
        return hasPlacedSupplyChest;
    }

    /**
     * Sets whether the player has placed a townhall
     *
     * @param hasPlacedSupplyChest boolean
     */
    public void setHasPlacedSupplyChest(boolean hasPlacedSupplyChest)
    {
        this.hasPlacedSupplyChest = hasPlacedSupplyChest;
    }
}