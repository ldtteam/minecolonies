package com.minecolonies.entity;

import com.minecolonies.lib.Constants;
import com.minecolonies.proxy.CommonProxy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public final class PlayerProperties implements IExtendedEntityProperties
{
    private boolean hasPlacedSupplyChest = false;

    private PlayerProperties(){}

    /**
     * Registers player property. Should be checked if already exists, and called in onEntityConstruct event.
     *
     * @param player player to create property for
     */
    public static void register(EntityPlayer player)
    {
        player.registerExtendedProperties(Constants.PLAYER_PROPERTY_NAME, new PlayerProperties());
    }

    /**
     * Gets the player properties for a player.
     *
     * @param player player to get property for
     * @return PlayerProperties for the player.
     */
    public static PlayerProperties get(EntityPlayer player)
    {
        return (PlayerProperties) player.getExtendedProperties(Constants.PLAYER_PROPERTY_NAME);
    }

    @Override
    public void saveNBTData(NBTTagCompound compound)
    {
        NBTTagCompound properties = new NBTTagCompound();

        properties.setBoolean("hasPlacedSupplyChest", hasPlacedSupplyChest);

        compound.setTag(Constants.PLAYER_PROPERTY_NAME, properties);
    }

    @Override
    public void loadNBTData(NBTTagCompound compound)
    {
        NBTTagCompound properties = (NBTTagCompound) compound.getTag(Constants.PLAYER_PROPERTY_NAME);

        this.hasPlacedSupplyChest = properties.getBoolean("hasPlacedSupplyChest");
    }

    @Override
    public void init(Entity entity, World world)
    {
        // Do nothing, need to override, but unused
    }

    /**
     * Adds support for other mods and multiple properties tags.
     *
     * @param player the player
     * @return String HashMap key
     */
    private static String getSaveKey(EntityPlayer player)
    {
        return player.getGameProfile().getId().toString() + ":" + Constants.PLAYER_PROPERTY_NAME;
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
     * Loads NBT data from proxy HashMap.
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
     * Gets the property whether the player has placed a supply chest.
     *
     * @return whether the player has placed a supply chest.
     */
    public boolean hasPlacedSupplyChest()
    {
        return hasPlacedSupplyChest;
    }

    /**
     * Sets hasPlacedSupplyChest to true.
     */
    public void placeSupplyChest()
    {
        this.hasPlacedSupplyChest = true;
    }
}
