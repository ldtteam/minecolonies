package com.minecolonies.core.client.render.state;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class DecoControllerRenderState extends BlockEntityRenderState
{
    public Direction direction = Direction.UP;
    public BlockState controllerState;
    public VoxelShape neighborShape;
    public Vec3 translation = Vec3.ZERO;
    public boolean renderAtNeighbor;
    public final BlockModelRenderState controllerModel = new BlockModelRenderState();

    public @NotNull VoxelShape neighborShape()
    {
        return this.neighborShape == null ? net.minecraft.world.phys.shapes.Shapes.empty() : this.neighborShape;
    }
}
