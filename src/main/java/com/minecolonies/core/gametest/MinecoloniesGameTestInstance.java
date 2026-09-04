package com.minecolonies.core.gametest;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;

/**
 * A small adapter for registering MineColonies' imperative lifecycle checks with the MC 26.2 GameTest registry.
 */
public final class MinecoloniesGameTestInstance extends GameTestInstance
{
    public static final MapCodec<MinecoloniesGameTestInstance> CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(TestData.CODEC.forGetter(MinecoloniesGameTestInstance::encodedInfo))
        .apply(instance, data -> new MinecoloniesGameTestInstance(data, helper -> { })));

    private final Consumer<GameTestHelper> test;

    private static TestData<Holder<TestEnvironmentDefinition<?>>> encodedInfo(final MinecoloniesGameTestInstance instance)
    {
        return instance.info();
    }

    public MinecoloniesGameTestInstance(
      final TestData<Holder<TestEnvironmentDefinition<?>>> info,
      final Consumer<GameTestHelper> test)
    {
        super(info);
        this.test = test;
    }

    @Override
    public void run(final GameTestHelper helper)
    {
        test.accept(helper);
    }

    @Override
    public MapCodec<? extends GameTestInstance> codec()
    {
        return CODEC;
    }

    @Override
    protected MutableComponent typeDescription()
    {
        return Component.literal("minecolonies_function");
    }
}
