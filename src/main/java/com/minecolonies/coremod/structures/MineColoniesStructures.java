package com.minecolonies.coremod.structures;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static com.minecolonies.coremod.structures.EmptyColonyStructure.COLONY_CODEC;
import static com.minecolonies.coremod.structures.EmptyNetherColonyStructure.NETHER_COLONY_CODEC;

/**
 * Thanks to: https://github.com/TelepathicGrunt/StructureTutorialMod/tree/1.18.x-Forge-Jigsaw
 */
public class MineColoniesStructures
{
    /**
     * We are using the Deferred Registry system to register our structure as this is the preferred way on Forge.
     * This will handle registering the base structure for us at the correct time so we don't have to handle it ourselves.
     */
    public static final DeferredRegister<StructureType<?>> DEFERRED_REGISTRY_STRUCTURE = DeferredRegister.create(Registry.STRUCTURE_TYPE_REGISTRY, Constants.MOD_ID);

    /**
     * Registers the base structure itself and sets what its path is. In this case,
     * this base structure will have the resourcelocation of structure_tutorial:sky_structures.
     */
    public static final RegistryObject<StructureType<EmptyColonyStructure>> EMPTY_COLONY = DEFERRED_REGISTRY_STRUCTURE.register("empty_colony", () -> () -> COLONY_CODEC);
    public static final RegistryObject<StructureType<EmptyNetherColonyStructure>> EMPTY_NETHER_COLONY = DEFERRED_REGISTRY_STRUCTURE.register("empty_nether_colony", () -> () -> NETHER_COLONY_CODEC);

}
