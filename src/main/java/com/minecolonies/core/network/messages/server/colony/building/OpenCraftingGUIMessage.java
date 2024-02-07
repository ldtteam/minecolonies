package com.minecolonies.core.network.messages.server.colony.building;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.crafting.ModCraftingTypes;
import com.minecolonies.api.inventory.container.ContainerCrafting;
import com.minecolonies.api.inventory.container.ContainerCraftingBrewingstand;
import com.minecolonies.api.inventory.container.ContainerCraftingFurnace;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Message sent to open an inventory.
 */
public class OpenCraftingGUIMessage extends AbstractBuildingServerMessage<IBuilding>
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "open_crafting_gui", OpenCraftingGUIMessage::new);

    /**
     * The type of container.
     */
    private final int id;

    /**
     * Creates an open inventory message for a building.
     * @param id the string id.
     * @param building {@link AbstractBuildingView}
     */
    public OpenCraftingGUIMessage(@NotNull final AbstractBuildingView building, final int id)
    {
        super(TYPE, building);
        this.id = id;
    }

    protected OpenCraftingGUIMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.id = buf.readInt();
    }

    @Override
    protected void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeInt(id);
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony, final IBuilding building)
    {
        if (building.getModule(id) instanceof final AbstractCraftingBuildingModule module)
        {
            if (module.canLearn(ModCraftingTypes.SMELTING.get()))
            {
                NetworkHooks.openScreen(player, new MenuProvider()
                {
                    @NotNull
                    @Override
                    public Component getDisplayName()
                    {
                        return Component.literal("Furnace Crafting GUI");
                    }

                    @NotNull
                    @Override
                    public AbstractContainerMenu createMenu(final int id, @NotNull final Inventory inv, @NotNull final Player player)
                    {
                        return new ContainerCraftingFurnace(id, inv, building.getID(), module.getProducer().getRuntimeID());
                    }
                }, buffer -> new FriendlyByteBuf(buffer.writeBlockPos(building.getID()).writeInt(module.getProducer().getRuntimeID())));
            }
            else if (module.canLearn(ModCraftingTypes.BREWING.get()))
            {
                NetworkHooks.openScreen(player, new MenuProvider()
                {
                    @NotNull
                    @Override
                    public Component getDisplayName()
                    {
                        return Component.literal("Brewing Crafting GUI");
                    }

                    @NotNull
                    @Override
                    public AbstractContainerMenu createMenu(final int id, @NotNull final Inventory inv, @NotNull final Player player)
                    {
                        return new ContainerCraftingBrewingstand(id, inv, building.getID(), module.getProducer().getRuntimeID());
                    }
                }, buffer -> new FriendlyByteBuf(buffer.writeBlockPos(building.getID()).writeInt(module.getProducer().getRuntimeID())));
            }
            else
            {
                net.neoforged.neoforge.network.NetworkHooks.openScreen(player,
                  new MenuProvider()
                  {
                      @NotNull
                      @Override
                      public Component getDisplayName()
                      {
                          return Component.literal("Crafting GUI");
                      }

                      @NotNull
                      @Override
                      public AbstractContainerMenu createMenu(final int id, @NotNull final Inventory inv, @NotNull final Player player)
                      {
                          return new ContainerCrafting(id, inv, module.canLearn(ModCraftingTypes.LARGE_CRAFTING.get()), building.getID(), module.getProducer().getRuntimeID());
                      }
                  },
                  buffer -> new FriendlyByteBuf(buffer.writeBoolean(module.canLearn(ModCraftingTypes.LARGE_CRAFTING.get()))).writeBlockPos(building.getID())
                    .writeInt(module.getProducer().getRuntimeID()));
            }
        }
    }
}
