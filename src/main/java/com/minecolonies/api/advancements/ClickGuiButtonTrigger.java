package com.minecolonies.api.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger.SimpleInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

import java.util.Optional;

public class ClickGuiButtonTrigger extends SimpleCriterionTrigger<ClickGuiButtonTrigger.ClickGuiButtonTriggerInstance>
{
    /**
     * Triggers the listener checks if there are any listening in
     * 
     * @param player         the player the check regards
     * @param buttonId       the id of the button in blockui
     * @param windowResource the blockui window id to refer to
     */
    public void trigger(final ServerPlayer player, final String buttonId, final String windowResource)
    {
        trigger(player, trigger -> trigger.test(buttonId, windowResource));
    }

    @Override
    public Codec<ClickGuiButtonTriggerInstance> codec()
    {
        return ClickGuiButtonTriggerInstance.CODEC;
    }

    public static record ClickGuiButtonTriggerInstance(Optional<ContextAwarePredicate> player, Optional<String> buttonId, Optional<String> windowResource) implements SimpleInstance
    {
        public static final Codec<ClickGuiButtonTriggerInstance> CODEC = RecordCodecBuilder.create(builder -> builder
            .group(ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(ClickGuiButtonTriggerInstance::player),
                ExtraCodecs.strictOptionalField(Codec.STRING, "button_id").forGetter(ClickGuiButtonTriggerInstance::buttonId),
                ExtraCodecs.strictOptionalField(Codec.STRING, "window_resource_location").forGetter(ClickGuiButtonTriggerInstance::windowResource))
            .apply(builder, ClickGuiButtonTriggerInstance::new));

        public static Criterion<ClickGuiButtonTriggerInstance> clickGuiButton()
        {
            return clickGuiButton(null, null);
        }

        /**
         * Construct the check with a single button condition
         * 
         * @param buttonId the button to be clicked to succeed
         */
        public static Criterion<ClickGuiButtonTriggerInstance> clickGuiButton(final String buttonId)
        {
            return clickGuiButton(buttonId, null);
        }

        /**
         * Construct the check more specifically
         * 
         * @param buttonId       the button to be clicked to succeed
         * @param windowResource the window id of the button to be clicked
         */
        public static Criterion<ClickGuiButtonTriggerInstance> clickGuiButton(final String buttonId, final String windowResource)
        {
            return AdvancementTriggers.CLICK_GUI_BUTTON.get()
                .createCriterion(new ClickGuiButtonTriggerInstance(Optional.empty(),
                    Optional.ofNullable(buttonId),
                    Optional.ofNullable(windowResource)));
        }

        /**
         * Performs the check for the conditions
         * 
         * @param  buttonId       the id of the button that was just clicked
         * @param  windowResource the blockui window id to check
         * @return                whether the check succeeded
         */
        public boolean test(final String buttonId, final String windowResource)
        {
            if (this.buttonId.isPresent() && this.windowResource.isPresent())
            {
                return this.buttonId.get().equalsIgnoreCase(buttonId) && this.windowResource.get().equalsIgnoreCase(windowResource);
            }
            else if (this.buttonId.isPresent())
            {
                return this.buttonId.get().equalsIgnoreCase(buttonId);
            }

            return true;
        }
    }
}
