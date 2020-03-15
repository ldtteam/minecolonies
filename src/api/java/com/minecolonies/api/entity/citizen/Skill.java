package com.minecolonies.api.entity.citizen;

import org.jetbrains.annotations.Nullable;

/**
 * All possible citizen skills with their complementaries and adversaries.
 */
public enum Skill
{
    Athletics {
        @Override
        Skill getComplimentary()
        {
            return Skill.Strength;
        }

        @Override
        Skill getAdverse()
        {
            return Skill.Dexterity;
        }
    },
    Dexterity {
        @Override
        Skill getComplimentary()
        {
            return Skill.Agility;
        }

        @Override
        Skill getAdverse()
        {
            return Skill.Athletics;
        }
    },
    Strength {
        @Override
        Skill getComplimentary()
        {
            return Skill.Athletics;
        }

        @Override
        Skill getAdverse()
        {
            return Skill.Agility;
        }
    },
    Agility {
        @Override
        Skill getComplimentary()
        {
            return Skill.Dexterity;
        }

        @Override
        Skill getAdverse()
        {
            return Skill.Strength;
        }
    },
    Stamina {
        @Override
        Skill getComplimentary()
        {
            return Skill.Memory;
        }

        @Override
        Skill getAdverse()
        {
            return Skill.Mana;
        }
    },
    Mana {
        @Override
        Skill getComplimentary()
        {
            return Skill.Determination;
        }

        @Override
        Skill getAdverse()
        {
            return Skill.Stamina;
        }
    },
    Adaptability {
        @Override
        Skill getComplimentary()
        {
            return Skill.Creativity;
        }

        @Override
        Skill getAdverse()
        {
            return Skill.Determination;
        }
    },
    Determination {
        @Override
        Skill getComplimentary()
        {
            return Skill.Mana;
        }

        @Override
        Skill getAdverse()
        {
            return Skill.Adaptability;
        }
    },
    Creativity {
        @Override
        Skill getComplimentary()
        {
            return Skill.Adaptability;
        }

        @Override
        Skill getAdverse()
        {
            return Skill.Memory;
        }
    },
    Memory {
        @Override
        Skill getComplimentary()
        {
            return Skill.Stamina;
        }

        @Override
        Skill getAdverse()
        {
            return Skill.Creativity;
        }
    },
    Intelligence {
        @Override
        Skill getComplimentary()
        {
            return null;
        }

        @Override
        Skill getAdverse()
        {
            return null;
        }
    };

    @Nullable
    abstract Skill getComplimentary();

    @Nullable
    abstract Skill getAdverse();
}

