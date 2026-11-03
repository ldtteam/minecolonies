# Contributing to MineColonies

Thank you for your interest in contributing! Please read this page before submitting a pull request.

## Before you start

- Please create a GitHub issue before submitting a pull request.
- All pull request authors must sign our CLA: https://cla-assistant.io/Minecolonies/minecolonies

## Reporting bugs

Not a developer, or not confident enough to submit a fix yourself? A detailed, reproducible bug report is one of the most valuable contributions you can make. It saves developers the time they'd otherwise spend tracking the problem down, and makes it far more likely the bug actually gets fixed.

A good bug report includes:

- **A clear description of the problem** — what you expected to happen and what actually happened.
- **Exact reproduction steps** — the minimal sequence of actions that reliably triggers the bug.
- **Your environment** — MineColonies version, Minecraft version, mod loader, and any other relevant mods.
- **Logs** — attach the full latest.log or crash report from your `.minecraft/logs` folder.
- **A minimal reproduction** — if possible, a save or modpack that isolates the issue. The simpler, the better.

Please search the [existing issues](https://github.com/ldtteam/minecolonies/issues) before opening a new one to avoid duplicates.

## Submitting a PR

Found a bug in our code? Think you can make it more efficient? Want to help in general? Great!

1. If you haven't already, create a GitHub account.
2. Click the `Fork` icon located at the top right.
3. Make your changes and commit them.
    * If you're making changes locally, you'll have to do `git commit -a` and `git push` in your command line (or with GitKraken stage the changes, commit them, and push them first).
4. Click `Pull Request` in the middle.
5. Click `New pull request` to create a pull request for this comparison, enter your PR's title, and create a detailed description telling us what you changed.
6. Click `Create pull request` and wait for feedback!

## AI Disclosure Policy

We welcome contributions from developers who use AI assistance as part of their workflow. However, AI tools must supplement your own skills and judgment — not replace them. Contributions where the author does not fully understand the code they are submitting will not be accepted, regardless of how the code was produced.

If any part of your contribution was written or significantly shaped by an AI tool (e.g. GitHub Copilot, ChatGPT, Claude, Cursor, etc.), the following conditions **all** apply:

1. **You know Java yourself.**
   You must have a solid understanding of Java. AI-generated code is only acceptable if you are capable of writing equivalent code yourself. If you cannot reason about the code, you cannot responsibly submit it.

2. **The majority of the code is still written by you.**
   AI may assist you, but it should not be the primary author. Patches that are almost entirely AI-generated will be rejected.

3. **You have tested both the changed system and the systems it touches.**
   Test that your changes work correctly. Also test that the surrounding systems — the ones your changes interact with — still behave as expected. Do not rely on the AI to have gotten the interactions right.

4. **You wrote the PR description yourself.**
   The pull request description must be written in your own words. Do not paste AI-generated summaries. A good description tells reviewers *what* changed, *why*, and what you tested.

5. **You have read and understood all of the code you are submitting.**
   Read every line. Understand what it does and why. Be able to explain any part of it to a reviewer. Submitting code you do not understand is not acceptable, regardless of its source.

AI tools can produce plausible-looking code that is subtly wrong, insecure, or incompatible with the rest of the codebase. Reviewers cannot catch every problem — the primary safeguard is an author who understands what they submitted. These rules ensure that a real human remains accountable for every contribution.

Violations of this policy (e.g. submitting code you do not understand, or a PR description that is clearly AI-generated) will result in the PR being closed.
