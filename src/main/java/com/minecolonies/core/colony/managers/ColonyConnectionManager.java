package com.minecolonies.core.colony.managers;

import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.managers.interfaces.IColonyConnectionManager;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.core.colony.Colony;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
    private final Colony colony;

    /**
     * Connected colonies mapped to their gate position.
     */
    private final Int2ObjectMap<BlockPos> connectedColonies = new Int2ObjectOpenHashMap<>();

    /**
     * Create a new connection manager.
     * @param colony its colony.
     */
    public ColonyConnectionManager(final Colony colony)
    {
        this.colony = colony;
    }

    @Override
    public boolean addNewConnectionNode(final BlockPos connectionPoint)
    {
        for (final ColonyConnectionNode node : colonyConnections.values())
        {
            // Only connect to a node with correct distance.
            if (node.canConnect())
            {
                if (node.getPosition().distSqr(connectionPoint) <= 50*50)
                {
                    BlockPos tempNode = node.getPreviousNode();
                    while (colonyConnections.containsKey(tempNode))
                    {
                        tempNode = colonyConnections.get(tempNode).getPreviousNode();
                    }

                    if (tempNode == null && !gateHouses.contains(tempNode))
                    {
                        MessageUtils.format(COM_MINECOLONIES_SIGN_MISSING_LINK).sendTo(colony).forManagers();
                        return false;
                    }

                    final ColonyConnectionNode newNode = new ColonyConnectionNode(connectionPoint);
                    node.alterNextNode(connectionPoint);
                    newNode.alterPreviousNode(node.getPosition());
                    colonyConnections.put(connectionPoint, newNode);

                    //todo add to pending queue
                    return true;
                }
            }
        }

        for (final BlockPos node : gateHouses)
        {
            if (node.distSqr(connectionPoint) <= 50*50)
            {
                final ColonyConnectionNode newNode = new ColonyConnectionNode(connectionPoint);
                newNode.alterPreviousNode(node);
                colonyConnections.put(connectionPoint, newNode);

                //todo add to pending queue
                return true;
            }
        }

        MessageUtils.format(COM_MINECOLONIES_SIGN_TOO_FAR).sendTo(colony).forManagers();;
        return false;
    }

    // todo: keep connection even on gate house removal. Connection only removed on player removal in UI.
    @Override
    public void removeConnectionNode(final BlockPos connectionPoint)
    {
        final ColonyConnectionNode colonyConnectionNode = colonyConnections.remove(connectionPoint);
        final ColonyConnectionNode previousNode = colonyConnections.get(colonyConnectionNode.getPreviousNode());
        if (previousNode != null)
        {
            previousNode.alterNextNode(BlockPos.ZERO);
        }
        final ColonyConnectionNode nextNode = colonyConnections.get(colonyConnectionNode.getNextNode());
        if (nextNode != null)
        {
            nextNode.alterPreviousNode(BlockPos.ZERO);
        }
    }

    @Override
    public boolean attemptEstablishConnection(final BlockPos clickedPos, final IColony targetColony)
    {
        BlockPos tempNode = null;
        // Find a suitable connection point.
        for (final ColonyConnectionNode node : colonyConnections.values())
        {
            // Only connect to a node with correct distance.
            if (node.canConnect())
            {
                if (node.getPosition().distSqr(clickedPos) <= 50*50)
                {
                    tempNode = node.getPosition();
                    break;
                }
            }
        }

        if (tempNode == null)
        {
            MessageUtils.format(COM_MINECOLONIES_SIGN_TOO_FAR).sendTo(this.colony).forManagers();;
            return false;
        }

        // Make sure we're connected until the gate.
        final BlockPos intermediateNode = tempNode;
        while (colonyConnections.containsKey(tempNode))
        {
            tempNode = colonyConnections.get(tempNode).getPreviousNode();
        }

        if (tempNode == null && !gateHouses.contains(tempNode))
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
        BlockPos targetTempNode = targetNode.getPreviousNode();
        while (targetManager.colonyConnections.containsKey(targetTempNode))
        {
            targetTempNode = targetManager.colonyConnections.get(targetTempNode).getPreviousNode();
        }

        if (targetTempNode == null && !targetManager.gateHouses.contains(targetTempNode))
        {
            MessageUtils.format(Component.translatable(COM_MINECOLONIES_CONNECTION_FAIL)).sendTo(this.colony).forManagers();;
            return false;
        }

        // Set gate houses as connected.
        connectedColonies.put(targetColony.getID(), targetTempNode);
        targetManager.connectedColonies.put(colony.getID(), tempNode);

        // Connect the two middle nodes.
        colonyConnections.get(intermediateNode).alterNextNode(clickedPos);
        targetNode.alterNextNode(intermediateNode);

        // Add the two colonies together
        targetTempNode = targetNode.getPosition();
        while (targetManager.colonyConnections.containsKey(targetTempNode))
        {
            final ColonyConnectionNode targetColonyNode = targetManager.colonyConnections.get(targetTempNode);
            targetColonyNode.setTargetColonyId(colony.getID());

            final ColonyConnectionNode colonyConnectionNode = new ColonyConnectionNode(targetTempNode);
            colonyConnectionNode.alterPreviousNode(targetColonyNode.getNextNode());
            colonyConnectionNode.alterNextNode(targetColonyNode.getPreviousNode());
            colonyConnectionNode.setTargetColonyId(targetColony.getID());
            colonyConnections.put(targetTempNode, colonyConnectionNode);

            targetTempNode = targetColonyNode.getPreviousNode();
        }

        // Add the two colonies together
        targetTempNode = intermediateNode;
        while (colonyConnections.containsKey(targetTempNode))
        {
            final ColonyConnectionNode targetColonyNode = colonyConnections.get(targetTempNode);
            targetColonyNode.setTargetColonyId(targetColony.getID());

            final ColonyConnectionNode colonyConnectionNode = new ColonyConnectionNode(targetTempNode);
            colonyConnectionNode.alterPreviousNode(targetColonyNode.getNextNode());
            colonyConnectionNode.alterNextNode(targetColonyNode.getPreviousNode());
            colonyConnectionNode.setTargetColonyId(colony.getID());
            targetManager.colonyConnections.put(targetTempNode, colonyConnectionNode);

            targetTempNode = targetColonyNode.getPreviousNode();
        }

        MessageUtils.format(COM_MINECOLONIES_CONNECTION_SUCCESS, colony.getName(), targetColony.getName()).sendTo(this.colony).forManagers();;
        MessageUtils.format(COM_MINECOLONIES_CONNECTION_SUCCESS, targetColony.getName(), colony.getName()).sendTo(targetColony).forManagers();;

        // todo Set connection as pending for pathfinding.
        return true;
    }

    @Override
    public void tick()
    {
        // todo Try to path between nodes and break sign if unsuccessful and remove connection.
        // todo maybe add those first to a pending set that we can process tick by tick. You can not connect if the previous node is not verified yet, so we need to add that to the error.
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
    }

    @Override
    public void removeGateHouse(final BlockPos gateHousePosition)
    {
        gateHouses.remove(gateHousePosition);
        //todo update graph
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        colonyConnections.clear();
        final ListTag connectionTagList = compound.getList(TAG_CONNECTIONS, Tag.TAG_COMPOUND);
        for (final Tag tag : connectionTagList)
        {
            final BlockPos pos = BlockPosUtil.read((CompoundTag) tag, TAG_POS);
            final ColonyConnectionNode connectionPoint = new ColonyConnectionNode(pos);
            connectionPoint.read((CompoundTag) tag);
            colonyConnections.put(connectionPoint.getPosition(), connectionPoint);
        }

        connectedColonies.clear();
        final ListTag connectedColonyTagList = compound.getList(TAG_COLONIES, Tag.TAG_COMPOUND);
        for (final Tag tag : connectedColonyTagList)
        {
            final BlockPos pos = BlockPosUtil.read((CompoundTag) tag, TAG_POS);
            final int colonyId = ((CompoundTag) tag).getInt(TAG_COLONY_ID);
            connectedColonies.put(colonyId, pos);
        }


        gateHouses.clear();
        final ListTag gateHouseTagList = compound.getList(TAG_GATEHOUSES, Tag.TAG_COMPOUND);
        for (final Tag tag : gateHouseTagList)
        {
            gateHouses.add(BlockPosUtil.read((CompoundTag) tag, TAG_POS));
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
        for (final Int2ObjectMap.Entry<BlockPos> entry : connectedColonies.int2ObjectEntrySet())
        {
            final CompoundTag compound = new CompoundTag();
            BlockPosUtil.write(compound, TAG_POS, entry.getValue());
            compoundTag.putInt(TAG_COLONY_ID, entry.getIntKey());
            connectedColonyTagList.add(compound);
        }
        compoundTag.put(TAG_COLONIES, connectedColonyTagList);

        @NotNull final ListTag gateHouseTagList = new ListTag();
        for (final BlockPos gateHouse : gateHouses)
        {
            gateHouseTagList.add(BlockPosUtil.write(new CompoundTag(), TAG_POS, gateHouse));
        }
        compoundTag.put(TAG_GATEHOUSES, connectedColonyTagList);
        return compoundTag;
    }
}
