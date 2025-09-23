package com.minecolonies.core.network.messages.client.colony;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.managers.interfaces.IBuildingModuleTemplateManager;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Message to synch research manager to colony.
 */
public class ColonyViewModuleTemplateManagerViewMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE =
        PlayMessageType.forClient(Constants.MOD_ID, "colony_view_module_template_manager_view", ColonyViewModuleTemplateManagerViewMessage::new);

    private final int colonyId;

    private final CompoundTag data;

    /**
     * Dimension of the colony.
     */
    private final ResourceKey<Level> dimension;

    /**
     * Creates a message to send the manager to the client.
     *
     * @param colony                the colony.
     * @param moduleTemplateManager the manager instance.
     */
    public ColonyViewModuleTemplateManagerViewMessage(final IColony colony, @NotNull final IBuildingModuleTemplateManager moduleTemplateManager)
    {
        super(TYPE);
        this.colonyId = colony.getID();
        this.dimension = colony.getDimension();
        this.data = moduleTemplateManager.serializeNBT(colony.getWorld().registryAccess());
    }

    public ColonyViewModuleTemplateManagerViewMessage(@NotNull final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        colonyId = buf.readInt();
        dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(buf.readUtf()));
        data = buf.readNbt();
    }

    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeUtf(dimension.location().toString());
        buf.writeNbt(data);
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final Player player)
    {
        final IColonyView colonyView = IColonyManager.getInstance().getColonyView(colonyId, dimension);
        if (colonyView != null)
        {
            colonyView.handleColonyViewModuleTemplateManagerUpdate(player.level().registryAccess(), data);
        }
    }
}
