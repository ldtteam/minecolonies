package com.minecolonies.coremod.colony.managers;

import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.colony.managers.interfaces.IColonyPackageManager;
import com.minecolonies.api.colony.workorders.IWorkManager;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.network.messages.PermissionsMessage;
import com.minecolonies.coremod.network.messages.client.colony.ColonyViewMessage;
import com.minecolonies.coremod.network.messages.client.colony.ColonyViewWorkOrderMessage;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.colony.IColony.CLOSE_COLONY_CAP;
import static com.minecolonies.api.util.constant.ColonyConstants.UPDATE_STATE_INTERVAL;
import static com.minecolonies.api.util.constant.Constants.TICKS_HOUR;

public class ColonyPackageManager implements IColonyPackageManager
{
    /**
     * List of players close to the colony receiving updates. Populated by chunk entry events
     */
    @NotNull
    private Set<ServerPlayer> closeSubscribers = new HashSet<>();

    /**
     * List of players with global permissions, like receiving important messages from far away. Populated on player login and logoff.
     */
    private Set<ServerPlayer> importantColonyPlayers = new HashSet<>();

    /**
     * New subscribers which havent received a view yet.
     */
    private Set<ServerPlayer> newSubscribers = new HashSet<>();

    /**
     * Variables taking care of updating the views.
     */
    private boolean isDirty = false;

    /**
     * Amount of ticks passed.
     */
    private int ticksPassed = 0;

    /**
     * The last contact in hours.
     */
    private int lastContactInHours = 0;

    /**
     * The colony of the manager.
     */
    private final Colony colony;

    /**
     * Creates the ColonyPackageManager for a colony.
     *
     * @param colony the colony.
     */
    public ColonyPackageManager(final Colony colony)
    {
        this.colony = colony;
    }

    @Override
    public int getLastContactInHours()
    {
        return lastContactInHours;
    }

    @Override
    public void setLastContactInHours(final int lastContactInHours)
    {
        this.lastContactInHours = lastContactInHours;
    }

    @Override
    public Set<ServerPlayer> getCloseSubscribers()
    {
        return closeSubscribers;
    }

    @Override
    public void updateSubscribers()
    {
        final Level world = colony.getWorld();
        // If the world or server is null, don't try to update the closeSubscribers this tick.
        if (world == null || world.getServer() == null)
        {
            return;
        }

        updateClosePlayers();
        updateColonyViews();
    }

    /**
     * Updates currently close players to the colony
     */
    private void updateClosePlayers()
    {
        for (Iterator<ServerPlayer> iterator = closeSubscribers.iterator(); iterator.hasNext(); )
        {
            final ServerPlayer player = iterator.next();

            if (!player.isAlive() || colony.getWorld() != player.level || !WorldUtil.isChunkLoaded(player.level, player.chunkPosition().x, player.chunkPosition().z))
            {
                iterator.remove();
                continue;
            }

            final LevelChunk chunk = colony.getWorld().getChunk(player.chunkPosition().x, player.chunkPosition().z);
            if (chunk.isEmpty())
            {
                iterator.remove();
                continue;
            }

            final IColonyTagCapability colonyCap = chunk.getCapability(CLOSE_COLONY_CAP, null).resolve().orElse(null);
            if (colonyCap == null)
            {
                iterator.remove();
                continue;
            }

            if (colonyCap.getOwningColony() != colony.getID())
            {
                iterator.remove();
            }
        }
    }

    /**
     * Updates the away timer for the colony.
     */
    @Override
    public void updateAwayTime()
    {
        if (importantColonyPlayers.isEmpty())
        {
            if (ticksPassed >= TICKS_HOUR)
            {
                ticksPassed = 0;
                lastContactInHours++;
                colony.markDirty();
            }
            ticksPassed += UPDATE_STATE_INTERVAL;
        }
        else if (lastContactInHours != 0)
        {
            lastContactInHours = 0;
            ticksPassed = 0;
            colony.markDirty();
        }
    }

