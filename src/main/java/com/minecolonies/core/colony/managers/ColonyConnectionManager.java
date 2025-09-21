package com.minecolonies.core.colony.managers;

import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.connections.*;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.core.entity.pathfinding.Pathfinding;
import com.minecolonies.core.entity.pathfinding.pathjobs.PathJobSignConnection;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.colony.connections.ConnectionEventType.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

public class ColonyConnectionManager implements IColonyConnectionManager
{
    /**
     * There are the individual connection points. The position represents the position a sign is at.
     */
    private final Map<BlockPos, ColonyConnectionNode> colonyConnections = new LinkedHashMap<>();

    /**
     * List of gate house positions.
     */
    private final List<BlockPos> gateHouses = new ArrayList<>();

    /**
     * Connected colony.
     */
    private final IColony colony;

    /**
     * Connected colonies mapped to their gate position.
     */
    private final TreeMap<Integer, ColonyConnection> directlyConnectedColonies = new TreeMap<>();

    /**
     * Cached connection data.
     */
    private final TreeMap<Integer, ColonyConnection> indirectlyConnectedColoniesCache = new TreeMap<>();

    /**
     * Connection events affecting this colony. From colony id invoking the event, to event data.
     */
    private final Map<Integer, ConnectionEvent> connectionEvents = new TreeMap<>();

    /**
     * Pending connection points. This is stored to nbt.
     */
    private final Map<BlockPos, PendingConnectionNode> pendingColonyConnections = new LinkedHashMap<>();

    /**
     * Create a new connection manager.
     * @param colony its colony.
     */
    public ColonyConnectionManager(final IColony colony)
    {
        this.colony = colony;
    }

    /**
     * Get the closest node with an open nextNode connection.
     * @param pos the pos the connection point is at.
     * @return a possible node or null.
     */
    @Nullable
    private ColonyConnectionNode getClosestNodeWithOpenConnection(final BlockPos pos)
    {
        int distance = Integer.MAX_VALUE;
        ColonyConnectionNode potentialConnection = null;
        for (final ColonyConnectionNode node : colonyConnections.values())
        {
            // Only connect to a node with correct distance.
            if (!node.hasNextNode())
            {
                final int localDistance = (int) node.getPosition().distSqr(pos);
                if (localDistance <= 50*50 && localDistance < distance)
                {
                    distance = localDistance;
                    potentialConnection = node;
                }
            }
        }
        return potentialConnection;
    }

    @Override
    public boolean addNewConnectionNode(final BlockPos connectionPoint)
    {
        final ColonyConnectionNode potentialConnection = getClosestNodeWithOpenConnection(connectionPoint);
        if (potentialConnection != null)
        {
            Set<BlockPos> visitedNodes = new HashSet<>();
            BlockPos tempNode = potentialConnection.getPreviousNode();
            while (colonyConnections.containsKey(tempNode) && !visitedNodes.contains(tempNode))
            {
                tempNode = colonyConnections.get(tempNode).getPreviousNode();
                visitedNodes.add(tempNode);
            }

            if (tempNode == null && !gateHouses.contains(tempNode))
            {
                MessageUtils.format(COM_MINECOLONIES_SIGN_MISSING_LINK).withPriority(MessageUtils.MessagePriority.DANGER).sendTo(colony).forManagers();
                return false;
            }

            final PendingConnectionNode newNode = new PendingConnectionNode(connectionPoint, createSignPath(connectionPoint, potentialConnection.getPosition()), PendingConnectionNode.PendingConnectionType.DEFAULT);
            newNode.alterPreviousNode(potentialConnection.getPosition());
            if (potentialConnection.getTargetColonyId() != -1)
            {
                newNode.setTargetColonyId(potentialConnection.getTargetColonyId());
            }

            pendingColonyConnections.put(connectionPoint, newNode);
            return true;
        }

        for (final BlockPos gateHousePos : gateHouses)
        {
            if (gateHousePos.distSqr(connectionPoint) <= 50*50)
            {
                final PendingConnectionNode newNode = new PendingConnectionNode(connectionPoint, createSignPath(connectionPoint, gateHousePos), PendingConnectionNode.PendingConnectionType.DEFAULT);
                newNode.alterPreviousNode(gateHousePos);

                pendingColonyConnections.put(connectionPoint, newNode);
                return true;
            }
        }

        MessageUtils.format(COM_MINECOLONIES_SIGN_TOO_FAR).withPriority(MessageUtils.MessagePriority.DANGER).sendTo(colony).forManagers();
        return false;
    }

