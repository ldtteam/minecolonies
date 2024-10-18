package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.entity.visitor.IVisitorType;
import com.minecolonies.api.entity.visitor.ModVisitorTypes;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.entity.visitor.ExpeditionaryVisitorType;
import com.minecolonies.core.entity.visitor.RegularVisitorType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;

/**
 * Initializer for the {@link ModVisitorTypes}.
 */
public class ModVisitorTypesInitializer
{
    public static final DeferredRegister<IVisitorType>
      DEFERRED_REGISTER = DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "visitortypes"), Constants.MOD_ID);
    static
    {
        ModVisitorTypes.visitor = DEFERRED_REGISTER.register(ModVisitorTypes.VISITOR_TYPE_ID.getPath(), RegularVisitorType::new);
        ModVisitorTypes.expeditionary = DEFERRED_REGISTER.register(ModVisitorTypes.EXPEDITIONARY_VISITOR_TYPE_ID.getPath(), ExpeditionaryVisitorType::new);
    }
    private ModVisitorTypesInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModVisitorTypesInitializer but this is a Utility class.");
    }
}