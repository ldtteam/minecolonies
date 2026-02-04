package com.minecolonies.core.network.messages.server.colony.building;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.settings.ISetting;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.modules.SettingsModule;
import com.minecolonies.core.colony.buildings.modules.settings.SettingKey;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Message handling setting triggering.
 */
public class TriggerSettingMessage extends AbstractColonyServerMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "trigger_setting", TriggerSettingMessage::new);

    /**
     * The unique setting key.
     */
    private ResourceLocation key;

    /**
     * The value of the setting.
     */
    private ISetting<?> value;

    /**
     * The module id
     */
    private int moduleID;

    /**
     * The building position that the setting was triggered for or zero for colony level.
     */
    private BlockPos buildingPos;


    /**
     * Settings constructor.
     * @param colony the building involving the setting.
     * @param key the unique key of it.
     * @param value the value of the setting.
     */
    public TriggerSettingMessage(final IColony colony, final ISettingKey<?> key, final ISetting value, final int moduleID, final BlockPos pos)
    {
        super(TYPE, colony);
        this.key = key.getUniqueId();
        this.value = value;
        this.moduleID = moduleID;
        this.buildingPos = pos;
    }

    protected TriggerSettingMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.moduleID = buf.readInt();
        this.key = buf.readResourceLocation();
        this.value = StandardFactoryController.getInstance().deserialize(buf);
        this.buildingPos = buf.readBlockPos();
    }

    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeInt(moduleID);
        buf.writeResourceLocation(this.key);
        StandardFactoryController.getInstance().serialize(buf, this.value);
        buf.writeBlockPos(this.buildingPos);
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
    {
        final ISettingKey settingKey = new SettingKey<>(this.value.getClass(), this.key);
        if (buildingPos.equals(BlockPos.ZERO))
        {
            colony.getSettings().updateSetting(settingKey, this.value, player);
        }
        else
        {
            final IBuilding building = colony.getServerBuildingManager().getBuilding(buildingPos);
            if (building != null && building.getModule(moduleID) instanceof SettingsModule module)
            {
                module.updateSetting(settingKey, this.value, player);
            }
        }
    }
}
