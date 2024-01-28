package com.minecolonies.api.util;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;
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
     * Hashmap which links a block class + property name to its Property object Used to shorten name searches
     */
    private static final Map<String, Property<?>> propertyBlockMap = new HashMap<>();

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
    public static boolean stateEqualsStateByBlockAndProp(@NotNull final BlockState state1, @NotNull final BlockState state2, @NotNull final String propertyName)
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
     * Compares two states by a property matching the given propertyName. Compared by the name of the Property-value, use when property is an enum without an actual value.
     *
     * @param state1       First state to compare
     * @param state2       Second state to compare
     * @param propertyName the property name we're searching for
     * @return true if states match in the property
     */
    public static boolean stateEqualsStateInPropertyByName(@NotNull final BlockState state1, @NotNull final BlockState state2, @NotNull final String propertyName)
    {
        final Property<?> propertyOne = getPropertyByNameFromState(state1, propertyName);

        if (propertyOne != null && state2.hasProperty(propertyOne))
        {
            return state1.getValue(propertyOne) == state2.getValue(propertyOne);
        }

        final Property<?> propertyTwo = getPropertyByNameFromState(state2, propertyName);

        if (propertyOne != null && propertyTwo != null && state1.hasProperty(propertyOne) && state2.hasProperty(propertyTwo))
        {
            return state1.getValue(propertyOne).toString().equals((state2.getValue(propertyTwo)).toString());
        }
        return false;
    }

    /**
     * Get the property object of a state matching the given name Caches lookups in the propertyBlockMap hashmap
     *
     * @param state Blockstate we're checking for a property
     * @param name  name of the property to find
     * @return the property.
     */
    public static Property<?> getPropertyByNameFromState(@NotNull final BlockState state, @NotNull final String name)
    {
        Property<?> property = propertyBlockMap.get(ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString() + ":" + name);

        if (property != null && state.hasProperty(property))
        {
            return property;
        }
        else
        {
            // Cached map entry nonexistant or wrong, calculate new
            property = getPropertyByName(state.getProperties(), name);

            if (property != null)
            {
                propertyBlockMap.put(ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString() + ":" + name, property);
            }
            return property;
        }
    }

    /**
     * Checks a list of properties for a matching name.
     *
     * @param properties the properties to check
     * @param name       the property name we're looking for
     * @return Property object found
     */
    public static Property<?> getPropertyByName(@NotNull final Collection<Property<?>> properties, @NotNull final String name)
    {
        for (final Property<?> tProperty : properties)
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
     * @param prop   Property to not compare
     * @return true if states are equal without the property
     */
    public static <T extends Comparable<T>> boolean stateEqualsStateWithoutProp(
      @NotNull final BlockState state1,
      @NotNull final BlockState state2,
      @NotNull final Property<T> prop)
    {
        if (!state1.hasProperty(prop) || !state2.hasProperty(prop))
        {
            return state1 == state2;
        }

        return state1.setValue(prop, state2.getValue(prop)) == state2;
    }

    /**
     * Check if two states are equal in Block and Properties
     *
     * @param state1 First state to compare
     * @param state2 Second state to compare
     * @return True if states are equal
     */
    public static boolean stateEqualsStateInBlockAndProp(final BlockState state1, final BlockState state2)
    {
        if (state1 == null || state2 == null)
        {
            return false;
        }

        if (state1.getBlock() != state2.getBlock())
        {
            return false;
        }

        if (state1.getProperties().size() != state2.getProperties().size())
        {
            return false;
        }

        for (final Property<?> prop : state1.getProperties())
        {
            if (!state2.hasProperty(prop))
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
