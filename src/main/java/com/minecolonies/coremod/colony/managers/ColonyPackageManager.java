package com.minecolonies.coremod.colony.managers;

import com.ldtteam.structurize.management.Structures;
import com.minecolonies.api.colony.managers.interfaces.IColonyPackageManager;
import com.minecolonies.api.colony.workorders.IWorkManager;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildMiner;
import com.minecolonies.coremod.network.messages.ColonyStylesMessage;
import com.minecolonies.coremod.network.messages.ColonyViewMessage;
import com.minecolonies.coremod.network.messages.ColonyViewWorkOrderMessage;
import com.minecolonies.coremod.network.messages.PermissionsMessage;
import com.minecolonies.coremod.util.ColonyUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import static com.minecolonies.api.util.constant.ColonyConstants.UPDATE_SUBSCRIBERS_INTERVAL;
import static com.minecolonies.api.util.constant.Constants.TICKS_HOUR;

public class ColonyPackageManager implements IColonyPackageManager
{
    /**
     * List of players close to the colony receiving updates. Populated by chunk entry events
     */
    @NotNull
    private Set<EntityPlayerMP> closeSubscribers = new HashSet<>();

    /**
     * List of players with global permissions, like receiving important messages from far away.
     * Populated on player login and logoff.
     */
    private Set<EntityPlayerMP> importantColonyPlayers = new HashSet<>();

    /**
     * New subscribers which havent received a view yet.
     */
    private Set<EntityPlayerMP> newSubscribers = new HashSet<>();

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
    public Set<EntityPlayerMP> getCloseSubscribers()
    {
        return closeSubscribers;
    }

    @Override
    public void updateSubscribers()
    {
        final World world = colony.getWorld();
        // If the world or server is null, don't try to update the closeSubscribers this tick.
        if (world == null || world.getMinecraftServer() == null)
        {
            return;
        }

        updateColonyViews();
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
            ticksPassed += UPDATE_SUBSCRIBERS_INTERVAL;
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
        if (!closeSubscribers.isEmpty())
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
            colony.getBuildingManager().sendPackets(closeSubscribers, newSubscribers);

            sendSchematicsPackets();
        }

        isDirty = false;
        colony.getPermissions().clearDirty();
        colony.getBuildingManager().clearDirty();
        colony.getCitizenManager().clearDirty();
        newSubscribers = new HashSet<>();
    }

    @Override
    public void sendColonyViewPackets()
    {
        if (isDirty || !newSubscribers.isEmpty())
        {
            final ByteBuf colonyByteBuf = Unpooled.buffer();
            ColonyView.serializeNetworkData(colony, colonyByteBuf, !newSubscribers.isEmpty());
            final Set<EntityPlayerMP> players = isDirty ? closeSubscribers : newSubscribers;
            players.forEach(player -> MineColonies.getNetwork().sendTo(new ColonyViewMessage(colony, colonyByteBuf, newSubscribers.contains(player)), player));
        }
        colony.getRequestManager().setDirty(false);
    }

    @Override
    public void sendPermissionsPackets()
    {
        final Permissions permissions = colony.getPermissions();
        if (permissions.isDirty() || !newSubscribers.isEmpty())
        {
            final Set<EntityPlayerMP> players = permissions.isDirty() ? closeSubscribers : newSubscribers;
            players.forEach(player -> MineColonies.getNetwork().sendTo(new PermissionsMessage.View(colony, permissions.getRank(player)), player));
        }
    }

    @Override
    public void sendWorkOrderPackets()
    {
        final IWorkManager workManager = colony.getWorkManager();
        if (workManager.isDirty() || !newSubscribers.isEmpty())
        {
            final Set<EntityPlayerMP> players = workManager.isDirty() ? closeSubscribers : newSubscribers;
            for (final IWorkOrder workOrder : workManager.getWorkOrders().values())
            {
                if (!(workOrder instanceof WorkOrderBuildMiner))
                {
                    ColonyUtils.sendToAll(players, new ColonyViewWorkOrderMessage(colony, workOrder));
                }
            }
            workManager.setDirty(false);
        }
    }

    @Override
    public void sendSchematicsPackets()
    {
        if (Structures.isDirty() || !newSubscribers.isEmpty())
        {
            final Set<EntityPlayerMP> players = Structures.isDirty() ? closeSubscribers : newSubscribers;
            ColonyUtils.sendToAll(players, new ColonyStylesMessage());
        }
        Structures.clearDirty();
    }

    @Override
    public void setDirty()
    {
        this.isDirty = true;
    }

    @Override
    public void addCloseSubscriber(@NotNull final EntityPlayerMP subscriber)
    {
        if (!closeSubscribers.contains(subscriber))
        {
            closeSubscribers.add(subscriber);
            newSubscribers.add(subscriber);
            // Send view right away upon subscriber add.
            updateSubscribers();
        }
    }

    @Override
    public void removeCloseSubscriber(@NotNull final EntityPlayerMP player)
    {
        newSubscribers.remove(player);
        closeSubscribers.remove(player);
    }

    /**
     * On login we're adding global subscribers.
     */
    @Override
    public void addImportantColonyPlayer(@NotNull final EntityPlayerMP subscriber)
    {
        importantColonyPlayers.add(subscriber);
    }

    /**
     * On logoff we're removing global subscribers.
     */
    @Override
    public void removeImportantColonyPlayer(@NotNull final EntityPlayerMP subscriber)
    {
        importantColonyPlayers.remove(subscriber);
    }

    /**
     * Returns the list of online global subscribers of the colony.
     */
    @Override
    public Set<EntityPlayerMP> getImportantColonyPlayers()
    {
        return importantColonyPlayers;
    }
}
