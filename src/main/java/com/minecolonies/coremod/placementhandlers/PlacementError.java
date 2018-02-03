package com.minecolonies.coremod.placementhandlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.util.math.BlockPos;

public class PlacementError {

    public enum PlacementErrorType {
        NOT_SOLID,
        INSIDE_COLONY,
        NEEDS_AIR_ABOVE
    }

    private PlacementErrorType type;
    private BlockPos pos;
    
    public PlacementError(PlacementErrorType type, BlockPos pos)
    {
        super();
        this.type = type;
        this.pos = pos;
    }

    public PlacementErrorType getType()
    {
        return type;
    }

    public void setType(PlacementErrorType type)
    {
        this.type = type;
    }

    public BlockPos getPos()
    {
        return pos;
    }

    public void setPos(BlockPos pos)
    {
        this.pos = pos;
    }

    public static String blockListToCommaSeparatedString(List<BlockPos> blocksToReportList)
    {
        final StringBuilder outputListStringBuilder = new StringBuilder();
        boolean firstItem = true;
        for (final BlockPos blockPos : blocksToReportList) {
            if (firstItem) {
                firstItem = false;
            } else {
                outputListStringBuilder.append(',');
                outputListStringBuilder.append(' ');
            }
            outputListStringBuilder.append('(');
            outputListStringBuilder.append(blockPos.getX());
            outputListStringBuilder.append(' ');
            outputListStringBuilder.append(blockPos.getY());
            outputListStringBuilder.append(' ');
            outputListStringBuilder.append(blockPos.getZ());
            outputListStringBuilder.append(')');
        }
        return outputListStringBuilder.toString();
    }

    public static Map<PlacementErrorType, List<BlockPos>> partitionPlacementErrorsByErrorType(
            List<PlacementError> placementErrorList)
    {
        final Map<PlacementErrorType, List<BlockPos>> blockPosListByErrorTypeMap = new HashMap<PlacementErrorType, List<BlockPos>>();
        for (final PlacementError placementError : placementErrorList) {
            final PlacementErrorType key = placementError.getType();
            final BlockPos blockPos = placementError.getPos();
            List<BlockPos> blockPosList = blockPosListByErrorTypeMap.get(key);
            if (null == blockPosList) {
                blockPosList = new ArrayList<BlockPos>();
                blockPosListByErrorTypeMap.put(key, blockPosList);
            }
            blockPosList.add(blockPos);
        }
        return blockPosListByErrorTypeMap;
    }

}
