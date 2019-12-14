package com.minecolonies.api.advancements.create_build_request;

import com.ldtteam.structurize.management.StructureName;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.util.ResourceLocation;

public class CreateBuildRequestCriterionInstance extends AbstractCriterionInstance
{
    private String hutName;
    private StructureName structureName;
    private int level = -1;

    public CreateBuildRequestCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CREATE_BUILD_REQUEST));
    }

    public CreateBuildRequestCriterionInstance(final StructureName structureName)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CREATE_BUILD_REQUEST));

        this.structureName = structureName;
    }

    public CreateBuildRequestCriterionInstance(final String hutName)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CREATE_BUILD_REQUEST));

        this.hutName = hutName;
    }

    public CreateBuildRequestCriterionInstance(final StructureName structureName, final int level)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CREATE_BUILD_REQUEST));

        this.structureName = structureName;
        this.level = level;
    }

    public CreateBuildRequestCriterionInstance(final String hutName, final int level)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CREATE_BUILD_REQUEST));

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
}
