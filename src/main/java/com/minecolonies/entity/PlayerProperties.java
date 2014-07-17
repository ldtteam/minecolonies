package com.minecolonies.entity;

import com.minecolonies.lib.Constants;
import com.minecolonies.proxy.CommonProxy;
import com.minecolonies.util.Vec3Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class PlayerProperties implements IExtendedEntityProperties
{
    private boolean hasPlacedTownHall = false;
    private Vec3 townhallPos;
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
        if(townhallPos != null) Vec3Utils.writeVecToNBT(properties, "townhall", townhallPos);
        properties.setBoolean("hasPlacedSupplyChest", hasPlacedSupplyChest);

        compound.setTag(Constants.PlayerPropertyName, properties);
    }

    @Override
    public void loadNBTData(NBTTagCompound compound)
    {
        NBTTagCompound properties = (NBTTagCompound) compound.getTag(Constants.PlayerPropertyName);

        this.hasPlacedTownHall = properties.getBoolean("hasPlacedTownHall");
        if(properties.hasKey("townhall")) this.townhallPos = Vec3Utils.readVecFromNBT(properties, "townhall");
        else if(properties.hasKey("townhallX") && properties.hasKey("townhallY") && properties.hasKey("townhallZ"))
        {
            int x = properties.getInteger("townhallX");
            int y = properties.getInteger("townhallY");
            int z = properties.getInteger("townhallZ");
            this.townhallPos = Vec3.createVectorHelper(x, y, z);
        }
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
        setTownhallPos(Vec3.createVectorHelper(x, y, z));
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
     * Returns the townhall position
     *
     * @return townhall position
     */
    public Vec3 getTownhallPos()
    {
        return townhallPos;
    }

    private void setTownhallPos(Vec3 pos)
    {
        townhallPos = pos;
    }
}