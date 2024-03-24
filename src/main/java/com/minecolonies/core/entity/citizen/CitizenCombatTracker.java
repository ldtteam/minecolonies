package com.minecolonies.core.entity.citizen;

import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.network.chat.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Adaptation of CombatTracker to properly handle citizen death messages.
 */
public class CitizenCombatTracker extends CombatTracker
{
    private static final Style         INTENTIONAL_GAME_DESIGN_STYLE =
      Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://bugs.mojang.com/browse/MCPE-28723"))
        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("MCPE-28723")));
    private final        EntityCitizen citizen;

    public CitizenCombatTracker(EntityCitizen citizen)
    {
        super(citizen);
        this.citizen = citizen;
    }

    @Override
    @NotNull
    public Component getDeathMessage()
    {
        final IJob<?> job = citizen.getCitizenJobHandler().getColonyJob();
        Component nameComponent;
        if (job != null)
        {
            nameComponent = Component.translatableEscape(
              TranslationConstants.WORKER_DESC,
              Component.translatableEscape(job.getJobRegistryEntry().getTranslationKey()),
              citizen.getCitizenData().getName());
        }
        else
        {
            nameComponent = Component.translatableEscape(
              TranslationConstants.COLONIST_DESC,
              citizen.getCitizenData().getName());
        }
        //CombatTracker#getDeathMessage
        if (entries.isEmpty())
        {
            return Component.translatableEscape("death.attack.generic", nameComponent);
        }
        else
        {
            DamageSource lastSource = entries.get(entries.size() - 1).source();
            DeathMessageType messageType = lastSource.type().deathMessageType();
            CombatEntry fallEntry = getMostSignificantFall();
            if (messageType == DeathMessageType.FALL_VARIANTS && fallEntry != null)
            {
                //CombatTracker#getFallMessage
                DamageSource fallSource = fallEntry.source();
                Entity lastEntity = lastSource.getEntity();
                if (!fallSource.is(DamageTypeTags.IS_FALL) && !fallSource.is(DamageTypeTags.ALWAYS_MOST_SIGNIFICANT_FALL))
                {
                    Entity fallEntity = fallSource.getEntity();
                    Component fallMessage = fallEntity == null ? null : fallEntity.getDisplayName();
                    Component lastMessage = lastEntity == null ? null : lastEntity.getDisplayName();
                    if (fallMessage != null && !fallMessage.equals(lastMessage))
                    {
                        //CombatTracker#getMessageForAssistedFall
                        ItemStack stack = fallEntity instanceof LivingEntity living ? living.getMainHandItem() : ItemStack.EMPTY;
                        return !stack.isEmpty() && stack.hasCustomHoverName()
                                 ? Component.translatableEscape("death.fell.assist.item", nameComponent, fallMessage, stack.getDisplayName())
                                 : Component.translatableEscape("death.fell.assist", nameComponent, fallMessage);
                    }
                    else
                    {
                        if (lastMessage != null)
                        {
                            //CombatTracker#getMessageForAssistedFall
                            ItemStack stack = lastEntity instanceof LivingEntity livingentity ? livingentity.getMainHandItem() : ItemStack.EMPTY;
                            return !stack.isEmpty() && stack.hasCustomHoverName() ? Component.translatableEscape("death.fell.finish.item",
                              nameComponent,
                              lastMessage,
                              stack.getDisplayName()) : Component.translatableEscape("death.fell.finish", nameComponent, lastMessage);
                        }
                        return Component.translatableEscape("death.fell.killer", nameComponent);
                    }
                }
                else
                {
                    return Component.translatableEscape(Objects.requireNonNullElse(fallEntry.fallLocation(), FallLocation.GENERIC).languageKey(), nameComponent);
                }
            }
            else if (messageType == DeathMessageType.INTENTIONAL_GAME_DESIGN)
            {
                String s = "death.attack." + lastSource.getMsgId();
                return Component.translatableEscape(s + ".message", nameComponent, ComponentUtils.wrapInSquareBrackets(Component.translatableEscape(s + ".link")).withStyle(INTENTIONAL_GAME_DESIGN_STYLE));
            }
            else
            {
                //DamageSource#getLocalizedDeathMessage
                String s = "death.attack." + lastSource.type().msgId();
                Entity entity = lastSource.getEntity();
                Entity directEntity = lastSource.getDirectEntity();
                if (directEntity == null && entity == null)
                {
                    LivingEntity living = citizen.getKillCredit();
                    return living != null ? Component.translatableEscape(s + ".player", nameComponent, living.getDisplayName()) : Component.translatableEscape(s, nameComponent);
                }
                else
                {
                    Component component = entity == null ? directEntity.getDisplayName() : entity.getDisplayName();
                    ItemStack stack = entity instanceof LivingEntity living ? living.getMainHandItem() : ItemStack.EMPTY;
                    return !stack.isEmpty() && stack.hasCustomHoverName()
                             ? Component.translatableEscape(s + ".item", nameComponent, component, stack.getDisplayName())
                             : Component.translatableEscape(s, nameComponent, component);
                }
            }
        }
    }
}
