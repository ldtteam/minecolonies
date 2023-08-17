package com.minecolonies.coremod.colony.buildings.modules.settings;

import com.ldtteam.blockui.Loader;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.View;
import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.settings.ISetting;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.client.gui.WindowSelectRes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
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
      final IBuildingView building, final BOWindow window)
    {

        Loader.createFromXMLFile(new ResourceLocation("minecolonies:gui/layouthuts/layoutblocksetting.xml"), (View) pane);
        pane.findPaneOfTypeByID("id", Text.class).setText(Component.literal(key.getUniqueId().toString()));
        pane.findPaneOfTypeByID("desc", Text.class).setText(Component.translatable("com.minecolonies.coremod.setting." + key.getUniqueId().toString()));

        pane.findPaneOfTypeByID("trigger", ButtonImage.class).setHandler(button -> new WindowSelectRes(
          window,
          (stack) -> {
              final Item item = stack.getItem();
              if (!( item instanceof BlockItem ))
              {
                  return false;
              }

              final Block block = ((BlockItem) item).getBlock();
              final BlockState state = block.defaultBlockState();
              if (block instanceof EntityBlock || block instanceof FallingBlock || state.is(BlockTags.LEAVES))
              {
                  return false;
              }

              return block.getShape(state, new SingleStateBlockGetter(state), BlockPos.ZERO, CollisionContext.empty()).equals(Shapes.block()) && state.getMaterial().blocksMotion();
          }, (stack, qty) -> {
              if (stack.isEmpty())
              {
                  return;
              }
            value = (BlockItem) stack.getItem();
            settingsModuleView.getSetting(new SettingKey(key.getType(), key.getUniqueId())).updateSetting(this);
            settingsModuleView.trigger(key);
        }, false).open());
    }

    @Override
    public void render(final ISettingKey<?> key, final Pane pane, final ISettingsModuleView settingsModuleView, final IBuildingView building, final BOWindow window)
    {
        pane.findPaneOfTypeByID("icon", ItemIcon.class).setItem(new ItemStack(value));
        pane.findPaneOfTypeByID("trigger", ButtonImage.class).setText(Component.translatable(SWITCH));
    }

    @Override
    public void trigger()
    {

    }

    @Override
    public void copyValue(final ISetting iSetting)
    {
        if (iSetting instanceof final BlockSetting other)
        {
            setValue(other.getValue());
        }
    }

    /**
     * Special block getter for shapes.
     */
    public class SingleStateBlockGetter implements BlockGetter
    {
        private final BlockState state;

        public SingleStateBlockGetter(BlockState state)
        {
            this.state = state;
        }

        @Nullable
        @Override
        public BlockEntity getBlockEntity(@NotNull BlockPos pos)
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

        @Override
        public int getHeight()
        {
            return Integer.MAX_VALUE;
        }

        @Override
        public int getMinBuildHeight()
        {
            return Integer.MIN_VALUE;
        }
    }
}
