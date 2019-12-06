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
    private static Map<ITextComponent, Predicate<ICitizenData>> map = new HashMap<>();

    /**
     * Map of all validator predicates.
     */
    private static Map<ITextComponent, Predicate<Tuple<ICitizenData, BlockPos>>> posMap = new HashMap<>();

    /**
     * Get the StandardInteractionValidatorPredicate.
     * @param key the key of it.
     * @return the predicate.
     */
    public static Predicate<ICitizenData> getStandardInteractionValidatorPredicate(final ITextComponent key)
    {
        return map.getOrDefault(key, null);
    }

    /**
     * Get the PosBasedInteractionValidatorPredicate.
     * @param key the key of it.
     * @return the predicate.
     */
    public static Predicate<Tuple<ICitizenData, BlockPos>> getPosBasedInteractionValidatorPredicate(final ITextComponent key)
    {
        return posMap.getOrDefault(key, null);
    }

    /**
     * Add a new StandardInteractionValidatorPredicate.
     * @param key it's key.
     * @param predicate it's predicate.
     */
    public static void addStandardInteractionValidatorPredicate(final ITextComponent key, final Predicate<ICitizenData> predicate)
    {
        map.put(key, predicate);
    }

    /**
     * Add a new PosBasedInteractionValidatorPredicate.
     * @param key it's key.
     * @param predicate it's predicate.
     */
    public static void addPosBasedInteractionValidatorPredicate(final ITextComponent key, final Predicate<Tuple<ICitizenData, BlockPos>> predicate)
    {
        posMap.put(key, predicate);
    }

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
