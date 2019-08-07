package com.minecolonies.coremod.colony.managers;

import com.ldtteam.structurize.management.Structures;
import com.minecolonies.api.colony.managers.interfaces.IColonyPackageManager;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.colony.workorders.IWorkManager;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildMiner;
import com.minecolonies.coremod.network.messages.ColonyStylesMessage;
import com.minecolonies.coremod.network.messages.ColonyViewMessage;
import com.minecolonies.coremod.network.messages.ColonyViewWorkOrderMessage;
import com.minecolonies.coremod.network.messages.PermissionsMessage;
import com.minecolonies.coremod.util.ColonyUtils;
import net.minecraft.network.PacketBuffer;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import static com.minecolonies.api.util.constant.ColonyConstants.MAX_SQ_DIST_OLD_SUBSCRIBER_UPDATE;
import static com.minecolonies.api.util.constant.ColonyConstants.MAX_SQ_DIST_SUBSCRIBER_UPDATE;
import static com.minecolonies.api.util.constant.Constants.TICKS_HOUR;

public class ColonyPackageManager implements IColonyPackageManager
{
    /**
     * 1 in x chance to update the permissions.
     */
    private static final int CHANCE_TO_UPDATE = 1000;

    /**
     * List of players subscribing to the colony already known for a long time.
     */
    @NotNull
    private Set<ServerPlayerEntity> oldSubscribers = new HashSet<>();

    /**
     * List of players subscribing to the colony.
     */
    @NotNull
    private Set<ServerPlayerEntity> subscribers   = new HashSet<>();

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
    public Set<ServerPlayerEntity> getSubscribers()
    {
        final Set<ServerPlayerEntity> set = new HashSet<>(oldSubscribers);
        set.addAll(subscribers);
        return set;
    }

    @Override
    public void updateSubscribers()
    {
        final World world = colony.getWorld();
        // If the world or server is null, don't try to update the subscribers this tick.
        if (world == null || world.getServer() == null)
        {
            return;
        }

        // Add owners
        world.getServer().getPlayerList().getPlayers()
                .stream()
                .filter(colony.getPermissions()::isSubscriber)
                .forEach(subscribers::add);

        //  Add nearby players
        for (final PlayerEntity o : world.getPlayers())
        {
            if (o instanceof ServerPlayerEntity)
            {
                @NotNull final ServerPlayerEntity player = (ServerPlayerEntity) o;

                if (player.connection.networkTickCount < 5)
                {
                    continue;
                }

                final double distance = player.getDistanceSq(new Vec3d(colony.getCenter()));
                if (distance < MAX_SQ_DIST_SUBSCRIBER_UPDATE
                        || (oldSubscribers.contains(player) && distance < MAX_SQ_DIST_OLD_SUBSCRIBER_UPDATE))
                {
                    // Players become subscribers if they come within 16 blocks of the edge of the colony
                    // Players remain subscribers while they remain within double the colony's radius
                    subscribers.add(player);
                }
            }
        }

        if (subscribers.isEmpty())
        {
            if (ticksPassed >= TICKS_HOUR)
            {
                ticksPassed = 0;
                lastContactInHours++;
                colony.markDirty();
            }
            ticksPassed++;
        }
        else if (lastContactInHours != 0)
        {
            lastContactInHours = 0;
            ticksPassed = 0;
            colony.markDirty();
        }

        final boolean hasNewSubscribers = ColonyUtils.hasNewSubscribers(oldSubscribers, subscribers);
        updateColonyViews(hasNewSubscribers);
    }