    @Override
    public void removeConnectionNode(final BlockPos connectionPoint)
    {
        final ColonyConnectionNode colonyConnectionNode = colonyConnections.remove(connectionPoint);
        if (colonyConnectionNode != null)
        {
            final ColonyConnectionNode previousNode = colonyConnections.get(colonyConnectionNode.getPreviousNode());
            if (previousNode != null)
            {
                previousNode.alterNextNode(BlockPos.ZERO);
                MessageUtils.format(Component.translatable(COM_MINECOLONIES_SIGN_DISRUPTED, previousNode.getPosition())).sendTo(this.colony).forManagers();
            }
            final ColonyConnectionNode nextNode = colonyConnections.get(colonyConnectionNode.getNextNode());
            if (nextNode != null)
            {
                nextNode.alterPreviousNode(BlockPos.ZERO);
                MessageUtils.format(Component.translatable(COM_MINECOLONIES_SIGN_DISRUPTED, nextNode.getPosition())).sendTo(this.colony).forManagers();
            }
        }
        pendingColonyConnections.remove(connectionPoint);
    }

    @Override
    public boolean attemptEstablishConnection(final BlockPos targetColonyConnectionPos, final IColony targetColony)
    {
        final ColonyConnectionNode thisColonyConnectionPos = getClosestNodeWithOpenConnection(targetColonyConnectionPos);
        if (thisColonyConnectionPos == null)
        {
            MessageUtils.format(COM_MINECOLONIES_SIGN_TOO_FAR).sendTo(this.colony).forManagers();
            return false;
        }

        final PendingConnectionNode newNode = new PendingConnectionNode(thisColonyConnectionPos.getPosition(), createSignPath(thisColonyConnectionPos.getPosition(), targetColonyConnectionPos), PendingConnectionNode.PendingConnectionType.CONNECT_COLONY);
        newNode.alterPreviousNode(targetColonyConnectionPos);
        newNode.setTargetColonyId(targetColony.getID());

        pendingColonyConnections.put(thisColonyConnectionPos.getPosition(), newNode);
        return true;
    }

