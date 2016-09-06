package com.minecolonies.entity;

import com.minecolonies.lib.Constants;
import com.minecolonies.proxy.CommonProxy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import org.jetbrains.annotations.NotNull;

public final class PlayerProperties implements IExtendedEntityProperties
{
    private boolean hasPlacedSupplyChest = false;

    private PlayerProperties() {}

    /**
     * Registers player property. Should be checked if already exists, and called in onEntityConstruct event.
     *
     * @param player player to create property for
     */
    public static void register(@NotNull EntityPlayer player)
    {
        player.registerExtendedProperties(Constants.PLAYER_PROPERTY_NAME, new PlayerProperties());
    }

    /**
     * Saves NBT data to proxy HashMap.
     *
     * @param player to save data for
     */
    public static void saveProxyData(@NotNull EntityPlayer player)
    {
        @NotNull final PlayerProperties playerData = PlayerProperties.get(player);
        @NotNull final NBTTagCompound savedData = new NBTTagCompound();

        playerData.saveNBTData(savedData);

        CommonProxy.storeEntityData(getSaveKey(player), savedData);
    }

    /**
     * Gets the player properties for a player.
     *
     * @param player player to get property for
     * @return PlayerProperties for the player.
     */
    @NotNull
    public static PlayerProperties get(@NotNull EntityPlayer player)
    {
        return (PlayerProperties) player.getExtendedProperties(Constants.PLAYER_PROPERTY_NAME);
    }

    @Override
    public void saveNBTData(@NotNull NBTTagCompound compound)
    {
        @NotNull final NBTTagCompound properties = new NBTTagCompound();

        properties.setBoolean("hasPlacedSupplyChest", hasPlacedSupplyChest);

        compound.setTag(Constants.PLAYER_PROPERTY_NAME, properties);
    }

    @Override
    public void loadNBTData(@NotNull NBTTagCompound compound)
    {
        @NotNull final NBTTagCompound properties = (NBTTagCompound) compound.getTag(Constants.PLAYER_PROPERTY_NAME);

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
    @NotNull
    private static String getSaveKey(@NotNull EntityPlayer player)
    {
        return player.getGameProfile().getId().toString() + ":" + Constants.PLAYER_PROPERTY_NAME;
    }

    /**
     * Loads NBT data from proxy HashMap.
     *
     * @param player to load data for
     */
    public static void loadProxyData(@NotNull EntityPlayer player)
    {
        @NotNull final PlayerProperties playerData = PlayerProperties.get(player);
        final NBTTagCompound savedData = CommonProxy.getEntityData(getSaveKey(player));

        if (savedData != null)
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