    /**
     * Update the subscribers of the colony.
     * @param hasNewSubscribers check if there are new ones.
     */
    public void updateColonyViews(final boolean hasNewSubscribers)
    {
        if (!subscribers.isEmpty())
        {
            //  Determine if any new subscribers were added this pass

            //  Send each type of update packet as appropriate:
            //      - To Subscribers if the data changes
            //      - To New Subscribers even if it hasn't changed

            //ColonyView
            sendColonyViewPackets(oldSubscribers, hasNewSubscribers);

            //Permissions
            sendPermissionsPackets(oldSubscribers, hasNewSubscribers);

            //WorkOrders
            sendWorkOrderPackets(oldSubscribers, hasNewSubscribers);

            colony.getCitizenManager().sendPackets(oldSubscribers, hasNewSubscribers, subscribers);

            colony.getBuildingManager().sendPackets(oldSubscribers, hasNewSubscribers, subscribers);

            //schematics
            if (Structures.isDirty())
            {
                sendSchematicsPackets(hasNewSubscribers);
                Structures.clearDirty();
            }
        }

        isDirty = false;
        colony.getPermissions().clearDirty();
        colony.getBuildingManager().clearDirty();
        colony.getCitizenManager().clearDirty();
        oldSubscribers = new HashSet<>(subscribers);
        subscribers = new HashSet<>();
    }

    @Override
    public void sendColonyViewPackets(@NotNull final Set<ServerPlayerEntity> oldSubscribers, final boolean hasNewSubscribers)
    {
        if (isDirty || hasNewSubscribers)
        {
            final PacketBuffer colonyPacketBuffer = new PacketBuffer(Unpooled.buffer());
            ColonyView.serializeNetworkData(colony, colonyPacketBuffer, hasNewSubscribers);
            for (final ServerPlayerEntity player : subscribers)
            {
                final boolean isNewSubscriber = !oldSubscribers.contains(player);
                if (isDirty || isNewSubscriber)
                {
                    Network.getNetwork().sendToPlayer(new ColonyViewMessage(colony, colonyPacketBuffer, isNewSubscriber), player);
                }
            }
        }
        colony.getRequestManager().setDirty(false);
    }

    @Override
    public void sendPermissionsPackets(@NotNull final Set<ServerPlayerEntity> oldSubscribers, final boolean hasNewSubscribers)
    {
        final Permissions permissions = colony.getPermissions();
        if (permissions.isDirty() || hasNewSubscribers || colony.getWorld().rand.nextInt(CHANCE_TO_UPDATE) <= 1)
        {
            subscribers
                    .stream()
                    .filter(player -> permissions.isDirty() || !oldSubscribers.contains(player)).forEach(player ->
            {
                final Rank rank = permissions.getRank(player);
                Network.getNetwork().sendToPlayer(new PermissionsMessage.View(colony, rank), player);
            });
        }
    }

    @Override
    public void sendWorkOrderPackets(@NotNull final Set<ServerPlayerEntity> oldSubscribers, final boolean hasNewSubscribers)
    {
        final IWorkManager workManager = colony.getWorkManager();
        if (workManager.isDirty() || hasNewSubscribers)
        {
            for (final IWorkOrder workOrder : workManager.getWorkOrders().values())
            {
                if (!(workOrder instanceof WorkOrderBuildMiner))
                {
                    subscribers.stream().filter(player -> workManager.isDirty() || !oldSubscribers.contains(player))
                            .forEach(player -> Network.getNetwork().sendToPlayer(new ColonyViewWorkOrderMessage(colony, workOrder), player));
                }
            }

            workManager.setDirty(false);
        }
    }

    @Override
    public void sendSchematicsPackets(final boolean hasNewSubscribers)
    {
        if (Structures.isDirty() || hasNewSubscribers)
        {
            subscribers.stream()
                    .forEach(player -> Network.getNetwork().sendToPlayer(new ColonyStylesMessage(), player));
        }
    }

    @Override
    public void setDirty()
    {
        this.isDirty = true;
    }

    @Override
    public void addSubscribers(@NotNull final ServerPlayerEntity subscriber)
    {
        subscribers.add(subscriber);
    }

    @Override
    public void removeSubscriber(@NotNull final ServerPlayerEntity player)
    {
        if(!colony.getMessagePlayerEntitys().contains(player))
        {
            subscribers.remove(player);
        }
    }
}
