package com.minecolonies.api.colony.interactionhandling;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Utility class to store all validator predicates for the chat handling.
 */
public final class InteractionValidatorPredicates
{
    /**
     * Map of all validator predicates.
     */
    public static Map<ITextComponent, Predicate<ICitizenData>> map = new HashMap<>();

    /**
     * Map of all validator predicates.
     */
    public static Map<ITextComponent, Predicate<Tuple<ICitizenData, BlockPos>>> posMap = new HashMap<>();

    /**
     * Private constructor to hide public one.
     */
    private InteractionValidatorPredicates()
    {
        /*
         * Intentionally left empty.
         */
    }

}
