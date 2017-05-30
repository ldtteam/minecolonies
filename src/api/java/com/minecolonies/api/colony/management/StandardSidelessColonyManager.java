package com.minecolonies.api.colony.management;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.configuration.Configurations;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class StandardSidelessColonyManager implements IColonyManager
{

    /**
     * Maps colonies to their Ids
     */
    private Map<IToken<?,?>, IColony> colonyMap = new HashMap<>();



    /**
     * Indicates if this {@link IColonyManager} is dirty and needs to be saved to disk.
     */
    private boolean dirty = false;


    @NotNull
    @Override
    public IColony createColony(
                           @NotNull final World w, final BlockPos pos, @NotNull final EntityPlayer player)
    {
        IColony newColony = StandardFactoryController.getInstance().getNewInstance(FactoryVoidInput.INSTANCE, w, pos, player);

        colonyMap.put(newColony.getID(), newColony);
        markDirty();

        return newColony;
    }

    @Override
    public void markDirty()
    {
        this.dirty = true;
    }

    @Override
    public boolean isDirty()
    {
        return dirty;
    }

    @Override
    public void deleteColony(final IToken id)
    {
        if (!colonyMap.containsKey(id)) {
            throw new IllegalArgumentException("Unknown colony id: " + id);
        }

        colonyMap.remove(id);
        markDirty();
    }

    @Override
    public IColony getColony(final IToken id)
    {
        return colonyMap.get(id);
    }

    @Override
    public void syncAllColoniesAchievements()
    {

    }

    @Override
    public IBuilding getBuilding(@NotNull final World w, @NotNull final BlockPos pos)
    {
        IColony colony = getColony(w, pos);
        return colony.getBuilding(pos);
    }

    @Override
    public IColony getColony(@NotNull final World w, @NotNull final BlockPos pos)
    {
        return getColonies(w).stream().filter(c -> c.isCoordInColony(w, pos)).findFirst().orElse(null);
    }

    @NotNull
    @Override
    public ImmutableList<IColony> getColonies(@NotNull final World w)
    {
        return ImmutableList.copyOf(getColonies().stream().filter(c -> c.getWorld().equals(w)).collect(Collectors.toSet()));
    }

    @NotNull
    @Override
    public ImmutableList<IColony> getColonies()
    {
        return ImmutableList.copyOf(colonyMap.values());
    }

    @Nullable
    @Override
    public IColony getClosestColony(@NotNull final World w, @NotNull final BlockPos pos)
    {
        return getColonies(w).stream().sorted(Comparator.comparingDouble(c -> c.getCenter().distanceSq(pos.getX(), pos.getY(), pos.getZ()))).findFirst().orElse(null);
    }

    @Nullable
    @Override
    public IColony getColonyByOwner(@NotNull final World w, @NotNull final EntityPlayer owner)
    {
        return getColonyByOwner(w, owner.getUniqueID());
    }

    @Nullable
    @Override
    public IColony getColonyByOwner(@NotNull final World w, final UUID owner)
    {
        return getColonies(w).stream().filter(c -> (owner.equals(c.getPermissions().getOwner()))).findFirst().orElse(null);
    }

    @Override
    public int getMinimumDistanceBetweenTownHalls()
    {
        //  [TownHall](Radius)+(Padding)+(Radius)[TownHall]
        return (2 * Configurations.workingRangeTownHall) + Configurations.townHallPadding;
    }

    @Override
    public void onServerTick(@NotNull final TickEvent.ServerTickEvent event)
    {
        colonyMap.values().forEach(c -> c.onServerTick(event));

        if (isDirty()) {

        }
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {

    }

    @Override
    public void onClientTick(@NotNull final TickEvent.ClientTickEvent event)
    {

    }

    @Override
    public void onWorldTick(@NotNull final TickEvent.WorldTickEvent event)
    {

    }

    @Override
    public void onWorldLoad(@NotNull final World world)
    {

    }

    @Override
    public boolean backupColonyData()
    {
        return false;
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {

    }

    @Override
    public void setServerUUID(final UUID uuid)
    {

    }

    @Override
    public UUID getServerUUID()
    {
        return null;
    }

    @Override
    public void onWorldSave(@NotNull final World world)
    {

    }

    @Override
    public void onWorldUnload(@NotNull final World world)
    {

    }

    @Override
    public boolean isSchematicDownloaded()
    {
        return false;
    }

    @Override
    public void setSchematicDownloaded(final boolean downloaded)
    {

    }

    @Override
    public boolean isCoordinateInAnyColony(@NotNull final World world, final BlockPos pos)
    {
        return getColony(world, pos) != null;
    }
}