    @Override
    public void tick()
    {
        for (Map.Entry<BlockPos, PendingConnectionNode> pendingConnection : new ArrayList<>(pendingColonyConnections.entrySet()))
        {
            if (pendingConnection.getValue().getCachedPathResult() == null)
            {
                if (WorldUtil.isBlockLoaded(colony.getWorld(), pendingConnection.getKey()))
                {
                    pendingConnection.getValue().setCachedPathResult(createSignPath(pendingConnection.getValue().getPosition(), pendingConnection.getValue().getPreviousNode()));
                }
            }
            else if (pendingConnection.getValue().getCachedPathResult().isDone())
            {
                if (pendingConnection.getValue().getCachedPathResult().isPathReachingDestination())
                {
                    if (pendingColonyConnections.remove(pendingConnection.getKey()) == null)
                    {
                        return;
                    }
                    final ColonyConnectionNode connection = colonyConnections.get(pendingConnection.getValue().getPreviousNode());
                    if (pendingConnection.getValue().getPendingConnectionType() == PendingConnectionNode.PendingConnectionType.DEFAULT)
                    {
                        if (connection == null && !gateHouses.contains(pendingConnection.getValue().getPreviousNode()))
                        {
                            colony.getWorld().destroyBlock(pendingConnection.getKey(), true);
                            MessageUtils.format(COM_MINECOLONIES_CONNECTION_PATH_FAILURE,
                                pendingConnection.getKey().toShortString(),
                                pendingConnection.getValue().getPreviousNode().toShortString()).withPriority(MessageUtils.MessagePriority.DANGER).sendTo(colony).forManagers();
                            continue;
                        }
                        colonyConnections.put(pendingConnection.getKey(), pendingConnection.getValue());
                    }

                    MessageUtils.format(COM_MINECOLONIES_SIGN_CONNECTED, pendingConnection.getValue().getPosition(), pendingConnection.getValue().getPreviousNode().toShortString())
                        .withPriority(MessageUtils.MessagePriority.IMPORTANT)
                        .sendTo(colony)
                        .forManagers();

                    if (connection != null)
                    {
                        connection.alterNextNode(pendingConnection.getValue().getPosition());
                    }

                    if (gateHouses.contains(pendingConnection.getKey()))
                    {
                        final ColonyConnectionNode nextNode = colonyConnections.get(pendingConnection.getValue().getNextNode());
                        if (nextNode != null)
                        {
                            nextNode.alterPreviousNode(pendingConnection.getKey());
                            final int targetColonyId = pendingConnection.getValue().getTargetColonyId();
                            if (targetColonyId != -1)
                            {
                                final IColony connectedColony = IColonyManager.getInstance().getColonyByDimension(targetColonyId, colony.getDimension());
                                if (connectedColony != null)
                                {
                                    connectedColony.getConnectionManager().getDirectlyConnectedColonies().put(colony.getID(),
                                        new ColonyConnection(colony.getID(),
                                            colony.getName(),
                                            pendingConnection.getKey(),
                                            directlyConnectedColonies.get(targetColonyId).diplomacyStatus));
                                }
                            }
                        }
                    }

                    if (pendingConnection.getValue().getPendingConnectionType() == PendingConnectionNode.PendingConnectionType.CONNECT_COLONY)
                    {
                        connectToColony(pendingConnection.getKey(), pendingConnection.getValue().getTargetColonyId(), pendingConnection.getValue().getPreviousNode());
                    }
                    else if (pendingConnection.getValue().getPendingConnectionType() == PendingConnectionNode.PendingConnectionType.DEFAULT)
                    {
                        // After successful connection try to find a next connection to (for repair inbetween).
                        int distance = Integer.MAX_VALUE;
                        ColonyConnectionNode potentialConnection = null;
                        for (final ColonyConnectionNode node : colonyConnections.values())
                        {
                            // Only connect to a node with correct distance.
                            if (node.getPreviousNode().equals(BlockPos.ZERO) && !node.getPosition().equals(pendingConnection.getKey()))
                            {
                                final int localDistance = (int) node.getPosition().distSqr(pendingConnection.getKey());
                                if (localDistance <= 50 * 50 && localDistance < distance && !node.getPosition().equals(pendingConnection.getKey()))
                                {
                                    distance = localDistance;
                                    potentialConnection = node;
                                }
                            }
                        }
                        if (potentialConnection != null)
                        {
                            final PendingConnectionNode newNode = new PendingConnectionNode(potentialConnection.getPosition(), createSignPath(potentialConnection.getPosition(), pendingConnection.getKey()), PendingConnectionNode.PendingConnectionType.FIX_PATH);
                            newNode.alterPreviousNode(pendingConnection.getKey());
                            newNode.alterNextNode(potentialConnection.getNextNode());
                            if (pendingConnection.getValue().getTargetColonyId() != -1)
                            {
                                newNode.setTargetColonyId(pendingConnection.getValue().getTargetColonyId());
                            }
                            else if (potentialConnection.getTargetColonyId() != -1)
                            {
                                newNode.setTargetColonyId(potentialConnection.getTargetColonyId());
                            }

                            pendingColonyConnections.put(newNode.getPosition(), newNode);
                        }
                    }
                }
                else
                {
                    if (pendingConnection.getValue().getPendingConnectionType() != PendingConnectionNode.PendingConnectionType.DEFAULT)
                    {
                        continue;
                    }
                    colony.getWorld().destroyBlock(pendingConnection.getKey(), true);
                    pendingColonyConnections.remove(pendingConnection.getKey());
                    MessageUtils.format(COM_MINECOLONIES_CONNECTION_PATH_FAILURE, pendingConnection.getKey().toShortString(), pendingConnection.getValue().getPreviousNode().toShortString()).withPriority(MessageUtils.MessagePriority.DANGER).sendTo(colony).forManagers();
                }
            }
        }

        // Update connections.
        updateConnectedColonies(directlyConnectedColonies);
        updateConnectedColonies(indirectlyConnectedColoniesCache);
    }

