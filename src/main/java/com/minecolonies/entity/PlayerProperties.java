package com.minecolonies.entity;

import com.minecolonies.lib.Constants;
import com.minecolonies.proxy.CommonProxy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class PlayerProperties implements IExtendedEntityProperties
{
    private boolean hasPlacedTownHall = false;
    private int     townhallX         = 0, townhallY = 0, townhallZ = 0;
    private boolean hasPlacedSupplyChest = false;

    private PlayerProperties(){}

    /**
     * Registers player property. Should be checked if already exists, and called in onEntityConstruct event
     *
     * @param player player to create property for
     */
    public static void register(EntityPlayer player)
    {
        player.registerExtendedProperties(Constants.PlayerPropertyName, new PlayerProperties());
    }

    /**
     * Gets the player properties for a player
     *
     * @param player player to get property for
     * @return PlayerProperties for the player.
     */
    public static PlayerProperties get(EntityPlayer player)
    {
        return (PlayerProperties) player.getExtendedProperties(Constants.PlayerPropertyName);
    }

    @Override
    public void saveNBTData(NBTTagCompound compound)
    {
        NBTTagCompound properties = new NBTTagCompound();

        properties.setBoolean("hasPlacedTownHall", hasPlacedTownHall);
        properties.setInteger("townhallX", townhallX);
        properties.setInteger("townhallY", townhallY);
        properties.setInteger("townhallZ", townhallZ);
        properties.setBoolean("hasPlacedSupplyChest", hasPlacedSupplyChest);

        compound.setTag(Constants.PlayerPropertyName, properties);
    }

    @Override
    public void loadNBTData(NBTTagCompound compound)
    {
        NBTTagCompound properties = (NBTTagCompound) compound.getTag(Constants.PlayerPropertyName);

        this.hasPlacedTownHall = properties.getBoolean("hasPlacedTownHall");
        this.townhallX = properties.getInteger("townhallX");
        this.townhallY = properties.getInteger("townhallY");
        this.townhallZ = properties.getInteger("townhallZ");
        this.hasPlacedSupplyChest = properties.getBoolean("hasPlacedSupplyChest");
    }

    @Override
    public void init(Entity entity, World world)
    {

    }

    /**
     * Adds support for other mods and multiple properties tags
     *
     * @param player the player
     * @return String HashMap key
     */
    private static String getSaveKey(EntityPlayer player)
    {
        return player.getUniqueID().toString() + ":" + Constants.PlayerPropertyName;
    }

    /**
     * Saves NBT data to proxy HashMap.
     *
     * @param player to save data for
     */
    public static void saveProxyData(EntityPlayer player)
    {
        PlayerProperties playerData = PlayerProperties.get(player);
        NBTTagCompound savedData = new NBTTagCompound();

        playerData.saveNBTData(savedData);

        CommonProxy.storeEntityData(getSaveKey(player), savedData);
    }

    /**
     * Loads NBT data from proxy HashMap
     *
     * @param player to load data for
     */
    public static void loadProxyData(EntityPlayer player)
    {
        PlayerProperties playerData = PlayerProperties.get(player);
        NBTTagCompound savedData = CommonProxy.getEntityData(getSaveKey(player));

        if(savedData != null)
        {
            playerData.loadNBTData(savedData);
        }
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
    private void setHasPlacedTownHall(boolean hasPlacedTownHall)
    {
        this.hasPlacedTownHall = hasPlacedTownHall;
    }

    /**
     * Set hasPlacedTownHall to true and sets coordinates
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     */
    public void placeTownhall(int x, int y, int z)
    {
        setHasPlacedTownHall(true);
        setTownhallPos(x, y, z);
    }

    /**
     * Set hasPlacedTownHall to false. Should be called when removing the townhall.
     */
    public void removeTownhall()
    {
        setHasPlacedTownHall(false);
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
     * Sets hasPlacedSupplyChest to true
     */
    public void placeSupplyChest()
    {
        this.hasPlacedSupplyChest = true;
    }

    /**
     * Returns the townhall x coordinate
     *
     * @return townhall x coordinate
     */
    public int getTownhallX()
    {
        return townhallX;
    }

    /**
     * Returns the townhall y coordinate
     *
     * @return townhall y coordinate
     */
    public int getTownhallY()
    {
        return townhallY;
    }

    /**
     * Returns the townhall z coordinate
     *
     * @return townhall z coordinate
     */
    public int getTownhallZ()
    {
        return townhallZ;
    }

    private void setTownhallPos(int x, int y, int z)
    {
        this.townhallX = x;
        this.townhallY = y;
        this.townhallZ = z;
    }
}