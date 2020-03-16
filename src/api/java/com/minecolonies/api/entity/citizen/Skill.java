package com.minecolonies.api.entity.citizen;

import org.jetbrains.annotations.Nullable;

/**
 * All possible citizen skills with their complementaries and adversaries.
 */
public enum Skill
{
    Athletics
      {
          @Override
          public Skill getComplimentary()
          {
              return Skill.Strength;
          }

          @Override
          public Skill getAdverse()
          {
              return Skill.Dexterity;
          }
      },
    Dexterity
      {
          @Override
          public Skill getComplimentary()
          {
              return Skill.Agility;
          }

          @Override
          public Skill getAdverse()
          {
              return Skill.Athletics;
          }
      },
    Strength
      {
          @Override
          public Skill getComplimentary()
          {
              return Skill.Athletics;
          }

          @Override
          public Skill getAdverse()
          {
              return Skill.Agility;
          }
      },
    Agility
      {
          @Override
          public Skill getComplimentary()
          {
              return Skill.Dexterity;
          }

          @Override
          public Skill getAdverse()
          {
              return Skill.Strength;
          }
      },
    Stamina
      {
          @Override
          public Skill getComplimentary()
          {
              return Skill.Knowledge;
          }

          @Override
          public Skill getAdverse()
          {
              return Skill.Mana;
          }
      },
    Mana
      {
          @Override
          public Skill getComplimentary()
          {
              return Skill.Focus;
          }

          @Override
          public Skill getAdverse()
          {
              return Skill.Stamina;
          }
      },
    Adaptability
      {
          @Override
          public Skill getComplimentary()
          {
              return Skill.Creativity;
          }

          @Override
          public Skill getAdverse()
          {
              return Skill.Focus;
          }
      },
    Focus
      {
          @Override
          public Skill getComplimentary()
          {
              return Skill.Mana;
          }

          @Override
          public Skill getAdverse()
          {
              return Skill.Adaptability;
          }
      },
    Creativity
      {
          @Override
          public Skill getComplimentary()
          {
              return Skill.Adaptability;
          }

          @Override
          public Skill getAdverse()
          {
              return Skill.Knowledge;
          }
      },
    Knowledge
      {
          @Override
          public Skill getComplimentary()
          {
              return Skill.Stamina;
          }

          @Override
          public Skill getAdverse()
          {
              return Skill.Creativity;
          }
      },
    Intelligence
      {
          @Override
          public Skill getComplimentary()
          {
              return null;
          }

          @Override
          public Skill getAdverse()
          {
              return null;
          }
      };

    @Nullable
    public abstract Skill getComplimentary();

    @Nullable
    public abstract Skill getAdverse();
}

