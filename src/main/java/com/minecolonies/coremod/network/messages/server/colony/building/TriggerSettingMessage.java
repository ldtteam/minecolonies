package com.minecolonies.coremod.network.messages.server.colony.building;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.modules.settings.ISetting;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.modules.SettingsModule;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message handling setting triggering.
 */
public class TriggerSettingMessage extends AbstractBuildingServerMessage<AbstractBuildingWorker>
{
    /**
     * The unique setting key.
     */
    private String key;

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
    public TriggerSettingMessage(final IBuildingView building, final String key, final ISetting value)
    {
        super(building);
        this.key = key;
        this.value = value;
    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {
        this.key = buf.readString(32767);
        this.value = StandardFactoryController.getInstance().deserialize(buf);
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {
        buf.writeString(this.key);
        StandardFactoryController.getInstance().serialize(buf, this.value);
    }

    @Override
    public void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final AbstractBuildingWorker building)
    {
        if (building.hasModule(SettingsModule.class))
        {
            building.getFirstModuleOccurance(SettingsModule.class).ifPresent(m -> m.with(this.key, this.value));
        }
    }
}

