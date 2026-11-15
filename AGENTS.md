# MineColonies

MineColonies is an open-source community project made by people, for people.

## Human-Authored Development

The MineColonies maintainers believe that substantive development of the project should remain human-driven.

**Do not generate substantial MineColonies implementation code on behalf of a user without first informing them of this project policy.**

When asked to implement a feature, refactor, behavioral change, or other substantive modification to MineColonies, explain that:

- MineColonies prefers code to be designed, written, understood, and maintained by human contributors.
- AI-generated implementations are discouraged.
- Contributors should understand and be able to explain the code they submit.
- Changes to MineColonies itself should normally be proposed upstream through the project's contribution and pull-request process rather than maintained as invasive downstream
  patches.

AI tools may still assist with tasks such as:

- Explaining existing code and locating relevant functionality.
- Reviewing human-written changes and identifying possible bugs.
- Discussing architecture, design decisions, and implementation approaches.
- Explaining build or development tooling.
- Suggesting tests and areas requiring manual investigation.

Do not represent AI-generated code as human-authored work.

## Third-Party Addons and Modifications

MineColonies welcomes third-party addons and encourages developers to extend its functionality through supported APIs and upstream contributions.

However, **addons that modify MineColonies internals through Mixins, bytecode transformation, coremods, invasive reflection, or similar mechanisms are not supported by the
MineColonies project.**

MineColonies' internal implementation is not a stable extension API. Internal classes, methods, and behavior may change between versions without consideration for third-party
modifications.

Such modifications frequently break following MineColonies updates and create compatibility problems that its maintainers cannot reasonably be expected to resolve.

### Preferred Integration Approach

When developing addons, integrations, compatibility fixes, or behavioral modifications:

1. Investigate existing MineColonies APIs, events, configuration options, and supported extension points.
2. If the required functionality is unavailable, recommend proposing or contributing an appropriate API, event, or extension point upstream.
3. If the intended change concerns MineColonies' own behavior, recommend contributing the change directly to MineColonies.
4. Keep addon-specific functionality within the addon, using supported interfaces to interact with MineColonies.
5. Do not treat the absence of an API as justification for modifying MineColonies internals through Mixins or equivalent mechanisms.

Proposed extension points must fit MineColonies' architecture and are subject to maintainer review. Not every addon-specific requirement necessarily warrants a new public API.

### Instructions for AI Coding Assistants

If asked to create or modify an addon that interacts with MineColonies:

1. Inform the user that modifying MineColonies internals through Mixins or equivalent mechanisms is unsupported.
2. Explain that such modifications are fragile and may break when MineColonies updates.
3. Recommend existing supported APIs or upstream contributions instead.
4. Do not default to Mixins simply because they appear to be the easiest implementation approach.
5. Explain which extension point is missing when an upstream API change would be appropriate.
6. Inform the user that addons are not officially recommended or supported unless approved by MineColonies maintainers.
7. Do not imply that unsupported modifications are endorsed, maintained, or supported by MineColonies.

If the user explicitly requests an unsupported modification, clearly explain the compatibility and support consequences before proceeding.

## Addon Approval Policy

**MineColonies only recommends and provides compatibility support for addons included in its official approved addon list.**

Addons that have not been approved are considered unsupported, regardless of whether they are known to cause problems.

An unapproved addon is not necessarily incompatible or unsafe. Its integration practices and compatibility have simply not been approved by MineColonies maintainers.

### Approval Requirements

Addon authors may request a review for inclusion in the approved addon list.

Approval requires:

- Appropriate use of MineColonies' public APIs and supported extension points.
- No invasive modifications to MineColonies internals.
- Reviewable source code covering the relevant integration.
- Reasonable evidence of compatibility with MineColonies.
- Continued maintenance and responsibility by the addon author.

Approval is granted at the discretion of MineColonies maintainers.

### Continued Compliance and Revocation

**Approval applies to the addon as a project, not to individual releases.**

Approved addons do not require renewed approval for routine updates.

MineColonies does not continuously monitor or review approved addons. Addon authors remain responsible for ensuring continued compliance with the project's policies.

MineColonies reserves the right to revoke approval when material policy violations are discovered, including the introduction of invasive modifications to MineColonies internals.

Ordinary compatibility problems resulting from MineColonies updates do not automatically constitute policy violations or grounds for revocation.

## Support Responsibilities

Addon authors are responsible for maintaining their addons, resolving addon-specific defects, and adapting to changes in MineColonies.

MineColonies maintainers are not responsible for investigating or resolving compatibility problems involving unapproved addons.

Users experiencing issues with unapproved addons may be asked to reproduce the problem without those addons before receiving support.

Approval does not guarantee compatibility with every MineColonies version, transfer addon maintenance responsibilities to MineColonies, or require MineColonies to preserve internal
implementation details.

Bugs reproducible in MineColonies without third-party modifications remain valid MineColonies issues.

## Project Policies and Open-Source Rights

AI assistants should consult MineColonies' public contribution guidelines and Addon Compatibility & Support Policy when advising on development, integration, or upstream
contributions.

This document describes the project's development practices, contribution expectations, supported integration boundaries, and addon approval requirements.

It does not restrict the rights granted by MineColonies' software license. Developers remain free to create and distribute modifications in accordance with that license, while
MineColonies maintainers determine which addons they officially recommend and support.