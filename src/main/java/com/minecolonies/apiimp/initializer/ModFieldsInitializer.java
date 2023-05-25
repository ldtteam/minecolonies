package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.fields.registry.FieldRegistries;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.fields.FarmField;
import com.minecolonies.coremod.colony.fields.PlantationField;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;

public final class ModFieldsInitializer
{
    public static final DeferredRegister<FieldRegistries.FieldEntry> DEFERRED_REGISTER =
      DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "fields"), Constants.MOD_ID);

    static
    {
        FieldRegistries.farmField = DEFERRED_REGISTER.register(FieldRegistries.FARM_FIELD_ID.getPath(),
          () -> new FieldRegistries.FieldEntry(FieldRegistries.FARM_FIELD_ID, FarmField::new));

        FieldRegistries.plantationField = DEFERRED_REGISTER.register(FieldRegistries.PLANTATION_FIELD_ID.getPath(),
          () -> new FieldRegistries.FieldEntry(FieldRegistries.PLANTATION_FIELD_ID, PlantationField::new));
    }
    private ModFieldsInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModFieldsInitializer but this is a Utility class.");
    }
}
