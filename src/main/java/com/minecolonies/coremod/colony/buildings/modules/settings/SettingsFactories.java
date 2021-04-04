package com.minecolonies.coremod.colony.buildings.modules.settings;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.buildings.modules.settings.IBoolSettingFactory;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

/**
 * Factory implementation taking care of creating new instances, serializing and deserializing Settings.
 */
public class SettingsFactories
{
    /**
     * Specific factory for the bool setting.
     */
    public static class BoolSettingFactory implements IBoolSettingFactory<BoolSetting>
    {
        /**
         * Compound tag for the value.
         */
        private static final String TAG_VALUE = "value";

        /**
         * Compound tag for the default.
         */
        private static final String TAG_DEFAULT = "default";

        @NotNull
        @Override
        public TypeToken<BoolSetting> getFactoryOutputType()
        {
            return TypeToken.of(BoolSetting.class);
        }

        @NotNull
        @Override
        public TypeToken<FactoryVoidInput> getFactoryInputType()
        {
            return TypeConstants.FACTORYVOIDINPUT;
        }

        @NotNull
        @Override
        public BoolSetting getNewInstance(final boolean value, final boolean def)
        {
            return new BoolSetting(value, def);
        }

        @NotNull
        @Override
        public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final BoolSetting storage)
        {
            final CompoundNBT compound = new CompoundNBT();
            compound.putBoolean(TAG_VALUE, storage.getValue());
            compound.putBoolean(TAG_DEFAULT, storage.getDefault());
            return compound;
        }

        @NotNull
        @Override
        public BoolSetting deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
        {
            return this.getNewInstance(nbt.getBoolean(TAG_VALUE), nbt.getBoolean(TAG_DEFAULT));
        }

        @Override
        public void serialize(@NotNull final IFactoryController controller, @NotNull final BoolSetting input, @NotNull final PacketBuffer packetBuffer)
        {
            packetBuffer.writeBoolean(input.getValue());
            packetBuffer.writeBoolean(input.getDefault());
        }

        @NotNull
        @Override
        public BoolSetting deserialize(@NotNull final IFactoryController controller, @NotNull final PacketBuffer buffer) throws Throwable
        {
            return this.getNewInstance(buffer.readBoolean(), buffer.readBoolean());
        }

        @Override
        public short getSerializationId()
        {
            return 43;
        }
    }
}
