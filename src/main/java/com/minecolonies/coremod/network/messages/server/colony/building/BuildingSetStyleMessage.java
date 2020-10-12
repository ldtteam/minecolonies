package com.minecolonies.coremod.network.messages.server.colony.building;

import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.LoadOnlyStructureHandler;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildBuilding;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import com.minecolonies.coremod.util.ColonyUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import static com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider.TAG_BLUEPRINTDATA;

/**
 * Message to set the style of a building.
 */
public class BuildingSetStyleMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * The style to set.
     */
    private String style;

    /**
     * Empty constructor used when registering the
     */
    public BuildingSetStyleMessage()
    {
        super();
    }

    /**
     * Creates object for the style of a building.
     *
     * @param building View of the building to read data from.
     * @param style    style of the building.
     */
    public BuildingSetStyleMessage(@NotNull final IBuildingView building, final String style)
    {
        super(building);
        this.style = style;
    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {
        style = buf.readString(32767);
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {
        buf.writeString(style);
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        building.setStyle(style);
        if (building.getTileEntity() != null)
        {
            building.getTileEntity().setStyle(style);

            final WorkOrderBuildBuilding workOrder = new WorkOrderBuildBuilding(building, building.getBuildingLevel() + 1);
            final LoadOnlyStructureHandler structure = new LoadOnlyStructureHandler(colony.getWorld(), building.getPosition(), workOrder.getStructureName(), new PlacementSettings(), true);

            CompoundNBT teData = structure.getBluePrint().getTileEntityData(building.getTileEntity().getPos(), structure.getBluePrint().getPrimaryBlockOffset());
            if (teData != null && teData.contains(TAG_BLUEPRINTDATA))
            {
                building.getTileEntity().readSchematicDataFromNBT(teData);
                Chunk chunk = (Chunk) building.getTileEntity().getWorld().getChunk(building.getTileEntity().getPos());
                PacketDistributor.TRACKING_CHUNK.with(() -> chunk).send(building.getTileEntity().getUpdatePacket());
                building.getTileEntity().markDirty();
                building.setHeight(structure.getBluePrint().getSizeY());
                final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> corners
                  = ColonyUtils.calculateCorners(building.getPosition(),
                  colony.getWorld(),
                  structure.getBluePrint(),
                  workOrder.getRotation(colony.getWorld()),
                  workOrder.isMirrored());
                building.setCorners(corners.getA().getA(), corners.getA().getB(), corners.getB().getA(), corners.getB().getB());
            }
        }
    }
}
