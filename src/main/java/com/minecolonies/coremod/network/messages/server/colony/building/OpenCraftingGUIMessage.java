package com.minecolonies.coremod.network.messages.server.colony.building;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.inventory.container.ContainerCrafting;
import com.minecolonies.api.inventory.container.ContainerCraftingFurnace;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

/**
 * Message sent to open an inventory.
 */
public class OpenCraftingGUIMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * The type of container.
     */
    private String id;

    /**
     * Empty public constructor.
     */
    public OpenCraftingGUIMessage()
    {
        super();
    }

    /**
     * Creates an open inventory message for a building.
     * @param id the string id.
     * @param building {@link AbstractBuildingView}
     */
    public OpenCraftingGUIMessage(@NotNull final AbstractBuildingView building, final String id)
    {
        super(building);
        this.id = id;
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        this.id = buf.readUtf(32767);
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeUtf(id);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        final ServerPlayer player = ctxIn.getSender();
        if (player == null)
        {
            return;
        }

        final AbstractCraftingBuildingModule module = building.getModuleMatching(AbstractCraftingBuildingModule.class, m -> m.getId().equals(id));
        if (module.canLearnFurnaceRecipes())
        {
            NetworkHooks.openGui(player, new MenuProvider()
            {
                @NotNull
                @Override
                public Component getDisplayName()
                {
                    return new TextComponent("Furnace Crafting GUI");
                }

                @NotNull
                @Override
                public AbstractContainerMenu createMenu(final int id, @NotNull final Inventory inv, @NotNull final Player player)
                {
                    return new ContainerCraftingFurnace(id, inv, building.getID(), module.getId());
                }
            }, buffer -> new FriendlyByteBuf(buffer.writeBlockPos(building.getID()).writeUtf(module.getId())));
        }
        else
        {
            NetworkHooks.openGui(player, new MenuProvider()
            {
                @NotNull
                @Override
                public Component getDisplayName()
                {
                    return new TextComponent("Crafting GUI");
                }

                @NotNull
                @Override
                public AbstractContainerMenu createMenu(final int id, @NotNull final Inventory inv, @NotNull final Player player)
                {
                    return new ContainerCrafting(id, inv, module.canLearnLargeRecipes(), building.getID(), module.getId());
                }
            }, buffer -> new FriendlyByteBuf(buffer.writeBoolean(module.canLearnLargeRecipes())).writeBlockPos(building.getID()).writeUtf(module.getId()));
        }
    }
}
