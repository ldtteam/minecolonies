package com.minecolonies.coremod.blocks.decorative;

import com.minecolonies.coremod.blocks.interfaces.IBlockMinecolonies;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;
import org.jetbrains.annotations.NotNull;

public interface IBlockConstructionTape<B extends IBlockConstructionTape<B>> extends IBlockMinecolonies<B>
{
    /**
     * The variants of the shingle slab.
     */
    PropertyEnum<ConstructionTapeType> VARIANT = PropertyEnum.create("variant", ConstructionTapeType.class);
    /**
     * The position it faces.
     */
    PropertyDirection                  FACING  = BlockHorizontal.FACING;

    /**
     * Types that the {@link IBlockConstructionTape} supports
     */
    public enum ConstructionTapeType implements IStringSerializable
    {
        STRAIGHT(0, "straight", MapColor.WOOD),
        CORNER(1, "corner", MapColor.OBSIDIAN);

        private static final ConstructionTapeType[] META_LOOKUP = new ConstructionTapeType[values().length];
        static
        {
            for (final ConstructionTapeType enumtype : values())
            {
                META_LOOKUP[enumtype.getMetadata()] = enumtype;
            }
        }
        private final int      meta;
        private final String   name;
        private final String   unlocalizedName;
        /**
         * The color that represents this entry on a map.
         */
        private final MapColor mapColor;

        ConstructionTapeType(final int metaIn, final String nameIn, final MapColor mapColorIn)
        {
            this(metaIn, nameIn, nameIn, mapColorIn);
        }

        ConstructionTapeType(final int metaIn, final String nameIn, final String unlocalizedNameIn, final MapColor mapColorIn)
        {
            this.meta = metaIn;
            this.name = nameIn;
            this.unlocalizedName = unlocalizedNameIn;
            this.mapColor = mapColorIn;
        }

        public static ConstructionTapeType byMetadata(final int meta)
        {
            int tempMeta = meta;
            if (tempMeta < 0 || tempMeta >= META_LOOKUP.length)
            {
                tempMeta = 0;
            }

            return META_LOOKUP[tempMeta];
        }

        public int getMetadata()
        {
            return this.meta;
        }

        /**
         * The color which represents this entry on a map.
         */
        public MapColor getMapColor()
        {
            return this.mapColor;
        }

        @Override
        public String toString()
        {
            return this.name;
        }

        @NotNull
        public String getName()
        {
            return this.name;
        }

        public String getTranslationKey()
        {
            return this.unlocalizedName;
        }
    }
}
