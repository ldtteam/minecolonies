package com.minecolonies.coremod.colony.managers;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IVisitorData;
import com.minecolonies.api.colony.managers.interfaces.IVisitorManager;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.VisitorData;
import com.minecolonies.coremod.entity.citizen.VisitorCitizen;
import com.minecolonies.coremod.network.messages.client.colony.ColonyVisitorViewDataMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Manages all visiting entities to the colony
 */
public class VisitorManager implements IVisitorManager
{
    /**
     * NBT Tags
     */
    public static String TAG_VISIT_MANAGER = "visitManager";
    public static String TAG_VISITORS      = "visitors";
    public static String TAG_NEXTID        = "nextID";

    /**
     * Map with visitor ID and data
     */
    private Map<Integer, IVisitorData> visitorMap = new HashMap<>();

    /**
     * Whether this manager is dirty and needs re-serialize
     */
    private boolean isDirty = false;

    /**
     * The colony of the manager.
     */
    private final IColony colony;

    /**
     * The next free ID
     */
    private int nextVisitorID = -1;

    public VisitorManager(final IColony colony)
    {
        this.colony = colony;
    }

    @Override
    public void registerCitizen(final AbstractEntityCitizen visitor)
    {
        if (visitor.getCitizenId() == 0 || visitorMap.get(visitor.getCitizenId()) == null)
        {
            visitor.remove();
            return;
        }

        final ICitizenData data = visitorMap.get(visitor.getCitizenId());
        final Optional<AbstractEntityCitizen> existingCitizen = data.getCitizenEntity();

        if (!existingCitizen.isPresent())
        {
            data.setCitizenEntity(visitor);
            visitor.setCitizenData(data);
            return;
        }

        if (existingCitizen.get() == visitor)
        {
            return;
        }

        if (!existingCitizen.get().isAlive() || !WorldUtil.isEntityBlockLoaded(colony.getWorld(), existingCitizen.get().getPosition()))
        {
            existingCitizen.get().remove();
            data.setCitizenEntity(visitor);
            visitor.setCitizenData(data);
            return;
        }

        visitor.remove();
    }

    @Override
    public void unregisterCitizen(final AbstractEntityCitizen citizen)
    {
        final ICitizenData data = visitorMap.get(citizen.getCitizenId());
        if (data != null && data.getCitizenEntity().isPresent() && data.getCitizenEntity().get() == citizen)
        {
            visitorMap.get(citizen.getCitizenId()).setCitizenEntity(null);
        }
    }

    @Override
    public void read(@NotNull final CompoundNBT compound)
    {
        if (compound.contains(TAG_VISIT_MANAGER))
        {
            final CompoundNBT visitorManagerNBT = compound.getCompound(TAG_VISIT_MANAGER);
            final ListNBT citizenList = visitorManagerNBT.getList(TAG_VISITORS, Constants.NBT.TAG_COMPOUND);
            for (final INBT citizen : citizenList)
            {
                final IVisitorData data = VisitorData.loadVisitorFromNBT(colony, (CompoundNBT) citizen);
                visitorMap.put(data.getId(), data);
            }

            nextVisitorID = visitorManagerNBT.getInt(TAG_NEXTID);
        }
        markCitizensDirty();
    }

    @Override
    public void write(@NotNull final CompoundNBT citizenCompound)
    {
        final CompoundNBT visitorManagerNBT = new CompoundNBT();

        final ListNBT citizenList = new ListNBT();
        for (Map.Entry<Integer, IVisitorData> entry : visitorMap.entrySet())
        {
            citizenList.add(entry.getValue().serializeNBT());
        }

        visitorManagerNBT.put(TAG_VISITORS, citizenList);
        visitorManagerNBT.putInt(TAG_NEXTID, nextVisitorID);
        citizenCompound.put(TAG_VISIT_MANAGER, visitorManagerNBT);
    }

    @Override
    public void sendPackets(
      @NotNull final Set<ServerPlayerEntity> closeSubscribers, @NotNull final Set<ServerPlayerEntity> newSubscribers)
    {
        Set<ServerPlayerEntity> players = new HashSet<>(newSubscribers);
        players.addAll(closeSubscribers);
        Set<IVisitorData> toSend = new HashSet<>();
        boolean refresh = !newSubscribers.isEmpty() || this.isDirty;

        if (refresh)
        {
            toSend = new HashSet<>(visitorMap.values());
            for (final IVisitorData data : visitorMap.values())
            {
                data.clearDirty();
            }
            this.clearDirty();
        }
        else
        {
            for (final IVisitorData data : visitorMap.values())
            {
                if (data.isDirty())
                {
                    toSend.add(data);
                }
                data.clearDirty();
            }
        }

        if (toSend.isEmpty())
        {
            return;
        }

        final ColonyVisitorViewDataMessage message = new ColonyVisitorViewDataMessage(colony, toSend, refresh);

        for (final ServerPlayerEntity player : players)
        {
            Network.getNetwork().sendToPlayer(message, player);
        }
    }

    @NotNull
    @Override
    public Map<Integer, ICitizenData> getCitizenMap()
    {
        return Collections.unmodifiableMap(visitorMap);
    }

    @Override
    public ICitizenData getCitizen(final int citizenId)
    {
        return visitorMap.get(citizenId);
    }

    @Override
    public <T extends IVisitorData> T getVisitor(int citizenId)
    {
        return (T) visitorMap.get(citizenId);
    }

    @Override
    public ICitizenData spawnOrCreateCitizen(ICitizenData data, final World world, final BlockPos spawnPos, final boolean force)
    {
        if (!WorldUtil.isEntityBlockLoaded(world, spawnPos))
        {
            return data;
        }

        if (data == null)
        {
            data = createAndRegisterNewCitizenData();
        }

        VisitorCitizen citizenEntity = (VisitorCitizen) ModEntities.VISITOR.create(colony.getWorld());

        if (citizenEntity == null)
        {
            return data;
        }

        data.setCitizenEntity(citizenEntity);
        data.initEntityValues();

        citizenEntity.setPosition(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        world.addEntity(citizenEntity);
        return data;
    }

    @Override
    public ICitizenData createAndRegisterNewCitizenData()
    {
        markCitizensDirty();
        final IVisitorData data = new VisitorData(nextVisitorID--, colony);
        data.initForNewCitizen();
        visitorMap.put(data.getId(), data);
        return data;
    }

    @Override
    public void removeCitizen(@NotNull final ICitizenData citizen)
    {
        final IVisitorData data = visitorMap.remove(citizen.getId());
        if (data != null && data.getCitizenEntity().isPresent())
        {
            data.getCitizenEntity().get().remove();
        }
    }

    @Override
    public void markCitizensDirty()
    {
        this.isDirty = true;
    }

    @Override
    public void clearDirty()
    {
        this.isDirty = false;
    }

    @Override
    public void onColonyTick(final IColony colony)
    {
        if (colony.hasTownHall())
        {
            for (final IVisitorData data : visitorMap.values())
            {
                data.updateCitizenEntityIfNecessary();
            }
        }
    }
}
