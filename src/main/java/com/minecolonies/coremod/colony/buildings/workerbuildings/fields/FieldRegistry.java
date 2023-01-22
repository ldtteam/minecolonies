package com.minecolonies.coremod.colony.buildings.workerbuildings.fields;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IFieldView;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.FieldStructureType;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.IField;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Registers the different field class types and links the correct instance back.
 */
public class FieldRegistry
{
    /**
     * Field type storage.
     */
    private static final Map<FieldStructureType, FieldCombination<?, ?>> fields = new EnumMap<>(FieldStructureType.class);

    private FieldRegistry()
    {
    }

    /**
     * Construct a new class for a specific {@link FieldStructureType}.
     *
     * @param type   the field structure type.
     * @param colony the colony this field belongs to.
     * @return the newly created field class.
     */
    public static IField getFieldClassForType(FieldStructureType type, IColony colony)
    {
        initialize();
        return fields.get(type).getField(colony);
    }

    /**
     * Internal method that prefills the field types.
     */
    private static void initialize()
    {
        if (fields.isEmpty())
        {
            fields.put(FieldStructureType.FARMER_FIELDS, new FieldCombination<>(FarmField::new, FarmField.View::new));
            fields.put(FieldStructureType.PLANTATION_FIELDS, new FieldCombination<>(PlantationField::new, PlantationField.View::new));
        }
    }

    /**
     * Construct a new class for a specific {@link FieldStructureType}.
     *
     * @param type   the field structure type.
     * @param colony the colony this field belongs to.
     * @return the newly created field class.
     */
    public static IFieldView getFieldViewClassForType(FieldStructureType type, IColonyView colony)
    {
        initialize();
        return fields.get(type).getFieldView(colony);
    }

    /**
     * @param fieldCreator     The field class generator.
     * @param fieldViewCreator The field view class generator.
     */
    private record FieldCombination<T1 extends IField, T2 extends IFieldView>(Function<IColony, T1> fieldCreator, Function<IColonyView, T2> fieldViewCreator)
    {
        /**
         * Get the field class for this combination.
         *
         * @param colony the colony this field will belong to.
         * @return the field class.
         */
        public T1 getField(IColony colony)
        {
            return fieldCreator.apply(colony);
        }

        /**
         * Get the field view class for this combination.
         *
         * @param colony the colony view this field will belong to.
         * @return the field view class.
         */
        public T2 getFieldView(IColonyView colony)
        {
            return fieldViewCreator.apply(colony);
        }
    }
}
