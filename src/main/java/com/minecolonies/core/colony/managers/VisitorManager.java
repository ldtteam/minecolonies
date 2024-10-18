package com.minecolonies.core.colony.managers;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IVisitorData;
import com.minecolonies.api.colony.managers.interfaces.IVisitorManager;
import com.minecolonies.api.entity.citizen.AbstractCivilianEntity;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.visitor.AbstractEntityVisitor;
import com.minecolonies.api.entity.visitor.IVisitorType;
import com.minecolonies.api.entity.visitor.ModVisitorTypes;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.VisitorData;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionType;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionTypeManager;
import com.minecolonies.core.colony.interactionhandling.ExpeditionInteraction;
import com.minecolonies.core.entity.visitor.ExpeditionaryVisitorType.DespawnTimeData.DespawnTime;
import com.minecolonies.core.network.messages.client.colony.ColonyVisitorViewDataMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.Constants.SLIGHTLY_UP;
import static com.minecolonies.api.util.constant.PathingConstants.HALF_A_BLOCK;
import static com.minecolonies.core.entity.visitor.ExpeditionaryVisitorType.DEFAULT_DESPAWN_TIME;
import static com.minecolonies.core.entity.visitor.ExpeditionaryVisitorType.EXTRA_DATA_DESPAWN_TIME;

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
     * The colony of the manager.
     */
    private final IColony colony;
    /**
     * Map with visitor ID and data
     */
    private Map<Integer, IVisitorData> visitorMap = new HashMap<>();
    /**
     * Whether this manager is dirty and needs re-serialize
     */
    private boolean isDirty = false;
    /**
     * The next free ID
     */
    private int nextVisitorID = -1;

    public VisitorManager(final IColony colony)
    {
        this.colony = colony;
    }

    @Override
    public void registerCivilian(final AbstractCivilianEntity visitor)
    {
        if (visitor.getCivilianID() == 0 || visitorMap.get(visitor.getCivilianID()) == null)
        {
            if (!visitor.isAddedToWorld())
            {
                Log.getLogger().warn("Discarding entity not added to world, should be only called after:", new Exception());
            }
            visitor.remove(Entity.RemovalReason.DISCARDED);
            return;
        }

        final ICitizenData data = visitorMap.get(visitor.getCivilianID());

        if (data == null || !visitor.getUUID().equals(data.getUUID()))
        {
            if (!visitor.isAddedToWorld())
            {
                Log.getLogger().warn("Discarding entity not added to world, should be only called after:", new Exception());
            }
            visitor.remove(Entity.RemovalReason.DISCARDED);
            return;
        }

        final Optional<AbstractEntityCitizen> existingCitizen = data.getEntity();

        if (!existingCitizen.isPresent())
        {
            data.setEntity(visitor);
            visitor.setCivilianData(data);
            return;
        }

        if (existingCitizen.get() == visitor)
        {
            return;
        }

        if (visitor.isAlive())
        {
            existingCitizen.get().remove(Entity.RemovalReason.DISCARDED);
            data.setEntity(visitor);
            visitor.setCivilianData(data);
            return;
        }

        if (!visitor.isAddedToWorld())
        {
            Log.getLogger().warn("Discarding entity not added to world, should be only called after:", new Exception());
        }
        visitor.remove(Entity.RemovalReason.DISCARDED);
    }

    @Override
    public void unregisterCivilian(final AbstractCivilianEntity entity)
    {
        final ICitizenData data = visitorMap.get(entity.getCivilianID());
        if (data != null && data.getEntity().isPresent() && data.getEntity().get() == entity)
        {
            visitorMap.get(entity.getCivilianID()).setEntity(null);
        }
    }

    @Override
    public void read(@NotNull final CompoundTag compound)
    {
        if (compound.contains(TAG_VISIT_MANAGER))
        {
            final CompoundTag visitorManagerNBT = compound.getCompound(TAG_VISIT_MANAGER);
            final ListTag citizenList = visitorManagerNBT.getList(TAG_VISITORS, Tag.TAG_COMPOUND);
            for (final Tag citizen : citizenList)
            {
                final IVisitorData data = VisitorData.loadVisitorFromNBT(colony, (CompoundTag) citizen);
                visitorMap.put(data.getId(), data);
            }

            nextVisitorID = visitorManagerNBT.getInt(TAG_NEXTID);
        }
        markDirty();
    }

    @Override
    public void write(@NotNull final CompoundTag compoundNBT)
    {
        final CompoundTag visitorManagerNBT = new CompoundTag();

        final ListTag citizenList = new ListTag();
        for (Map.Entry<Integer, IVisitorData> entry : visitorMap.entrySet())
        {
            citizenList.add(entry.getValue().serializeNBT());
        }

        visitorManagerNBT.put(TAG_VISITORS, citizenList);
        visitorManagerNBT.putInt(TAG_NEXTID, nextVisitorID);
        compoundNBT.put(TAG_VISIT_MANAGER, visitorManagerNBT);
    }

    @Override
    public void sendPackets(@NotNull final Set<ServerPlayer> closeSubscribers, @NotNull final Set<ServerPlayer> newSubscribers)
    {
        Set<ServerPlayer> players = new HashSet<>(newSubscribers);
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

        for (final ServerPlayer player : players)
        {
            Network.getNetwork().sendToPlayer(message, player);
        }
    }

    @NotNull
    @Override
    public Map<Integer, IVisitorData> getCivilianDataMap()
    {
        return Collections.unmodifiableMap(visitorMap);
    }

    @Override
    public IVisitorData getCivilian(final int citizenId)
    {
        return visitorMap.get(citizenId);
    }

    @Override
    public void removeCivilian(@NotNull final IVisitorData citizen)
    {
        final IVisitorData data = visitorMap.remove(citizen.getId());
        if (data != null && data.getEntity().isPresent())
        {
            data.getEntity().get().remove(Entity.RemovalReason.DISCARDED);
        }
    }

    @Override
    public void markDirty()
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
                data.updateEntityIfNecessary();
            }

            if (colony.getExpeditionManager().canStartNewExpedition())
            {
                spawnExpeditionary();
            }
        }
    }

    @Override
    public boolean tickVisitorData()
    {
        for (IVisitorData visitorData : this.getCivilianDataMap().values())
        {
            visitorData.update();
        }
        return false;
    }

    @Override
    public IVisitorData spawnOrCreateVisitor(final IVisitorType visitorType, IVisitorData data, final Level world, final BlockPos spawnPos)
    {
        if (!WorldUtil.isEntityBlockLoaded(world, spawnPos))
        {
            return data;
        }

        if (data == null)
        {
            data = createAndRegisterVisitorData(visitorType);
        }

        final AbstractEntityVisitor citizenEntity = visitorType.getEntityCreator().apply(world);
        if (citizenEntity == null)
        {
            return data;
        }

        citizenEntity.setUUID(data.getUUID());
        citizenEntity.setPos(spawnPos.getX() + HALF_A_BLOCK, spawnPos.getY() + SLIGHTLY_UP, spawnPos.getZ() + HALF_A_BLOCK);
        world.addFreshEntity(citizenEntity);

        citizenEntity.setCitizenId(data.getId());
        citizenEntity.getCitizenColonyHandler().setColonyId(colony.getID());
        if (citizenEntity.isAddedToWorld())
        {
            citizenEntity.getCitizenColonyHandler().registerWithColony(data.getColony().getID(), data.getId());
        }

        return data;
    }

    @Override
    public IVisitorData createAndRegisterVisitorData(final IVisitorType visitorType)
    {
        markDirty();
        final IVisitorData data = new VisitorData(nextVisitorID--, colony, visitorType);
        data.initForNewCivilian();
        visitorMap.put(data.getId(), data);
        return data;
    }

    /**
     * Spawn an expeditionary citizen.
     */
    public void spawnExpeditionary()
    {
        final ColonyExpeditionType expeditionType = ColonyExpeditionTypeManager.getInstance().getRandomExpeditionType(colony);
        if (expeditionType != null)
        {
            final IVisitorData newVisitor = createAndRegisterVisitorData(ModVisitorTypes.expeditionary.get());
            newVisitor.setExtraDataValue(EXTRA_DATA_DESPAWN_TIME, DespawnTime.fromNow(colony.getWorld(), DEFAULT_DESPAWN_TIME));
            newVisitor.triggerInteraction(new ExpeditionInteraction());

            if (colony.getExpeditionManager().addExpedition(newVisitor.getId(), expeditionType.id()))
            {
                spawnOrCreateVisitor(ModVisitorTypes.expeditionary.get(),
                  newVisitor,
                  colony.getWorld(),
                  BlockPosUtil.findSpawnPosAround(colony.getWorld(), colony.getBuildingManager().getTownHall().getPosition()));
            }
        }
    }
}
