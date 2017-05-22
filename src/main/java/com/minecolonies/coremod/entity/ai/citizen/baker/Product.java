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
     * Recipe id of the product.
     */
    private static final String TAG_RECIPE_ID = "recipeId";

    /**
     * Baking progress of the bread.
     */
    private static final int FINISHED_BAKING_PROGRESS = 10;

    /**
     * Current state of the product, intantiated at raw.
     */
    private ProductState state = ProductState.UNCRAFTED;

    /**
     * The end product of the Product.
     */
    private final ItemStack endProduct;

    /**
     * The baking progress of the product.
     */
    private int bakingProgress = 0;

    /**
     * The recipe id of the product.
     */
    private int recipeId = 0;

    /**
     * Instantiates the Product, requires the end product of it.
     * @param endProduct the product when finished.
     */
    public Product(final ItemStack endProduct, int recipeId)
    {
        this.endProduct = endProduct;
        this.recipeId = recipeId;
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
     * Get the Baking progress of the product in the oven.
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

        if(bakingProgress >= FINISHED_BAKING_PROGRESS)
        {
            this.nextState();
        }
    }

    /**
     * Getter for the recipe id of the product.
     * @return the id.
     */
    public int getRecipeId()
    {
        return recipeId;
    }

    /**
     * Create the product from NBT.
     * @param productCompound the compound to use.
     * @return the restored Product.
     */
    public static Product createFromNBT(final NBTTagCompound productCompound)
    {
        final ProductState state = ProductState.values()[productCompound.getInteger(TAG_STATE)];
        final int recipeId = productCompound.getInteger(TAG_RECIPE_ID);
        final Product product = new Product(ItemStack.loadItemStackFromNBT(productCompound), recipeId);
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
        productCompound.setInteger(TAG_RECIPE_ID, recipeId);
        endProduct.writeToNBT(productCompound);
    }

    public enum ProductState
    {
        UNCRAFTED,
        RAW,
        PREPARED,
        BAKING,
        BAKED
    }
}


