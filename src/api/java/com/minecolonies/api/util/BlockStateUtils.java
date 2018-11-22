package com.minecolonies.api.util;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for handling block states and their properties
 */
public class BlockStateUtils
{

    /**
     * Hashmap which links a block class + property name to its IProperty object
     * Used to shorten name searches
     */
    private static final Map<String, IProperty> propertyBlockMap = new HashMap<>();

    /**
     * Private constructor to hide the public one.
     */
    private BlockStateUtils()
    {
        //Hides implicit constructor.
    }

    /**
     * Checks if two states contain the same block and are equal in the given property, or all properties
     *
     * @param state1       First state to compare
     * @param state2       Second state to compare
     * @param propertyName property name to search for
     * @return true or false
     */
    public static boolean stateEqualsStateByBlockAndProp(@NotNull final IBlockState state1, @NotNull final IBlockState state2, @NotNull final String propertyName)
    {
        if (state1.getBlock() != state2.getBlock())
        {
            return false;
        }

        if (stateEqualsStateInPropertyByName(state1, state2, propertyName))
        {
            return true;
        }

        // Compare states in case the property wasn't found
        return state1 == state2;
    }

    /**
     * Compares two states by a property matching the given propertyName.
     * Compared by the name of the Property-value, use when property is an enum without an actual value.
     *
     * @param state1       First state to compare
     * @param state2       Second state to compare
     * @param propertyName the property name we're searching for
     * @return true if states match in the property
     */
    public static boolean stateEqualsStateInPropertyByName(@NotNull final IBlockState state1, @NotNull final IBlockState state2, @NotNull final String propertyName)
    {
        final IProperty propertyOne = getPropertyByNameFromState(state1, propertyName);

        if (propertyOne != null && state2.getPropertyKeys().contains(propertyOne))
        {
            return state1.getValue(propertyOne) == state2.getValue(propertyOne);
        }

        final IProperty propertyTwo = getPropertyByNameFromState(state2, propertyName);

        if (propertyOne != null && propertyTwo != null && state1.getPropertyKeys().contains(propertyOne) && state2.getPropertyKeys().contains(propertyTwo))
        {
            return state1.getValue(propertyOne).toString().equals((state2.getValue(propertyTwo)).toString());
        }
        return false;
    }

    /**
     * Get the property object of a state matching the given name
     * Caches lookups in the propertyBlockMap hashmap
     *
     * @param state Blockstate we're checking for a property
     * @param name  name of the property to find
     */
    public static IProperty getPropertyByNameFromState(@NotNull final IBlockState state, @NotNull final String name)
    {
        IProperty property = propertyBlockMap.get(state.getBlock().getRegistryName().toString() + ":" + name);

        if (property != null && state.getPropertyKeys().contains(property))
        {
            return property;
        }
        else
        {
            // Cached map entry nonexistant or wrong, calculate new
            property = getPropertyByName(state.getPropertyKeys(), name);

            if (property != null)
            {
                propertyBlockMap.put(state.getBlock().getRegistryName().toString() + ":" + name, property);
            }
            return property;
        }
    }

    /**
     * Checks a list of properties for a matching name.
     *
     * @param properties the properties to check
     * @param name       the property name we're looking for
     * @return IProperty object found
     */
    public static IProperty getPropertyByName(@NotNull final Collection<IProperty<?>> properties, @NotNull final String name)
    {
        for (final IProperty tProperty : properties)
        {
            if (tProperty.getName().equals(name))
            {
                return tProperty;
            }
        }
        return null;
    }

    /**
     * Compare two Blockstates ignoring one Property
     *
     * @param state1 First state to compare
     * @param state2 Second state to compare
     * @param prop   IProperty to not compare
     * @return true if states are equal without the property
     */
    public static boolean stateEqualsStateWithoutProp(@NotNull final IBlockState state1, @NotNull final IBlockState state2, @NotNull final IProperty prop)
    {
        if (!state1.getPropertyKeys().contains(prop) || !state2.getPropertyKeys().contains(prop))
        {
            return state1 == state2;
        }

        return state1.withProperty(prop, state2.getValue(prop)) == state2;
    }

    /**
     * Check if two states are equal in Block and Properties
     *
     * @param state1 First state to compare
     * @param state2 Second state to compare
     * @return True if states are equal
     */
    public static boolean stateEqualsStateInBlockAndProp(final IBlockState state1, final IBlockState state2)
    {
        if (state1 == null || state2 == null)
        {
            return false;
        }

        if (state1.getBlock() != state2.getBlock())
        {
            return false;
        }

        if (state1.getPropertyKeys().size() != state2.getPropertyKeys().size())
        {
            return false;
        }

        for (final IProperty prop : state1.getPropertyKeys())
        {
            if (!state2.getPropertyKeys().contains(prop))
            {
                return false;
            }

            if (state1.getValue(prop) != state2.getValue(prop))
            {
                return false;
            }
        }
        return true;
    }
}