    /**
     * Handle the connection between two colonies. Make sure both gates are reachable.
     * @param thisColonyConnectionPos the connection pos in this colony.
     * @param targetColonyId the target colony id.
     * @param targetColonyConnectionPos the connection pos in the target colony.
     */
    private void connectToColony(final BlockPos thisColonyConnectionPos, final int targetColonyId, final BlockPos targetColonyConnectionPos)
    {
        final IColony targetColony = IColonyManager.getInstance().getColonyByDimension(targetColonyId, colony.getDimension());
        if (targetColony == null)
        {
            MessageUtils.format(Component.translatable(COM_MINECOLONIES_CONNECTION_FAIL)).sendTo(this.colony).forManagers();
            return;
        }
        // Make sure we're connected until the gate.
        BlockPos thisColonyGatePos = thisColonyConnectionPos;
        Set<BlockPos> visitedNodes = new HashSet<>();
        while (colonyConnections.containsKey(thisColonyGatePos))
        {
            thisColonyGatePos = colonyConnections.get(thisColonyGatePos).getPreviousNode();
            if (!visitedNodes.add(thisColonyGatePos))
            {
                break;
            }
        }

        if (thisColonyGatePos == null || !gateHouses.contains(thisColonyGatePos))
        {
            MessageUtils.format(Component.translatable(COM_MINECOLONIES_CONNECTION_FAIL)).sendTo(this.colony).forManagers();
            return;
        }

        final ColonyConnectionManager targetManager = (ColonyConnectionManager) targetColony.getConnectionManager();
        final ColonyConnectionNode targetNode = targetManager.colonyConnections.get(targetColonyConnectionPos);
        if ((targetNode != null && targetNode.hasNextNode()) && !targetManager.gateHouses.contains(targetColonyConnectionPos))
        {
            MessageUtils.format(Component.translatable(COM_MINECOLONIES_CONNECTION_FAIL)).sendTo(this.colony).forManagers();
            return;
        }

        // Make sure the target colony is also connected until the gate.
        BlockPos targetColonyGatePos = targetNode == null ? targetColonyConnectionPos : targetNode.getPreviousNode();
        visitedNodes = new HashSet<>();
        while (targetManager.colonyConnections.containsKey(targetColonyGatePos))
        {
            targetColonyGatePos = targetManager.colonyConnections.get(targetColonyGatePos).getPreviousNode();
            if (!visitedNodes.add(targetColonyGatePos))
            {
                break;
            }
        }

        if (targetColonyGatePos == null || !targetManager.gateHouses.contains(targetColonyGatePos))
        {
            MessageUtils.format(Component.translatable(COM_MINECOLONIES_CONNECTION_FAIL)).sendTo(this.colony).forManagers();
            return;
        }

        // Set gate houses as connected.
        directlyConnectedColonies.put(targetColony.getID(), new ColonyConnection(targetColony.getID(), targetColony.getName(), targetColonyGatePos, DiplomacyStatus.NEUTRAL));
        targetManager.directlyConnectedColonies.put(colony.getID(), new ColonyConnection(colony.getID(), colony.getName(), thisColonyGatePos, DiplomacyStatus.NEUTRAL));

        // Connect the two middle nodes.
        final ColonyConnectionNode intermediateNode = colonyConnections.get(thisColonyConnectionPos);
        intermediateNode.alterNextNode(targetColonyConnectionPos);
        intermediateNode.setTargetColonyId(targetColony.getID());

        if (targetNode != null)
        {
            targetNode.alterNextNode(thisColonyConnectionPos);
            targetNode.setTargetColonyId(colony.getID());

            targetColonyGatePos = targetNode.getPreviousNode();
            while (targetManager.colonyConnections.containsKey(targetColonyGatePos))
            {
                final ColonyConnectionNode node = targetManager.colonyConnections.get(targetColonyGatePos);
                node.setTargetColonyId(colony.getID());
                targetColonyGatePos = node.getPreviousNode();
            }
        }

        thisColonyGatePos = thisColonyConnectionPos;
        while (colonyConnections.containsKey(thisColonyGatePos))
        {
            final ColonyConnectionNode node = colonyConnections.get(thisColonyGatePos);
            node.setTargetColonyId(targetColony.getID());
            thisColonyGatePos = node.getPreviousNode();
        }

        MessageUtils.format(COM_MINECOLONIES_CONNECTION_SUCCESS, colony.getName(), targetColony.getName()).sendTo(this.colony).forManagers();
        MessageUtils.format(COM_MINECOLONIES_CONNECTION_SUCCESS, targetColony.getName(), colony.getName()).sendTo(targetColony).forManagers();

        colony.markDirty();
    }

