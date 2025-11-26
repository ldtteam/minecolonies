package com.minecolonies.core.network.messages.server.colony.building.template;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.ITemplateModule;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Update a template for a building, given the current configuration of the module on the server.
 */
public class UpdateTemplateDataMessage extends AbstractBuildingServerMessage<IBuilding>
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "update_template_data", UpdateTemplateDataMessage::new);

    /**
     * The storage key of the module.
     */
    private final ResourceLocation storageKey;

    /**
     * The name of the template to use.
     */
    private final String templateName;

    /**
     * Creates an update module template message.
     *
     * @param building     the building instance.
     * @param storageKey   the storage key of the module.
     * @param templateName the name of the template to use.
     */
    public UpdateTemplateDataMessage(final IBuildingView building, final ResourceLocation storageKey, final String templateName)
    {
        super(TYPE, building);
        this.storageKey = storageKey;
        this.templateName = templateName;
    }

    private UpdateTemplateDataMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<AbstractServerPlayMessage> type)
    {
        super(buf, type);
        storageKey = buf.readResourceLocation();
        templateName = buf.readUtf();
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player, final IColony colony, final IBuilding building)
    {
        try
        {
            final ITemplateModule module = building.getModuleMatching(ITemplateModule.class, m -> m.getTemplateStorageId().equals(storageKey));
            colony.getBuildingModuleTemplateManager().updateTemplate(ctxIn.player().registryAccess(), module, templateName);
        }
        catch (final Exception ex)
        {
            Log.getLogger().error("Exception during updating module template", ex);
        }
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeResourceLocation(storageKey);
        buf.writeUtf(templateName);
    }
}
