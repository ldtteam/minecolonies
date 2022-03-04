package com.minecolonies.coremod.structures;

import com.minecolonies.api.util.constant.Constants;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Thanks to: https://github.com/TelepathicGrunt/StructureTutorialMod/tree/1.18.x-Forge-Jigsaw
 */
public class MineColoniesStructures
{
    public static final Codec<JigsawConfiguration> COLONY_CODEC = RecordCodecBuilder.create((conf) -> conf.group(StructureTemplatePool.CODEC.fieldOf("start_pool")
      .forGetter(JigsawConfiguration::startPool), Codec.intRange(0, 10).fieldOf("size")
      .forGetter(JigsawConfiguration::maxDepth)).apply(conf, JigsawConfiguration::new));

    public static final    DeferredRegister<StructureFeature<?>> DEFERRED_REGISTRY_STRUCTURE = DeferredRegister.create(ForgeRegistries.STRUCTURE_FEATURES, Constants.MOD_ID);

    public static final RegistryObject<StructureFeature<JigsawConfiguration>> EMPTY_COLONY = DEFERRED_REGISTRY_STRUCTURE.register("empty_colony", () -> (new EmptyColonyStructure(COLONY_CODEC)));
}
