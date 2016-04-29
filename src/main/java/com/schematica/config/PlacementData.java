package com.schematica.config;

import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlacementData
{
    public enum PlacementType
    {
        BLOCK, PLAYER, PISTON
    }

    public static final EnumFacing[] VALID_DIRECTIONS = EnumFacing.VALUES;

    public final PlacementType type;
    public       int                          maskOffset         = 0x0;
    public       float                        offsetLowY         = 0.0f;
    public       float                        offsetHighY        = 1.0f;
    public       int                          maskMetaInHand     = -1;
    public       int                          bitShiftMetaInHand = 0;
    public       int                          maskMeta           = 0xF;
    public final Map<EnumFacing, Integer>     mapping            = new HashMap<>();

    public PlacementData(PlacementType type, int... metadata)
    {
        this.type = type;

        for(int i = 0; i < VALID_DIRECTIONS.length && i < metadata.length; i++)
        {
            if(metadata[i] >= 0x0 && metadata[i] <= 0xF)
            {
                this.mapping.put(VALID_DIRECTIONS[i], metadata[i]);
            }
        }
    }

    public PlacementData setOffset(int maskOffset, float offsetLowY, float offsetHighY)
    {
        this.maskOffset = maskOffset;
        this.offsetLowY = offsetLowY;
        this.offsetHighY = offsetHighY;
        return this;
    }

    public PlacementData setMaskMetaInHand(int maskMetaInHand)
    {
        this.maskMetaInHand = maskMetaInHand;
        return this;
    }

    public PlacementData setBitShiftMetaInHand(int bitShiftMetaInHand)
    {
        this.bitShiftMetaInHand = bitShiftMetaInHand;
        return this;
    }

    public PlacementData setMaskMeta(int maskMeta)
    {
        this.maskMeta = maskMeta;
        return this;
    }

    public float getOffsetFromMetadata(int metadata)
    {
        return (metadata & this.maskOffset) == 0 ? this.offsetLowY : this.offsetHighY;
    }

    public int getMetaInHand(int metadata)
    {
        if(this.maskMetaInHand != -1)
        {
            metadata &= this.maskMetaInHand;
        }

        if(this.bitShiftMetaInHand > 0)
        {
            metadata >>= this.bitShiftMetaInHand;
        }
        else if(this.bitShiftMetaInHand < 0)
        {
            metadata <<= -this.bitShiftMetaInHand;
        }

        return metadata;
    }

    public EnumFacing[] getValidDirections(EnumFacing[] solidSides, int metadata)
    {
        List<EnumFacing> list = new ArrayList<>();

        for(EnumFacing direction : solidSides)
        {
            if(this.maskOffset != 0)
            {
                if((metadata & this.maskOffset) == 0)
                {
                    if(this.offsetLowY < 0.5f && direction == EnumFacing.UP)
                    {
                        continue;
                    }
                }
                else
                {
                    if(this.offsetLowY < 0.5f && direction == EnumFacing.DOWN)
                    {
                        continue;
                    }
                }
            }

            if(this.type == PlacementType.BLOCK)
            {
                Integer meta = this.mapping.get(direction);
                if((meta != null ? meta : -1) != (this.maskMeta & metadata) && this.mapping.size() != 0)
                {
                    continue;
                }
            }

            list.add(direction);
        }

        EnumFacing[] directions = new EnumFacing[list.size()];
        return list.toArray(directions);
    }
}