    /**
     * Creates and starts the pathjob towards this spawnpoint
     *
     * @param originPos the origin position.
     * @param targetPos the target position.
     * @return the path result.
     */
    private PathResult createSignPath(final BlockPos originPos, final BlockPos targetPos)
    {
        final PathJobSignConnection job = new PathJobSignConnection(colony.getWorld(), originPos, targetPos, 16);
        job.getResult().startJob(Pathfinding.getExecutor());
        return job.getResult();
    }

    /**
     * Go through connected colonies and check for potential neighbors and update name, or remove if necessary.
     * @param connectedColonies the list of connected colonies to process.
     */
    private void updateConnectedColonies(final TreeMap<Integer, ColonyConnection> connectedColonies)
    {
        // Update name in cache.
        for (final ColonyConnection colonyEntry : new ArrayList<>(connectedColonies.values()))
        {
            final IColony connectedColony = IColonyManager.getInstance().getColonyByDimension(colonyEntry.id, colony.getDimension());
            if (connectedColony == null)
            {
                connectedColonies.remove(colonyEntry.id);
                continue;
            }

            if (!connectedColony.getName().equals(colonyEntry.name))
            {
                connectedColonies.put(colonyEntry.id,
                    new ColonyConnection(connectedColony.getID(), connectedColony.getName(), colonyEntry.pos, colonyEntry.diplomacyStatus));
            }

            if (colonyEntry.diplomacyStatus == DiplomacyStatus.ALLIES)
            {
                for (final ColonyConnection indirectConnectedColony : connectedColony.getConnectionManager().getDirectlyConnectedColonies().values())
                {
                    if (!directlyConnectedColonies.containsKey(indirectConnectedColony.id) && indirectConnectedColony.id != colony.getID())
                    {
                        indirectlyConnectedColoniesCache.put(indirectConnectedColony.id, indirectConnectedColony);
                    }
                }
            }
        }
    }

    @Override
    public TreeMap<Integer, ColonyConnection> getDirectlyConnectedColonies()
    {
        return directlyConnectedColonies;
    }

    @Override
    public TreeMap<Integer, ColonyConnection> getIndirectlyConnectedColonies()
    {
        return indirectlyConnectedColoniesCache;
    }

    @Override
    public ColonyConnectionNode getNode(final BlockPos blockPos)
    {
        return colonyConnections.get(blockPos);
    }

    @Override
    public void addNewGateHouse(final BlockPos gateHouseConnectionNode)
    {
        if (!gateHouses.contains(gateHouseConnectionNode))
        {
            gateHouses.add(gateHouseConnectionNode);
            for (final ColonyConnectionNode node : colonyConnections.values())
            {
                // Only connect to a node with correct distance.
                if (node.getPreviousNode().equals(BlockPos.ZERO))
                {
                    if (node.getPosition().distSqr(gateHouseConnectionNode) <= 50 * 50)
                    {
                        final PendingConnectionNode newNode = new PendingConnectionNode(gateHouseConnectionNode,
                            createSignPath(gateHouseConnectionNode, node.getPosition()),
                            PendingConnectionNode.PendingConnectionType.FIX_PATH);
                        newNode.setTargetColonyId(node.getTargetColonyId());
                        newNode.alterNextNode(node.getPosition());
                        pendingColonyConnections.put(newNode.getPosition(), newNode);
                    }
                }
            }
        }
    }

    @Override
    public void removeGateHouse(final BlockPos gateHousePosition)
    {
        for (final ColonyConnectionNode colonyConnectionNode : colonyConnections.values())
        {
            if (colonyConnectionNode.getPreviousNode().equals(gateHousePosition))
            {
                colonyConnectionNode.alterPreviousNode(BlockPos.ZERO);
                MessageUtils.format(COM_MINECOLONIES_SIGN_DISRUPTED, colonyConnectionNode.getPosition()).sendTo(this.colony).forManagers();
            }
        }

       gateHouses.remove(gateHousePosition);

        // Set connected pos to zero, can't teleport to gatehouse now.
        for (final ColonyConnection connectedColonyData : directlyConnectedColonies.values())
        {
            final IColony connectedColony = IColonyManager.getInstance().getColonyByDimension(connectedColonyData.id, colony.getDimension());
            if (connectedColony != null)
            {
                connectedColony.getConnectionManager().getDirectlyConnectedColonies().put(colony.getID(),
                    new ColonyConnection(colony.getID(), colony.getName(), BlockPos.ZERO, connectedColonyData.diplomacyStatus));
            }
        }
    }

