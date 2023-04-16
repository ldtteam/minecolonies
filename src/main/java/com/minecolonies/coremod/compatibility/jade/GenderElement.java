package com.minecolonies.coremod.compatibility.jade;

import com.ldtteam.blockui.UiRenderMacros;
import com.minecolonies.api.util.constant.WindowConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IElement;

/**
 * Renders a colonist's gender.
 */
public class GenderElement extends Element
{
    private static final GenderElement MALE = new GenderElement(WindowConstants.FEMALE_SOURCE.replace("female", "male"));
    private static final GenderElement FEMALE = new GenderElement(WindowConstants.FEMALE_SOURCE);

    /**
     * Gets a {@link GenderElement} for the specified gender.
     * @param isFemale true for female, false for male.
     * @return the corresponding element.
     */
    public static IElement get(final boolean isFemale)
    {
        return isFemale ? FEMALE : MALE;
    }

    private final ResourceLocation textureLocation;

    private GenderElement(final String texture)
    {
        this.textureLocation = new ResourceLocation(texture);
    }

    @NotNull
    @Override
    public Vec2 getSize()
    {
        return new Vec2(15, 15);
    }

    @Override
    public void render(@NotNull final PoseStack poseStack,
                       final float x, final float y, final float maxX, final float maxY)
    {
        UiRenderMacros.blit(poseStack, this.textureLocation, (int) x, (int) y, 15, 15, 0, 0, 30, 30, 30, 30);
    }

    @Nullable
    @Override
    public Component getMessage()
    {
        return Component.translatable("com.minecolonies.coremod.jade.gender." + (this == FEMALE ? "female" : "male"));
    }
}
