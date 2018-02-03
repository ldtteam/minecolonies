package com.minecolonies.coremod.placementhandlers;

import net.minecraft.util.math.BlockPos;

public class PlacementError {

    public enum PlacementErrorType {
        NOT_SOLID,
        INSIDE_COLONY,
        NEEDS_AIR_ABOVE
    }

    private PlacementErrorType type;
    private BlockPos pos;
    
    public PlacementError(PlacementErrorType type, BlockPos pos) {
        super();
        this.type = type;
        this.pos = pos;
    }

    public PlacementErrorType getType() {
        return type;
    }

    public void setType(PlacementErrorType type) {
        this.type = type;
    }

    public BlockPos getPos() {
        return pos;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }
}
