**This document is a work in progress.**

# Contributing to Adaptive Alerting

Thanks for your interest in contributing to Adaptive Alerting (AA). We welcome and greatly appreciate all contributions, from
issues to documentation to code.

This document offers some guidelines for you to consider when submitting a contribution.

## Tests

**Unit tests.** Please include unit tests for your code. Our tests are a work in progress, including code coverage, but this is a current
focus area and we ask that you help us out.

## Code style

In general, try to mirror the code style that's already there.

**Default IntelliJ styles.** Officially we use the default IntelliJ styles. Unofficially different developers use slightly
modified versions of this baseline, so you may see some minor divergences, but at some point we'll likely enforce this
through Checkstyle, EditorConfig or similar.

**@author tags.** Please avoid the use of Javadoc `@author` tags. They get in the way of the "shared code ownership" ethos
we're trying to promote, and anyway it's easy enough to figure out who did what by using `git blame`.

**Lombok.** AA uses [Lombok](https://projectlombok.org/). Lombok includes Java language extensions that help keep the code
streamlined and [DRY](https://en.wikipedia.org/wiki/Don%27t_repeat_yourself). We encourage you to use it in any code you
submit.
