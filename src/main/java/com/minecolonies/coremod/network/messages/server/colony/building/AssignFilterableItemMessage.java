package com.minecolonies.coremod.network.messages.server.colony.building;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.modules.ItemListModule;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message which handles the assignment of items to filterable item lists.
 */
public class AssignFilterableItemMessage extends AbstractBuildingServerMessage<AbstractBuildingWorker>
{
    /**
     * True if assign, false if remove.
     */
    private boolean assign;

    /**
     * The item in question.
     */
    private ItemStorage item;

    /**
     * The id of the list.
     */
    private String id;

    /**
     * Empty standard constructor.
     */
    public AssignFilterableItemMessage()
    {
        super();
    }

    /**
     * Creates the message to add an item.
     *
     * @param id       the id of the list of filterables.
     * @param assign   compost if true, dont if false.
     * @param item     the item to assign
     * @param building the building we're executing on.
     */
    public AssignFilterableItemMessage(final IBuildingView building, final String id, final ItemStorage item, final boolean assign)
    {
        super(building);
        this.assign = assign;
        this.item = item;
        this.id = id;
    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {

        this.assign = buf.readBoolean();
        this.item = new ItemStorage(buf.readItem());
        this.id = buf.readUtf(32767);
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {

        buf.writeBoolean(this.assign);
        buf.writeItem(this.item.getItemStack());
        buf.writeUtf(this.id);
    }

    @Override
    public void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final AbstractBuildingWorker building)
    {
        if (building.hasModule(ItemListModule.class))
        {
            if (assign)
            {
                building.getModuleMatching(ItemListModule.class, m -> m.getId().equals(id)).addItem(item);
            }
            else
            {
                building.getModuleMatching(ItemListModule.class, m -> m.getId().equals(id)).removeItem(item);
            }
        }
    }
}

