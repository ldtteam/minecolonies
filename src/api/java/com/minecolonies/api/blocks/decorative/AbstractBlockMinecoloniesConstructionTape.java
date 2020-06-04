package com.minecolonies.api.blocks.decorative;

import com.minecolonies.api.blocks.AbstractBlockMinecoloniesFalling;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.util.IStringSerializable;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractBlockMinecoloniesConstructionTape<B extends AbstractBlockMinecoloniesConstructionTape<B>>
    extends AbstractBlockMinecoloniesFalling<B>
{
    /**
     * The variants of the shingle slab.
     */
    public static final EnumProperty<ConstructionTapeType> VARIANT = EnumProperty.create("variant", ConstructionTapeType.class);

    /**
     * The position it faces.
     */
    public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;

    public AbstractBlockMinecoloniesConstructionTape(final Properties properties)
    {
        super(properties);
    }

    /**
     * Types that the {@link AbstractBlockMinecoloniesConstructionTape} supports
     */
    public enum ConstructionTapeType implements IStringSerializable
    {
        STRAIGHT(0, "straight", MaterialColor.WOOD),
        CORNER(1, "corner", MaterialColor.OBSIDIAN);

        private static final ConstructionTapeType[] META_LOOKUP = new ConstructionTapeType[values().length];
        static
        {
            for (final ConstructionTapeType enumtype : values())
            {
                META_LOOKUP[enumtype.getMetadata()] = enumtype;
            }
        }
        private final int meta;
        private final String name;
        private final String unlocalizedName;
        /**
         * The color that represents this entry on a map.
         */
        private final MaterialColor mapColor;

        ConstructionTapeType(final int metaIn, final String nameIn, final MaterialColor mapColorIn)
        {
            this(metaIn, nameIn, nameIn, mapColorIn);
        }

        ConstructionTapeType(final int metaIn, final String nameIn, final String unlocalizedNameIn, final MaterialColor mapColorIn)
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
         * 
         * @return the material color.
         */
        public MaterialColor getMaterialColor()
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
