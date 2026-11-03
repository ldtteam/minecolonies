package com.minecolonies.core.network.messages.server.colony.building;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.settings.ISetting;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.SettingsModule;
import com.minecolonies.core.colony.buildings.modules.settings.SettingKey;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message handling setting triggering.
 */
//todo make this non building based, and give it an optional blockpos which if zero means colony level setting.
public class TriggerSettingMessage extends AbstractColonyServerMessage
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
     * The module id
     */
    private int moduleID;

    /**
     * The building position that the setting was triggered for or zero for colony level.
     */
    private BlockPos buildingPos;

    /**
     * Empty standard constructor.
     */
    public TriggerSettingMessage()
    {
        super();
    }

    /**
     * Settings constructor.
     * @param colony the building involving the setting.
     * @param key the unique key of it.
     * @param value the value of the setting.
     */
    public TriggerSettingMessage(final IColony colony, final ISettingKey<?> key, final ISetting value, final int moduleID, final BlockPos pos)
    {
        super(colony);
        this.key = key.getUniqueId();
        this.value = value;
        this.moduleID = moduleID;
        this.buildingPos = pos;
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        this.moduleID = buf.readInt();
        this.key = buf.readResourceLocation();
        this.value = StandardFactoryController.getInstance().deserialize(buf);
        this.buildingPos = buf.readBlockPos();
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(moduleID);
        buf.writeResourceLocation(this.key);
        StandardFactoryController.getInstance().serialize(buf, this.value);
        buf.writeBlockPos(this.buildingPos);
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        final ISettingKey settingKey = new SettingKey<>(this.value.getClass(), this.key);
        if (buildingPos.equals(BlockPos.ZERO))
        {
            colony.getSettings().updateSetting(settingKey, this.value, ctxIn.getSender());
        }
        else
        {
            final IBuilding building = colony.getServerBuildingManager().getBuilding(buildingPos);
            if (building != null && building.getModule(moduleID) instanceof SettingsModule module)
            {
                module.updateSetting(settingKey, this.value, ctxIn.getSender());
            }
        }
    }
}
