package com.minecolonies.api.tileentities.storageblocks.registry;

import com.minecolonies.api.tileentities.storageblocks.IStorageBlockInterface;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Predicate;

public final class StorageBlockEntry
{
    /**
     * The registry identifier for this storage block.
     */
    private final ResourceLocation registryName;

    /**
     * Predicate to determine whether a given BlockEntity
     * is this storage block type.
     */
    private final Predicate<BlockEntity> isStorageBlock;

    /**
     * The interface that will be used to interact with the particular block.
     */
    private final IStorageBlockInterface storageInterface;

    /**
     * Constructor
     *
     * @param registryName        The registry name of this entry
     * @param isStorageBlock      The predicate to determine if a block is this storage block type
     * @param storageInterface    The interface used to interact with the particular block
     */
    public StorageBlockEntry(ResourceLocation registryName, Predicate<BlockEntity> isStorageBlock, IStorageBlockInterface storageInterface)
    {
        this.registryName = registryName;
        this.isStorageBlock = isStorageBlock;
        this.storageInterface = storageInterface;
    }

    /**
     * Check whether a particular blockentity is this type of storageblock
     *
     * @param blockEntity The block entity to check
     * @return Whether the blockentity matches or not.
     */
    public boolean matches(final BlockEntity blockEntity)
    {
        return isStorageBlock.test(blockEntity);
    }

    /**
     * Get the storageinterface that knows how to interact with this block type.
     *
     * @return The interface.
     */
    public IStorageBlockInterface getStorageInterface()
    {
        return storageInterface;
    }

    public static class Builder
    {
        /**
         * The registry identifier for this storage block.
         */
        private ResourceLocation registryName;

        /**
         * Predicate to determine whether a given BlockEntity
         * is this storage block type.
         */
        private Predicate<BlockEntity> isStorageBlock;

        /**
         * The interface that will be used to interact with the particular block.
         */
        private IStorageBlockInterface storageInterface;

        /**
         * Set the registry name for the StorageBlockEntry being built.
         *
         * @param registryName The new registry name
         * @return this
         */
        public Builder setRegistryName(ResourceLocation registryName)
        {
            this.registryName = registryName;
            return this;
        }

        /**
         * Set the predicate to check if a block is the given storage block type
         * for the StorageBlockEntry being built.
         *
         * @param isStorageBlock The new predicate
         * @return this
         */
        public Builder setIsStorageBlock(Predicate<BlockEntity> isStorageBlock)
        {
            this.isStorageBlock = isStorageBlock;
            return this;
        }

        /**
         * Set the interface that will be used to interact with blocks of this kind.
         *
         * @param storageInterface The interface
         * @return this
         */
        public Builder setStorageInterface(IStorageBlockInterface storageInterface)
        {
            this.storageInterface = storageInterface;
            return this;
        }

        /**
         * Build the StorageBlockEntry.
         *
         * @return the new StorageBlockEntry
         */
        public StorageBlockEntry build()
        {
            return new StorageBlockEntry(registryName, isStorageBlock, storageInterface);
        }
    }
}
