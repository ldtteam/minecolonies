package com.minecolonies.api.advancements.complete_build_request;

import com.ldtteam.structurize.management.StructureName;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class CompleteBuildRequestCriterionInstance extends CriterionInstance
{
    private String        hutName;
    private StructureName structureName;
    private int           level = -1;
    private int           minGroundDistance = -1;

    public CompleteBuildRequestCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_COMPLETE_BUILD_REQUEST));
    }

    public CompleteBuildRequestCriterionInstance(final StructureName structureName)
    {
        this();

        this.structureName = structureName;
    }

    public CompleteBuildRequestCriterionInstance(final String hutName)
    {
        this();

        this.hutName = hutName;
    }

    public CompleteBuildRequestCriterionInstance(final StructureName structureName, final int level)
    {
        this();

        this.structureName = structureName;
        this.level = level;
    }

    public CompleteBuildRequestCriterionInstance(final String hutName, final int level)
    {
        this();

        this.hutName = hutName;
        this.level = level;
    }

    public boolean test(final StructureName structureName, final int level)
    {
        if (this.hutName != null && this.level != -1)
        {
            return this.hutName.equalsIgnoreCase(structureName.getHutName()) && this.level <= level;
        }
        else if (this.hutName != null)
        {
            return this.hutName.equalsIgnoreCase(structureName.getHutName());
        }

        if (this.structureName != null && this.level != -1)
        {
            return this.structureName.equals(structureName) && this.level <= level;
        }
        else if (this.structureName != null)
        {
            return this.structureName.equals(structureName);
        }

        return true;
    }

    public boolean test(final StructureName structureName, final int level, final int y)
    {
        return test(structureName, level) && y >= this.minGroundDistance;
    }
}
