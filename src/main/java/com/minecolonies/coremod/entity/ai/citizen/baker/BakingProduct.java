package com.minecolonies.coremod.entity.ai.citizen.baker;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import org.jetbrains.annotations.NotNull;

public class BakingProduct
{
    /**
     * Tag used to store the state of the product.
     */
    private static final String TAG_STATE = "state";

    /**
     * Baking progress of the bread.
     */
    private static final int FINISHED_BAKING_PROGRESS = 10;

    /**
     * The end product of the BakingProduct.
     */
    private final ItemStack endProduct;

    /**
     * Current state of the product, intantiated at uncrafted.
     */
    private ProductState state = ProductState.UNCRAFTED;

    /**
     * The baking progress of the product.
     */
    private int bakingProgress = 0;

    /**
     * Instantiates the BakingProduct, requires the end product of it.
     *
     * @param endProduct the product when finished.
     */
    public BakingProduct(@NotNull final ItemStack endProduct)
    {
        this.endProduct = endProduct;
    }

    /**
     * Create the product from NBT.
     *
     * @param productCompound the compound to use.
     * @return the restored BakingProduct.
     */
    public static BakingProduct createFromNBT(final CompoundNBT productCompound)
    {
        if (productCompound.keySet().contains(TAG_STATE))
        {
            final ProductState state = ProductState.values()[productCompound.getInt(TAG_STATE)];
            final BakingProduct bakingProduct = new BakingProduct(ItemStack.read(productCompound));
            bakingProduct.setState(state);
            return bakingProduct;
        }
        return null;
    }

    /**
     * Get the current State of the BakingProduct.
     *
     * @return the state.
     */
    public ProductState getState()
    {
        return state;
    }

    /**
     * Set the State of the BakingProduct.
     *
     * @param state the state to set it to.
     */
    private void setState(final ProductState state)
    {
        this.state = state;
    }

    /**
     * Getter for the end product of the bakery.
     *
     * @return the itemStack of the product.
     */
    public ItemStack getEndProduct()
    {
        return endProduct;
    }

    /**
     * Get the Baking progress of the product in the oven.
     *
     * @return an integer between 0-10.
     */
    public int getBakingProgress()
    {
        return bakingProgress;
    }

    /**
     * Increase the baking progress.
     * If reached finish line, this will make the product reach the next state.
     */
    public void increaseBakingProgress()
    {
        this.bakingProgress++;

        if (bakingProgress >= FINISHED_BAKING_PROGRESS)
        {
            this.nextState();
        }
    }

    /**
     * Advance the product 1 state.
     *
     * @return true if possible.
     */
    public boolean nextState()
    {
        if (ProductState.values().length <= state.ordinal())
        {
            return false;
        }
        state = ProductState.values()[state.ordinal() + 1];
        return true;
    }

    /**
     * Write the BakingProduct to NBT.
     *
     * @param productCompound the compound to write it to.
     */
    public void write(final CompoundNBT productCompound)
    {
        productCompound.putInt(TAG_STATE, state.ordinal());
        endProduct.write(productCompound);
    }

    @Override
    public int hashCode()
    {
        int result = state.hashCode();
        result = 31 * result + endProduct.write(new CompoundNBT()).hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof BakingProduct))
        {
            return false;
        }

        final BakingProduct bakingProduct = (BakingProduct) o;

        if (state != bakingProduct.state)
        {
            return false;
        }
        return endProduct.isItemEqual(bakingProduct.endProduct);
    }
}


