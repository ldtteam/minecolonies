package com.minecolonies.api.entity.visitor;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry holder for the visitor types.
 */
public class ModVisitorTypes
{
    /**
     * Resource ids.
     */
    public static final ResourceLocation VISITOR_TYPE_ID               = new ResourceLocation(Constants.MOD_ID, "visitor");
    public static final ResourceLocation EXPEDITIONARY_VISITOR_TYPE_ID = new ResourceLocation(Constants.MOD_ID, "expeditionary");

    /**
     * Registry objects.
     */
    public static RegistryObject<IVisitorType> visitor;
    public static RegistryObject<IVisitorType> expeditionary;
}