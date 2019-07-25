package com.minecolonies.api.util;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Stores a blockstate for comparing.
 */
public class BlockStateStorage
{
    /**
     * The state to store.
     */
    private final BlockState state;

    /**
     * List of properties used to compare
     */
    private final List<IProperty> propertyList;

    /**
     * Hashcode of the storage.
     */
    private int hashCode;

    /**
     * True: states are compared ignoring the properties in the given propertyList.
     * False: states are only compared within the properties on the propertyList.
     */
    private final boolean exclude;

    /**
     * Create an instance of the storage.
     *
     * @param state             The blockstate to store
     * @param compareProperties the list of properties to compare
     * @param exclude           True: states are compared ignoring the properties in the given list.
     *                          False: states are only compared within the properties on the list.
     */
    public BlockStateStorage(@NotNull final BlockState state, @NotNull final List<IProperty> compareProperties, final boolean exclude)
    {
        this.state = state;
        this.propertyList = compareProperties;
        this.exclude = exclude;

        // Calculating the hashcode once
        hashCode = state.getBlock().hashCode();

        if (!exclude)
        {
            // hashcode only for included properties
            for (final IProperty prop : compareProperties)
            {
                if (state.getPropertyKeys().contains(prop))
                {
                    hashCode += prop.hashCode();
                    hashCode += state.getValue(prop).hashCode();
                }
            }
        }
        else
        {
            // hashcode for all except the excluded properties
            for (final IProperty prop : state.getPropertyKeys())
            {
                if (!compareProperties.contains(prop))
                {
                    hashCode += prop.hashCode();
                    hashCode += state.getValue(prop).hashCode();
                }
            }
        }
    }

    /**
     * Returns the stored state
     *
     * @return state
     */
    public BlockState getState()
    {
        return state;
    }

    /**
     * Gets the list of the properties this storage uses to compare storages.
     *
     * @return property list
     */
    public List<IProperty> getCompareProperties()
    {
        return propertyList;
    }

    @Override
    public int hashCode()
    {
        return hashCode;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final BlockStateStorage comparingToStorage = (BlockStateStorage) o;

        if (comparingToStorage.getState() == state)
        {
            return true;
        }

        if (exclude)
        {
            for (final IProperty prop : state.getPropertyKeys())
            {
                // skip excluded properties upon comparing
                if (getCompareProperties().contains(prop))
                {
                    continue;
                }

                if (!comparingToStorage.getState().getPropertyKeys().contains(prop))
                {
                    return false;
                }

                if (!comparingToStorage.getState().getValue(prop).equals(state.getValue(prop)))
                {
                    return false;
                }
            }
        }
        else
        {
            for (final IProperty prop : propertyList)
            {
                if (!comparingToStorage.getState().getPropertyKeys().contains(prop))
                {
                    return false;
                }

                if (!comparingToStorage.getState().getValue(prop).equals(state.getValue(prop)))
                {
                    return false;
                }
            }
        }

        return true;
    }
}
