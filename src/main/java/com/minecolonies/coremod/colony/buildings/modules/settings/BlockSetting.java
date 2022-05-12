package com.minecolonies.coremod.colony.buildings.modules.settings;

import com.ldtteam.blockout.Loader;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.ButtonImage;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.View;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.buildings.modules.settings.ISetting;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.client.gui.WindowSelectRes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Stores a solid block setting.
 */
public class BlockSetting implements ISetting
{
    /**
     * The value of the setting.
     */
    private BlockItem value;

    /**
     * Default value of the setting.
     */
    private BlockItem defaultValue;

    /**
     * Create a new boolean setting.
     *
     * @param init the initial value.
     */
    public BlockSetting(final BlockItem init)
    {
        this.value = init;
        this.defaultValue = init;
    }

    /**
     * Create a new boolean setting.
     *
     * @param value the value.
     * @param def   the default value.
     */
    public BlockSetting(final BlockItem value, final BlockItem def)
    {
        this.value = value;
        this.defaultValue = def;
    }

    /**
     * Get the setting value.
     *
     * @return the set value.
     */
    public BlockItem getValue()
    {
        return value;
    }

    /**
     * Get the default value.
     *
     * @return the default value.
     */
    public BlockItem getDefault()
    {
        return defaultValue;
    }

    /**
     * Set a new block value.
     *
     * @param value the itemblock to set.
     */
    public void setValue(final BlockItem value)
    {
        this.value = value;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void setupHandler(
      final ISettingKey<?> key,
      final Pane pane,
      final ISettingsModuleView settingsModuleView,
      final IBuildingView building, final Window window)
    {

        Loader.createFromXMLFile("minecolonies:gui/layouthuts/layoutblocksetting.xml", (View) pane);
        pane.findPaneOfTypeByID("id", Text.class).setText(key.getUniqueId().toString());
        pane.findPaneOfTypeByID("desc", Text.class).setText(new TranslationTextComponent("com.minecolonies.coremod.setting." + key.getUniqueId().toString()));

        pane.findPaneOfTypeByID("trigger", ButtonImage.class).setHandler(button -> new WindowSelectRes(
          window,
          building,
          (stack) -> {
              final Item item = stack.getItem();
              if (!( item instanceof BlockItem ))
              {
                  return false;
              }

              final Block block = ((BlockItem) item).getBlock();
              final BlockState state = block.defaultBlockState();
              if (block.hasTileEntity(state) || block instanceof FallingBlock || state.is(BlockTags.LEAVES))
              {
                  return false;
              }

              return block.getShape(state, new SingleStateBlockGetter(state), BlockPos.ZERO, ISelectionContext.empty()).equals(VoxelShapes.block()) && state.getMaterial().blocksMotion();
          }, (stack, qty) -> {
            value = (BlockItem) stack.getItem();
            settingsModuleView.trigger(key);
        }, false).open());
    }

    @Override
    public void render(final ISettingKey<?> key, final Pane pane, final ISettingsModuleView settingsModuleView, final IBuildingView building, final Window window)
    {
        pane.findPaneOfTypeByID("icon", ItemIcon.class).setItem(new ItemStack(value));
        pane.findPaneOfTypeByID("trigger", ButtonImage.class).setText(new TranslationTextComponent(SWITCH));
    }

    @Override
    public void trigger()
    {

    }

    /**
     * Special block getter for shapes.
     */
    public class SingleStateBlockGetter implements IBlockReader
    {
        private final BlockState state;

        public SingleStateBlockGetter(BlockState state)
        {
            this.state = state;
        }

        @Nullable
        @Override
        public TileEntity getBlockEntity(@NotNull BlockPos pos)
        {
            return null;
        }

        @NotNull
        @Override
        public BlockState getBlockState(@NotNull BlockPos pos)
        {
            if (pos == BlockPos.ZERO)
                return state;
            return Blocks.AIR.defaultBlockState();
        }

        @NotNull
        @Override
        public FluidState getFluidState(@NotNull BlockPos pos)
        {
            return Fluids.EMPTY.defaultFluidState();
        }
    }
}
