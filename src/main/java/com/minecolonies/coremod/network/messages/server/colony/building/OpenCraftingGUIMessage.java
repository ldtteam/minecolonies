package com.minecolonies.coremod.network.messages.server.colony.building;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.inventory.container.ContainerCrafting;
import com.minecolonies.api.inventory.container.ContainerCraftingBrewingstand;
import com.minecolonies.api.inventory.container.ContainerCraftingFurnace;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;
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
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {
        this.id = buf.readUtf(32767);
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {
        buf.writeUtf(id);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        final ServerPlayerEntity player = ctxIn.getSender();
        if (player == null)
        {
            return;
        }

        final AbstractCraftingBuildingModule module = building.getModuleMatching(AbstractCraftingBuildingModule.class, m -> m.getId().equals(id));
        if (module.canLearnRecipe(ICraftingBuildingModule.CrafingType.SMELTING))
        {
            NetworkHooks.openGui(player, new INamedContainerProvider()
            {
                @NotNull
                @Override
                public ITextComponent getDisplayName()
                {
                    return new StringTextComponent("Furnace Crafting GUI");
                }

                @NotNull
                @Override
                public Container createMenu(final int id, @NotNull final PlayerInventory inv, @NotNull final PlayerEntity player)
                {
                    return new ContainerCraftingFurnace(id, inv, building.getID(), module.getId());
                }
            }, buffer -> new PacketBuffer(buffer.writeBlockPos(building.getID()).writeUtf(module.getId())));
        }
        else if (module.canLearnRecipe(ICraftingBuildingModule.CrafingType.BREWING))
        {
            NetworkHooks.openGui(player, new INamedContainerProvider()
            {
                @NotNull
                @Override
                public ITextComponent getDisplayName()
                {
                    return new StringTextComponent("Brewing Crafting GUI");
                }

                @NotNull
                @Override
                public Container createMenu(final int id, @NotNull final PlayerInventory inv, @NotNull final PlayerEntity player)
                {
                    return new ContainerCraftingBrewingstand(id, inv, building.getID(), module.getId());
                }
            }, buffer -> new PacketBuffer(buffer.writeBlockPos(building.getID()).writeUtf(module.getId())));
        }
        else
        {
            NetworkHooks.openGui(player, new INamedContainerProvider()
            {
                @NotNull
                @Override
                public ITextComponent getDisplayName()
                {
                    return new StringTextComponent("Crafting GUI");
                }

                @NotNull
                @Override
                public Container createMenu(final int id, @NotNull final PlayerInventory inv, @NotNull final PlayerEntity player)
                {
                    return new ContainerCrafting(id, inv, module.canLearnRecipe(ICraftingBuildingModule.CrafingType.LARGE), building.getID(), module.getId());
                }
            }, buffer -> new PacketBuffer(buffer.writeBoolean(module.canLearnRecipe(ICraftingBuildingModule.CrafingType.LARGE))).writeBlockPos(building.getID()).writeUtf(module.getId()));
        }
    }
}
