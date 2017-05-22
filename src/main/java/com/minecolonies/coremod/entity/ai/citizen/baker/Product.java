package com.minecolonies.coremod.entity.ai.citizen.baker;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class Product
{
    /**
     * Tag used to store the state of the product.
     */
    private static final String TAG_STATE = "state";

    /**
     * Current state of the product, intantiated at raw.
     */
    private ProductState state         = ProductState.RAW;

    /**
     * The end product of the Product.
     */
    private final ItemStack endProduct;

    /**
     * Instantiates the Product, requires the end product of it.
     * @param endProduct the product when finished.
     */
    public Product(final ItemStack endProduct)
    {
        this.endProduct = endProduct;
    }

    /**
     * Get the current State of the Product.
     * @return the state.
     */
    public ProductState getState()
    {
        return state;
    }

    /**
     * Advance the product 1 state.
     * @return true if possible.
     */
    public boolean nextState()
    {
        if(ProductState.values().length <= state.ordinal())
        {
            return false;
        }
        state = ProductState.values()[state.ordinal()+1];
        return true;
    }

    /**
     * Set the State of the Product.
     * @param state the state to set it to.
     */
    private void setState(final ProductState state)
    {
        this.state = state;
    }

    /**
     * Getter for the end product of the bakery.
     * @return the itemStack of the product.
     */
    public ItemStack getEndProduct()
    {
        return endProduct;
    }

    /**
     * Create the product from NBT.
     * @param productCompound the compound to use.
     * @return the restored Product.
     */
    public static Product createFromNBT(final NBTTagCompound productCompound)
    {
        final ProductState state = ProductState.values()[productCompound.getInteger(TAG_STATE)];
        final Product product = new Product(ItemStack.loadItemStackFromNBT(productCompound));
        product.setState(state);
        return product;
    }

    /**
     * Write the Product to NBT.
     * @param productCompound the compound to write it to.
     */
    public void writeToNBT(final NBTTagCompound productCompound)
    {
        productCompound.setInteger(TAG_STATE, state.ordinal());
        endProduct.writeToNBT(productCompound);
    }

    public enum ProductState
    {
        RAW,
        PREPARED,
        BAKED
    }
}


