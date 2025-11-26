package com.minecolonies.core.network.messages.server.colony.building.template;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Ignore using a module template on a given building.
 */
public class IgnoreTemplateMessage extends AbstractBuildingServerMessage<IBuilding>
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "ignore_template", IgnoreTemplateMessage::new);

    /**
     * The storage key of the module.
     */
    private final ResourceLocation storageKey;

    /**
     * The name of the template to use.
     */
    private final String templateName;

    /**
     * Creates an ignore module template message.
     *
     * @param building     the building instance.
     * @param storageKey   the storage key of the module.
     * @param templateName the name of the template to use.
     */
    public IgnoreTemplateMessage(final IBuildingView building, final ResourceLocation storageKey, final String templateName)
    {
        super(TYPE, building);
        this.storageKey = storageKey;
        this.templateName = templateName;
    }

    private IgnoreTemplateMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<AbstractServerPlayMessage> type)
    {
        super(buf, type);
        storageKey = buf.readResourceLocation();
        templateName = buf.readUtf();
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player, final IColony colony, final IBuilding building)
    {
        colony.getBuildingModuleTemplateManager().ignoreTemplate(building.getBuildingType(), storageKey, templateName, building.getID());
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeResourceLocation(storageKey);
        buf.writeUtf(templateName);
    }
}