    @Override
    public void serializeToView(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(directlyConnectedColonies.size());
        for (final Map.Entry<Integer, ColonyConnection> connectedColony : directlyConnectedColonies.entrySet())
        {
            connectedColony.getValue().serializeByteBuf(buf);
        }

        buf.writeInt(indirectlyConnectedColoniesCache.size());
        for (final Map.Entry<Integer, ColonyConnection> connectedColony : indirectlyConnectedColoniesCache.entrySet())
        {
            connectedColony.getValue().serializeByteBuf(buf);
        }

        buf.writeInt(connectionEvents.size());
        for (final ConnectionEvent connectionEventType : connectionEvents.values())
        {
            connectionEventType.serializeByteBuf(buf);
        }
    }

    @Override
    public void deserializeFromView(@NotNull final FriendlyByteBuf buf)
    {
       final int directConnectionsSize = buf.readInt();
       for (int i = 0; i < directConnectionsSize; i++)
       {
           final ColonyConnection connectedColonyData = new ColonyConnection().deserializeByteBuf(buf);
           directlyConnectedColonies.put(connectedColonyData.id, connectedColonyData);
       }

        final int indirectConnectionsSize = buf.readInt();
        for (int i = 0; i < indirectConnectionsSize; i++)
        {
            final ColonyConnection connectedColonyData = new ColonyConnection().deserializeByteBuf(buf);
            indirectlyConnectedColoniesCache.put(connectedColonyData.id, connectedColonyData);
        }

        connectionEvents.clear();
        final int connectionEventSize = buf.readInt();
        for (int i = 0; i < connectionEventSize; i++)
        {
            final ConnectionEvent connectionEventData = ConnectionEvent.deserializeByteBuf(buf);
            connectionEvents.put(connectionEventData.id(), connectionEventData);
        }
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        final ListTag connectionTagList = compound.getList(TAG_CONNECTIONS, Tag.TAG_COMPOUND);
        for (final Tag tag : connectionTagList)
        {
            final BlockPos pos = BlockPosUtil.read((CompoundTag) tag, TAG_POS);
            final ColonyConnectionNode connectionPoint = new ColonyConnectionNode(pos);
            connectionPoint.read((CompoundTag) tag);
            colonyConnections.put(pos, connectionPoint);
        }

        final ListTag connectedColonyTagList = compound.getList(TAG_COLONIES, Tag.TAG_COMPOUND);
        for (final Tag tag : connectedColonyTagList)
        {
            final ColonyConnection colonyConnectionData = new ColonyConnection().deserializeNBT((CompoundTag) tag);
            directlyConnectedColonies.put(colonyConnectionData.id, colonyConnectionData);
        }

        gateHouses.clear();
        final ListTag gateHouseTagList = compound.getList(TAG_GATEHOUSES, Tag.TAG_COMPOUND);
        for (final Tag tag : gateHouseTagList)
        {
            gateHouses.add(BlockPosUtil.read((CompoundTag) tag, TAG_POS));
        }

        connectionEvents.clear();
        final ListTag connectionEventList = compound.getList(TAG_CONNECTION_EVENTS, Tag.TAG_COMPOUND);
        for (final Tag tag : connectionEventList)
        {
            final ConnectionEvent connectionEventData = ConnectionEvent.deserializeNBT((CompoundTag) tag);
            connectionEvents.put(connectionEventData.id(), connectionEventData);
        }

        final ListTag pendingConnectionTagList = compound.getList(TAG_PENDING, Tag.TAG_COMPOUND);
        for (final Tag tag : pendingConnectionTagList)
        {
            final BlockPos pos = BlockPosUtil.read((CompoundTag) tag, TAG_POS);
            final PendingConnectionNode colonyConnectionData = new PendingConnectionNode(pos);
            colonyConnectionData.read((CompoundTag) tag);
            pendingColonyConnections.put(pos, colonyConnectionData);
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compoundTag = new CompoundTag();
        @NotNull final ListTag connectionTagList = new ListTag();
        for (@NotNull final ColonyConnectionNode connectionPoint : colonyConnections.values())
        {
            connectionTagList.add(connectionPoint.write());
        }
        compoundTag.put(TAG_CONNECTIONS, connectionTagList);

        @NotNull final ListTag connectedColonyTagList = new ListTag();
        for (final Map.Entry<Integer, ColonyConnection> entry : directlyConnectedColonies.entrySet())
        {
            connectedColonyTagList.add(entry.getValue().serializeNBT());
        }
        compoundTag.put(TAG_COLONIES, connectedColonyTagList);

        @NotNull final ListTag gateHouseTagList = new ListTag();
        for (final BlockPos gateHouse : gateHouses)
        {
            gateHouseTagList.add(BlockPosUtil.write(new CompoundTag(), TAG_POS, gateHouse));
        }
        compoundTag.put(TAG_GATEHOUSES, gateHouseTagList);

        @NotNull final ListTag connectionEventTagList = new ListTag();
        for (final ConnectionEvent connectionEvent : connectionEvents.values())
        {
            connectionEventTagList.add(connectionEvent.serializeNBT());
        }
        compoundTag.put(TAG_CONNECTION_EVENTS, connectionEventTagList);

        @NotNull final ListTag pendingConnectionTagList = new ListTag();
        for (final PendingConnectionNode connectionEvent : pendingColonyConnections.values())
        {
            pendingConnectionTagList.add(connectionEvent.write());
        }
        compoundTag.put(TAG_PENDING, pendingConnectionTagList);
        return compoundTag;
    }

    @Override
    public void triggerConnectionEvent(final ConnectionEvent connectionEventData)
    {
        final int originColonyId = connectionEventData.id();
        final IColony originColony = IColonyManager.getInstance().getColonyByDimension(originColonyId, colony.getDimension());
        if (originColony == null)
        {
            return;
        }

        connectionEvents.put(connectionEventData.id(), connectionEventData);
        final ColonyConnection connectedColonyData;
        final TreeMap<Integer, ColonyConnection> affectedMap;
        if (directlyConnectedColonies.containsKey(originColonyId))
        {
            connectedColonyData = directlyConnectedColonies.get(originColonyId);
            affectedMap = directlyConnectedColonies;
        }
        else if (indirectlyConnectedColoniesCache.containsKey(originColonyId))
        {
            connectedColonyData = indirectlyConnectedColoniesCache.get(originColonyId);
            affectedMap = indirectlyConnectedColoniesCache;
        }
        else
        {
            return;
        }

        final DiplomacyStatus diplomacyStatus = switch (connectionEventData.connectionEventType())
        {
            case ALLY_CONFIRMED -> DiplomacyStatus.ALLIES;
            case FEUD_STARTED -> DiplomacyStatus.HOSTILE;
            case NEUTRAL_SET -> DiplomacyStatus.NEUTRAL;
            default -> connectedColonyData.diplomacyStatus;
        };

        affectedMap.put(originColonyId, new ColonyConnection(originColonyId, originColony.getName(), connectedColonyData.pos, diplomacyStatus));

        final ColonyConnection originConnectedColonyData;
        final TreeMap<Integer, ColonyConnection> originAffectedMap;
        final IColonyConnectionManager originColonyConnectionManager = originColony.getConnectionManager();
        if (originColonyConnectionManager.getDirectlyConnectedColonies().containsKey(colony.getID()))
        {
            originConnectedColonyData = originColonyConnectionManager.getDirectlyConnectedColonies().get(colony.getID());
            originAffectedMap = originColonyConnectionManager.getDirectlyConnectedColonies();
        }
        else if (originColonyConnectionManager.getIndirectlyConnectedColonies().containsKey(colony.getID()))
        {
            originConnectedColonyData = originColonyConnectionManager.getIndirectlyConnectedColonies().get(colony.getID());
            originAffectedMap = originColonyConnectionManager.getIndirectlyConnectedColonies();
        }
        else
        {
            return;
        }

        originAffectedMap.put(colony.getID(), new ColonyConnection(colony.getID(), colony.getName(), originConnectedColonyData.pos, diplomacyStatus));
        originColony.markDirty();
        colony.markDirty();
    }

    @Override
    public List<ConnectionEvent> getConnectionEvents()
    {
        return new ArrayList<>(connectionEvents.values());
    }

    @Override
    public DiplomacyStatus getColonyDiplomacyStatus(final int id)
    {
        if (directlyConnectedColonies.containsKey(id))
        {
            return directlyConnectedColonies.get(id).diplomacyStatus;
        }
        else if (indirectlyConnectedColoniesCache.containsKey(id))
        {
            return indirectlyConnectedColoniesCache.get(id).diplomacyStatus;
        }
        return DiplomacyStatus.NEUTRAL;
    }
}
