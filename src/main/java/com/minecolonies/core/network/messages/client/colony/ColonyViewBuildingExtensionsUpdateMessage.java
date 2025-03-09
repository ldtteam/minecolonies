package com.minecolonies.core.network.messages.client.colony;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildingextensions.IBuildingExtension;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildingextensions.registry.BuildingExtensionDataManager;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Update message for auto syncing the entire building extensions list.
 */
public class ColonyViewBuildingExtensionsUpdateMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "colony_view_building_extensions_update", ColonyViewBuildingExtensionsUpdateMessage::new);

    /**
     * The colony this building extension belongs to.
     */
    private final int colonyId;

    /**
     * Dimension of the colony.
     */
    private final ResourceKey<Level> dimension;

    /**
     * The list of building extension items.
     */
    private final Map<IBuildingExtension, IBuildingExtension> extensions;

    /**
     * Creates a message to handle colony all building extension views.
     *
     * @param colony the colony this building extension is in.
     * @param extensions the complete list of building extensions of this colony.
     */
    public ColonyViewBuildingExtensionsUpdateMessage(@NotNull final IColony colony, @NotNull final Set<IBuildingExtension> extensions)
    {
        super(TYPE);
        this.colonyId = colony.getID();
        this.dimension = colony.getDimension();
        this.extensions = new HashMap<>();
        extensions.forEach(extension -> this.extensions.put(extension, extension));
    }

    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeUtf(dimension.location().toString());
        buf.writeInt(extensions.size());
        for (final IBuildingExtension extension : extensions.keySet())
        {
            final RegistryFriendlyByteBuf extensionBuffer = BuildingExtensionDataManager.extensionToBuffer(extension, buf.registryAccess());
            extensionBuffer.resetReaderIndex();
            buf.writeByteArray(extensionBuffer.array());
        }
    }

    protected ColonyViewBuildingExtensionsUpdateMessage(@NotNull final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        colonyId = buf.readInt();
        dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(buf.readUtf(32767)));
        extensions = new HashMap<>();
        final int extensionCount = buf.readInt();
        for (int i = 0; i < extensionCount; i++)
        {
            final IBuildingExtension extension = BuildingExtensionDataManager.bufferToExtension(new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(buf.readByteArray()), buf.registryAccess()));
            extensions.put(extension, extension);
        }
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final Player player)
    {
        final IColonyView view = IColonyManager.getInstance().getColonyView(colonyId, dimension);
        if (view != null)
        {
            final Set<IBuildingExtension> updatedExtensions = new HashSet<>();
            view.getBuildingExtensions(extension -> true).forEach(existingExtension -> {
                if (this.extensions.containsKey(existingExtension))
                {
                    final RegistryFriendlyByteBuf copyBuffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.level().registryAccess());
                    this.extensions.get(existingExtension).serialize(copyBuffer);
                    existingExtension.deserialize(copyBuffer);
                    updatedExtensions.add(existingExtension);
                }
            });
            updatedExtensions.addAll(this.extensions.keySet());

            view.handleColonyBuildingExtensionsViewUpdateMessage(updatedExtensions);
        }
        else
        {
            Log.getLogger().error("Colony view does not exist for ID #{}", colonyId);
        }
    }
}
