package com.minecolonies.core.commands.arguments;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * This class handles registration for custom argument types.
 */
public class ModArgumentTypes
{
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, MOD_ID);

    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, SingletonArgumentInfo<ColonyIdArgument>> COLONY_ID =
            ARGUMENT_TYPES.register("colony_id", () -> ArgumentTypeInfos.registerByClass(ColonyIdArgument.class,
                    SingletonArgumentInfo.contextFree(ColonyIdArgument::id)));
}
