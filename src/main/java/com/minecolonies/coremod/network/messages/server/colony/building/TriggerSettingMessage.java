package com.minecolonies.coremod.network.messages.server.colony.building;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.modules.settings.ISetting;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.modules.SettingsModule;
import com.minecolonies.coremod.colony.buildings.modules.settings.SettingKey;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message handling setting triggering.
 */
public class TriggerSettingMessage extends AbstractBuildingServerMessage<AbstractBuildingWorker>
{
    /**
     * The unique setting key.
     */
    private ResourceLocation key;

    /**
     * The value of the setting.
     */
    private ISetting value;

    /**
     * Empty standard constructor.
     */
    public TriggerSettingMessage()
    {
        super();
    }

    /**
     * Settings constructor.
     * @param building the building involving the setting.
     * @param key the unique key of it.
     * @param value the value of the setting.
     */
    public TriggerSettingMessage(final IBuildingView building, final ISettingKey<?> key, final ISetting value)
    {
        super(building);
        this.key = key.getUniqueId();
        this.value = value;
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        this.key = buf.readResourceLocation();
        this.value = StandardFactoryController.getInstance().deserialize(buf);
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(this.key);
        StandardFactoryController.getInstance().serialize(buf, this.value);
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final AbstractBuildingWorker building)
    {
        building.getFirstOptionalModuleOccurance(SettingsModule.class).ifPresent(m -> m.updateSetting(new SettingKey<>(this.value.getClass(), this.key), this.value, ctxIn.getSender()));
    }
}

