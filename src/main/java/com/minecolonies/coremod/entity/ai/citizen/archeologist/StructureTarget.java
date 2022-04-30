package com.minecolonies.coremod.entity.ai.citizen.archeologist;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public record StructureTarget(BlockPos workspaceSpawnTarget, BlockPos structureCenter, ResourceLocation name)
{
}
