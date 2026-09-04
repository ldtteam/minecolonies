package com.minecolonies.api.blocks;

import com.minecolonies.api.blocks.interfaces.IBlockMinecolonies;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public abstract class AbstractBlockMinecolonies<B extends AbstractBlockMinecolonies<B>> extends Block implements IBlockMinecolonies<B>
{
    private static final ThreadLocal<ResourceKey<Block>> REGISTRATION_ID = new ThreadLocal<>();

    public AbstractBlockMinecolonies(final Properties properties)
    {
        super(properties);
    }

    public static Properties registrationProperties()
    {
        final Properties properties = Properties.of();
        final ResourceKey<Block> key = REGISTRATION_ID.get();
        return key == null ? properties : properties.setId(key);
    }

    public static void beginRegistration(final ResourceKey<Block> key)
    {
        REGISTRATION_ID.set(key);
    }

    public static void endRegistration()
    {
        REGISTRATION_ID.remove();
    }

    @Override
    public void registerBlockItem(final Registry<Item> registry, final Item.Properties properties)
    {
        Registry.register(registry, getRegistryName(), new BlockItem(this, properties));
    }

    @Override
    public B registerBlock(final Registry<Block> registry)
    {
        Registry.register(registry, getRegistryName(), this);
        return (B) this;
    }
}
