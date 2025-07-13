package com.minecolonies.core.colony.managers;

import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.connections.ColonyConnectionNode;
import com.minecolonies.api.colony.connections.ConnectedColonyData;
import com.minecolonies.api.colony.connections.IColonyConnectionManager;
import com.minecolonies.api.colony.connections.PendingConnectionNode;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.core.entity.pathfinding.Pathfinding;
import com.minecolonies.core.entity.pathfinding.pathjobs.PathJobSignConnection;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

public class ColonyConnectionManager implements IColonyConnectionManager
{
    /**
     * All points. This is stored to nbt.
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
    private final Int2ObjectAVLTreeMap<ConnectedColonyData> directlyConnectedColonies = new Int2ObjectAVLTreeMap<>();

    /**
     * Cached connection data.
     */
    private final Int2ObjectAVLTreeMap<ConnectedColonyData> indirectlyConnectedColoniesCache = new Int2ObjectAVLTreeMap<>();

    /**
     * Connection events affecting this colony.
     */
    private final List<ConnectionEventData> connectionEvents = new ArrayList<>();

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

    @Override
    public boolean addNewConnectionNode(final BlockPos connectionPoint)
    {
        int distance = Integer.MAX_VALUE;
        ColonyConnectionNode potentialConnection = null;
        for (final ColonyConnectionNode node : colonyConnections.values())
        {
            // Only connect to a node with correct distance.
            if (node.canConnect())
            {
                final int localDistance = (int) node.getPosition().distSqr(connectionPoint);
                if (localDistance <= 50*50 && localDistance < distance)
                {
                    distance = localDistance;
                    potentialConnection = node;
                }
            }
        }

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

            final PendingConnectionNode newNode = new PendingConnectionNode(connectionPoint, createSignPath(connectionPoint, potentialConnection.getPosition()), false);
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
                final PendingConnectionNode newNode = new PendingConnectionNode(connectionPoint, createSignPath(connectionPoint, gateHousePos), false);
                newNode.alterPreviousNode(gateHousePos);

                pendingColonyConnections.put(connectionPoint, newNode);
                return true;
            }
        }

        MessageUtils.format(COM_MINECOLONIES_SIGN_TOO_FAR).withPriority(MessageUtils.MessagePriority.DANGER).sendTo(colony).forManagers();;
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
    }

    @Override
    public boolean attemptEstablishConnection(final BlockPos clickedPos, final IColony targetColony)
    {
        BlockPos tempNodePos = null;
        // Find a suitable connection point.
        for (final ColonyConnectionNode node : colonyConnections.values())
        {
            // Only connect to a node with correct distance.
            if (node.canConnect())
            {
                if (node.getPosition().distSqr(clickedPos) <= 50*50)
                {
                    tempNodePos = node.getPosition();
                    break;
                }
            }
        }

        if (tempNodePos == null)
        {
            MessageUtils.format(COM_MINECOLONIES_SIGN_TOO_FAR).sendTo(this.colony).forManagers();;
            return false;
        }

        // Make sure we're connected until the gate.
        final BlockPos intermediateNodePos = tempNodePos;
        Set<BlockPos> visitedNodes = new HashSet<>();
        while (colonyConnections.containsKey(tempNodePos) && !visitedNodes.contains(tempNodePos))
        {
            tempNodePos = colonyConnections.get(tempNodePos).getPreviousNode();
            visitedNodes.add(tempNodePos);
        }

        if (tempNodePos == null && !gateHouses.contains(tempNodePos))
        {
            MessageUtils.format(Component.translatable(COM_MINECOLONIES_CONNECTION_FAIL)).sendTo(this.colony).forManagers();;
            return false;
        }

        final ColonyConnectionManager targetManager = (ColonyConnectionManager) targetColony.getConnectionManager();
        final ColonyConnectionNode targetNode = targetManager.colonyConnections.get(clickedPos);
        if (!targetNode.canConnect())
        {
            MessageUtils.format(Component.translatable(COM_MINECOLONIES_CONNECTION_FAIL)).sendTo(this.colony).forManagers();
            return false;
        }

        // Make sure the target colony is also connected until the gate.
        BlockPos targetTempNodePos = targetNode.getPreviousNode();
        while (targetManager.colonyConnections.containsKey(targetTempNodePos))
        {
            targetTempNodePos = targetManager.colonyConnections.get(targetTempNodePos).getPreviousNode();
        }

        if (targetTempNodePos == null && !targetManager.gateHouses.contains(targetTempNodePos))
        {
            MessageUtils.format(Component.translatable(COM_MINECOLONIES_CONNECTION_FAIL)).sendTo(this.colony).forManagers();;
            return false;
        }

        // Set gate houses as connected.
        directlyConnectedColonies.put(targetColony.getID(), new ConnectedColonyData(targetColony.getID(), targetColony.getName(), targetTempNodePos, DiplomacyStatus.NEUTRAL));
        targetManager.directlyConnectedColonies.put(colony.getID(), new ConnectedColonyData(colony.getID(), colony.getName(), tempNodePos, DiplomacyStatus.NEUTRAL));

        // Connect the two middle nodes.
        final ColonyConnectionNode intermediateNode = colonyConnections.get(intermediateNodePos);
        intermediateNode.alterNextNode(clickedPos);
        intermediateNode.setTargetColonyId(targetColony.getID());
        targetNode.alterNextNode(intermediateNodePos);
        targetNode.setTargetColonyId(colony.getID());

        tempNodePos = intermediateNodePos;
        while (colonyConnections.containsKey(tempNodePos))
        {
            final ColonyConnectionNode node = colonyConnections.get(tempNodePos);
            node.setTargetColonyId(targetColony.getID());
            tempNodePos = node.getPreviousNode();
        }

        targetTempNodePos = targetNode.getPreviousNode();
        while (targetManager.colonyConnections.containsKey(targetTempNodePos))
        {
            final ColonyConnectionNode node = targetManager.colonyConnections.get(targetTempNodePos);
            node.setTargetColonyId(colony.getID());
            targetTempNodePos = node.getPreviousNode();
        }

        MessageUtils.format(COM_MINECOLONIES_CONNECTION_SUCCESS, colony.getName(), targetColony.getName()).sendTo(this.colony).forManagers();;
        MessageUtils.format(COM_MINECOLONIES_CONNECTION_SUCCESS, targetColony.getName(), colony.getName()).sendTo(targetColony).forManagers();;

        // todo Set connection as pending for pathfinding.
        colony.markDirty();
        return true;
    }

    @Override
    public void tick()
    {
        for (Map.Entry<BlockPos, PendingConnectionNode> pendingConnection : new ArrayList<>(pendingColonyConnections.entrySet()))
        {
            if (pendingConnection.getValue().getCachedPathResult() == null)
            {
                pendingConnection.getValue().setCachedPathResult(createSignPath(pendingConnection.getValue().getPosition(), pendingConnection.getValue().getPreviousNode()));
            }
            else if (pendingConnection.getValue().getCachedPathResult().isDone())
            {
                if (pendingConnection.getValue().getCachedPathResult().isPathReachingDestination())
                {
                    pendingColonyConnections.remove(pendingConnection.getKey());
                    final ColonyConnectionNode connection = colonyConnections.get(pendingConnection.getValue().getPreviousNode());
                    if (connection == null && !gateHouses.contains(pendingConnection.getValue().getPreviousNode()))
                    {
                        colony.getWorld().destroyBlock(pendingConnection.getKey(), true);
                        MessageUtils.format(COM_MINECOLONIES_CONNECTION_PATH_FAILURE, pendingConnection.getKey().toShortString(), pendingConnection.getValue().getPreviousNode().toShortString()).withPriority(MessageUtils.MessagePriority.DANGER).sendTo(colony).forManagers();
                        continue;
                    }
                    MessageUtils.format(COM_MINECOLONIES_SIGN_CONNECTED, pendingConnection.getValue().getPreviousNode().toShortString()).withPriority(MessageUtils.MessagePriority.IMPORTANT).sendTo(colony).forManagers();

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
                                        new ConnectedColonyData(colony.getID(),
                                            colony.getName(),
                                            pendingConnection.getKey(),
                                            directlyConnectedColonies.get(targetColonyId).diplomacyStatus));
                                }
                            }
                        }
                    }
                    else
                    {
                        colonyConnections.put(pendingConnection.getKey(), pendingConnection.getValue());
                    }

                    if (!pendingConnection.getValue().isPathMending())
                    {
                        // After successful connection try to find a next connection to (for repair inbetween).
                        int distance = Integer.MAX_VALUE;
                        ColonyConnectionNode potentialConnection = null;
                        for (final ColonyConnectionNode node : colonyConnections.values())
                        {
                            // Only connect to a node with correct distance.
                            if (node.getPreviousNode() == BlockPos.ZERO)
                            {
                                final int localDistance = (int) node.getPosition().distSqr(pendingConnection.getKey());
                                if (localDistance <= 50 * 50 && localDistance < distance)
                                {
                                    distance = localDistance;
                                    potentialConnection = node;
                                }
                            }
                        }
                        if (potentialConnection != null)
                        {
                            final PendingConnectionNode newNode = new PendingConnectionNode(potentialConnection.getPosition(), createSignPath(potentialConnection.getPosition(), pendingConnection.getKey()), true);
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
                    if (pendingConnection.getValue().isPathMending())
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
    private void updateConnectedColonies(final Int2ObjectMap<ConnectedColonyData> connectedColonies)
    {
        // Update name in cache.
        for (final ConnectedColonyData colonyEntry : new ArrayList<>(connectedColonies.values()))
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
                    new ConnectedColonyData(connectedColony.getID(), connectedColony.getName(), colonyEntry.pos, colonyEntry.diplomacyStatus));
            }

            if (colonyEntry.diplomacyStatus == DiplomacyStatus.ALLIES)
            {
                for (final ConnectedColonyData indirectConnectedColony : connectedColony.getConnectionManager().getDirectlyConnectedColonies().values())
                {
                    indirectlyConnectedColoniesCache.put(indirectConnectedColony.id, indirectConnectedColony);
                }
            }
        }
    }

    @Override
    public Int2ObjectMap<ConnectedColonyData> getDirectlyConnectedColonies()
    {
        return directlyConnectedColonies;
    }

    @Override
    public Int2ObjectMap<ConnectedColonyData> getIndirectlyConnectedColonies()
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
        gateHouses.add(gateHouseConnectionNode);
        for (final ColonyConnectionNode node : colonyConnections.values())
        {
            // Only connect to a node with correct distance.
            if (node.getPreviousNode().equals(BlockPos.ZERO))
            {
                if (node.getPosition().distSqr(gateHouseConnectionNode) <= 50*50)
                {
                    final PendingConnectionNode newNode = new PendingConnectionNode(gateHouseConnectionNode, createSignPath(gateHouseConnectionNode, node.getPosition()), true);
                    newNode.setTargetColonyId(node.getTargetColonyId());
                    newNode.alterNextNode(node.getPosition());
                    pendingColonyConnections.put(newNode.getPosition(), newNode);
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
                MessageUtils.format(COM_MINECOLONIES_SIGN_DISRUPTED, colonyConnectionNode.getPosition()).sendTo(this.colony).forManagers();;
            }
        }

       gateHouses.remove(gateHousePosition);

        // Set connected pos to zero, can't teleport to gatehouse now.
        for (final ConnectedColonyData connectedColonyData : directlyConnectedColonies.values())
        {
            final IColony connectedColony = IColonyManager.getInstance().getColonyByDimension(connectedColonyData.id, colony.getDimension());
            if (connectedColony != null)
            {
                connectedColony.getConnectionManager().getDirectlyConnectedColonies().put(colony.getID(),
                    new ConnectedColonyData(colony.getID(), colony.getName(), BlockPos.ZERO, connectedColonyData.diplomacyStatus));
            }
        }
    }

    @Override
    public void serializeToView(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(directlyConnectedColonies.size());
        for (final Int2ObjectMap.Entry<ConnectedColonyData> connectedColony : directlyConnectedColonies.int2ObjectEntrySet())
        {
            connectedColony.getValue().serializeByteBuf(buf);
        }

        buf.writeInt(indirectlyConnectedColoniesCache.size());
        for (final Int2ObjectMap.Entry<ConnectedColonyData> connectedColony : indirectlyConnectedColoniesCache.int2ObjectEntrySet())
        {
            connectedColony.getValue().serializeByteBuf(buf);
        }

        buf.writeInt(connectionEvents.size());
        for (final ConnectionEventData connectionEventType : connectionEvents)
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
           final ConnectedColonyData connectedColonyData = new ConnectedColonyData().deserializeByteBuf(buf);
           directlyConnectedColonies.put(connectedColonyData.id, connectedColonyData);
       }

        final int indirectConnectionsSize = buf.readInt();
        for (int i = 0; i < indirectConnectionsSize; i++)
        {
            final ConnectedColonyData connectedColonyData = new ConnectedColonyData().deserializeByteBuf(buf);
            indirectlyConnectedColoniesCache.put(connectedColonyData.id, connectedColonyData);
        }

        connectionEvents.clear();
        final int connectionEventSize = buf.readInt();
        for (int i = 0; i < connectionEventSize; i++)
        {
            final ConnectionEventData connectionEventData = ConnectionEventData.deserializeByteBuf(buf);
            connectionEvents.add(connectionEventData);
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
            final ConnectedColonyData colonyConnectionData = new ConnectedColonyData().deserializeNBT((CompoundTag) tag);
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
            connectionEvents.add(ConnectionEventData.deserializeNBT((CompoundTag) tag));
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
        for (final Int2ObjectMap.Entry<ConnectedColonyData> entry : directlyConnectedColonies.int2ObjectEntrySet())
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
        for (final ConnectionEventData connectionEvent : connectionEvents)
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
    public void triggerConnectionEvent(final ConnectionEventData connectionEventData)
    {
        final IColony originColony = IColonyManager.getInstance().getColonyByDimension(connectionEventData.id(), colony.getDimension());
        if (originColony == null)
        {
            return;
        }

        connectionEvents.add(connectionEventData);
        final ConnectedColonyData connectedColonyData;
        final Int2ObjectMap<ConnectedColonyData> affectedMap;
        if (directlyConnectedColonies.containsKey(connectionEventData.id()))
        {
            connectedColonyData = directlyConnectedColonies.get(connectionEventData.id());
            affectedMap = directlyConnectedColonies;
        }
        else if (indirectlyConnectedColoniesCache.containsKey(connectionEventData.id()))
        {
            connectedColonyData = indirectlyConnectedColoniesCache.get(connectionEventData.id());
            affectedMap = indirectlyConnectedColoniesCache;
        }
        else
        {
            return;
        }

        affectedMap.put(connectionEventData.id(), new ConnectedColonyData(connectionEventData.id(), originColony.getName(), connectedColonyData.pos, switch (connectionEventData.connectionEventType())
        {
            case ALLY_CONFIRMED -> DiplomacyStatus.ALLIES;
            case FEUD_STARTED -> DiplomacyStatus.HOSTILE;
            case NEUTRAL_SET -> DiplomacyStatus.NEUTRAL;
            default -> connectedColonyData.diplomacyStatus;
        }));

        final ConnectedColonyData originConnectedColonyData;
        final Int2ObjectMap<ConnectedColonyData> originAffectedMap;
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

        originAffectedMap.put(connectionEventData.id(), new ConnectedColonyData(colony.getID(), colony.getName(), originConnectedColonyData.pos, switch (connectionEventData.connectionEventType())
        {
            case ALLY_CONFIRMED -> DiplomacyStatus.ALLIES;
            case FEUD_STARTED -> DiplomacyStatus.HOSTILE;
            case NEUTRAL_SET -> DiplomacyStatus.NEUTRAL;
            default -> connectedColonyData.diplomacyStatus;
        }));

        colony.markDirty();
    }

    //todo: pathfinding

    @Override
    public List<ConnectionEventData> getConnectionEvents()
    {
        return connectionEvents;
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
