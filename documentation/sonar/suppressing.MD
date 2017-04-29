# Suppressing an issue

If SonarQube tells you a false positive and you are 100% certain of that, this is the way to fix this:

You will see why this Sonar Rule exists and check if it applies to your case.
In case it does, consult a Maintainer to accept your exception.

If it was a falso positive, report it to the Sonar team to resolve it eventually.

In any case, use this code:

```
/*
 * Suppressing Sonar Rule squid:Sxxyy
 * This rule does "Find that you are doing x wrong"
 * But in this case the rule does not apply because
 * We are doing x in way y and that is reasonable
 */
@SuppressWarnings("squid:Sxxyy")
```
