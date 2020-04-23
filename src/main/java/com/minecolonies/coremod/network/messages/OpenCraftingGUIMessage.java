package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.inventory.container.ContainerCrafting;
import com.minecolonies.api.inventory.container.ContainerCraftingFurnace;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingSmelterCrafter;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message sent to open an inventory.
 */
public class OpenCraftingGUIMessage implements IMessage
{
    /**
     * The position of the inventory block/entity.
     */
    private BlockPos buildingId;

    /**
     * The colony id the field or building etc is in.
     */
    private int colonyId;

    /**
     * Size of the crafting grid.
     */
    private int gridSize;

    /**
     * The dimension of the 
     */
    private int dimension;

    /**
     * Empty public constructor.
     */
    public OpenCraftingGUIMessage()
    {
        super();
    }

    /**
     * Creates an open inventory message for a building.
     *
     * @param building {@link AbstractBuildingView}
     */
    public OpenCraftingGUIMessage(@NotNull final AbstractBuildingView building, final int gridSize)
    {
        super();
        this.buildingId = building.getPosition();
        this.gridSize = gridSize;
        this.colonyId = building.getColony().getID();
        this.dimension = building.getColony().getDimension();
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        this.gridSize = buf.readInt();
        this.colonyId = buf.readInt();
        this.buildingId = buf.readBlockPos();
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(this.gridSize);
        buf.writeInt(this.colonyId);
        buf.writeBlockPos(buildingId);
        buf.writeInt(dimension);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, dimension);
        final ServerPlayerEntity player = ctxIn.getSender();
        if (colony != null && checkPermissions(colony, player))
        {
            final BlockPos pos = buildingId;
            //todo, which is our inventory?
            final IBuilding building = colony.getBuildingManager().getBuilding(buildingId);
            if (building instanceof AbstractBuildingSmelterCrafter)
            {
                NetworkHooks.openGui(player, new INamedContainerProvider()
                {
                    @Override
                    public ITextComponent getDisplayName()
                    {
                        return new StringTextComponent("Furnace Crafting GUI");
                    }

                    @NotNull
                    @Override
                    public Container createMenu(final int id, @NotNull final PlayerInventory inv, @NotNull final PlayerEntity player)
                    {
                        final PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
                        buffer.writeBlockPos(pos);
                        return new ContainerCraftingFurnace(id, inv, buffer);
                    }
                }, pos);
            }
            else
            {
                NetworkHooks.openGui(player, new INamedContainerProvider()
                {
                    @Override
                    public ITextComponent getDisplayName()
                    {
                        return new StringTextComponent("Crafting GUI");
                    }

                    @NotNull
                    @Override
                    public Container createMenu(final int id, @NotNull final PlayerInventory inv, @NotNull final PlayerEntity player)
                    {
                        final PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
                        buffer.writeBoolean(gridSize > 2);
                        buffer.writeBlockPos(pos);
                        return new ContainerCrafting(id, inv, buffer);
                    }
                }, buffer -> new PacketBuffer(buffer.writeBoolean(gridSize > 2)).writeBlockPos(pos));
            }
        }
    }

    private static boolean checkPermissions(final IColony colony, final ServerPlayerEntity player)
    {
        //Verify player has permission to change this huts settings
        return colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS);
    }
}