    /**
     * Update the closeSubscribers of the colony.
     */
    public void updateColonyViews()
    {
        if (!closeSubscribers.isEmpty() || !newSubscribers.isEmpty())
        {
            //  Send each type of update packet as appropriate:
            //      - To close Subscribers if the data changes
            //      - To New Subscribers even if it hasn't changed

            //ColonyView
            sendColonyViewPackets();

            //Permissions
            sendPermissionsPackets();

            //WorkOrders
            sendWorkOrderPackets();

            colony.getCitizenManager().sendPackets(closeSubscribers, newSubscribers);
            colony.getVisitorManager().sendPackets(closeSubscribers, newSubscribers);
            colony.getBuildingManager().sendPackets(closeSubscribers, newSubscribers);
            colony.getResearchManager().sendPackets(closeSubscribers, newSubscribers);
        }

        if (newSubscribers.isEmpty())
        {
            isDirty = false;
        }
        colony.getPermissions().clearDirty();
        colony.getBuildingManager().clearDirty();
        colony.getCitizenManager().clearDirty();
        colony.getVisitorManager().clearDirty();
        colony.getResearchManager().clearDirty();
        newSubscribers = new HashSet<>();
    }

    @Override
    public void sendColonyViewPackets()
    {
        if (isDirty || !newSubscribers.isEmpty())
        {
            final FriendlyByteBuf colonyFriendlyByteBuf = new FriendlyByteBuf(Unpooled.buffer());
            ColonyView.serializeNetworkData(colony, colonyFriendlyByteBuf, !newSubscribers.isEmpty());
            final Set<ServerPlayer> players = new HashSet<>();
            if (isDirty)
            {
                players.addAll(closeSubscribers);
            }
            players.addAll(newSubscribers);

            final ColonyViewMessage message = new ColonyViewMessage(colony, colonyFriendlyByteBuf);
            for (ServerPlayer player : players)
            {
                message.setIsNewSubscription(newSubscribers.contains(player));
                Network.getNetwork().sendToPlayer(message, player);
            }
        }
        colony.getRequestManager().setDirty(false);
    }

    @Override
    public void sendPermissionsPackets()
    {
        final Permissions permissions = colony.getPermissions();
        if (permissions.isDirty() || !newSubscribers.isEmpty())
        {
            final Set<ServerPlayer> players = new HashSet<>();
            if (isDirty)
            {
                players.addAll(closeSubscribers);
            }
            players.addAll(newSubscribers);
            players.forEach(player -> Network.getNetwork().sendToPlayer(new PermissionsMessage.View(colony, permissions.getRank(player)), player));
        }
    }

    @Override
    public void sendWorkOrderPackets()
    {
        final IWorkManager workManager = colony.getWorkManager();
        if (workManager.isDirty() || !newSubscribers.isEmpty())
        {
            final Set<ServerPlayer> players = new HashSet<>();

            players.addAll(closeSubscribers);
            players.addAll(newSubscribers);

            List<IWorkOrder> workOrders = new ArrayList<>(workManager.getWorkOrders().values());
            final ColonyViewWorkOrderMessage message = new ColonyViewWorkOrderMessage(colony, workOrders);
            players.forEach(player -> Network.getNetwork().sendToPlayer(message, player));

            workManager.setDirty(false);
        }
    }

    @Override
    public void setDirty()
    {
        this.isDirty = true;
    }

    @Override
    public void addCloseSubscriber(@NotNull final ServerPlayer subscriber)
    {
        if (!closeSubscribers.contains(subscriber))
        {
            closeSubscribers.add(subscriber);
            newSubscribers.add(subscriber);
            updateColonyViews();
        }
    }

    @Override
    public void removeCloseSubscriber(@NotNull final ServerPlayer player)
    {
        newSubscribers.remove(player);
        closeSubscribers.remove(player);
    }

    /**
     * On login we're adding global subscribers.
     */
    @Override
    public void addImportantColonyPlayer(@NotNull final ServerPlayer subscriber)
    {
        importantColonyPlayers.add(subscriber);
        newSubscribers.add(subscriber);
    }

    /**
     * On logoff we're removing global subscribers.
     */
    @Override
    public void removeImportantColonyPlayer(@NotNull final ServerPlayer subscriber)
    {
        importantColonyPlayers.remove(subscriber);
        newSubscribers.remove(subscriber);
    }

    /**
     * Returns the list of online global subscribers of the colony.
     */
    @Override
    public Set<ServerPlayer> getImportantColonyPlayers()
    {
        return importantColonyPlayers;
    }
}
