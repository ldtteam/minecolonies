package com.minecolonies.coremod.structures;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Thanks to: https://github.com/TelepathicGrunt/StructureTutorialMod/tree/1.18.x-Forge-Jigsaw
 */
public class MineColoniesStructures
{
    public static final DeferredRegister<StructureFeature<?>> DEFERRED_REGISTRY_STRUCTURE = DeferredRegister.create(ForgeRegistries.STRUCTURE_FEATURES, Constants.MOD_ID);

    public static final RegistryObject<StructureFeature<JigsawConfiguration>> EMPTY_COLONY = DEFERRED_REGISTRY_STRUCTURE.register("empty_colony", () -> (new EmptyColonyStructure(JigsawConfiguration.CODEC)));
}
