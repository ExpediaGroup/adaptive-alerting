**This document is a work in progress.**

# Contributing to Adaptive Alerting

Thanks for your interest in contributing to Adaptive Alerting (AA). We welcome and greatly appreciate all contributions, from
issues to documentation to code.

This document offers some guidelines for you to consider when submitting a contribution.

## Tests

**Unit tests.** Please include unit tests for your code. Our tests are a work in progress, including code coverage, but this
is a current focus area and we ask that you help us out.

## Build

**Adding dependencies.** Be conservative in adding dependencies. If there's already a dependency that we're using to cover
some specific need, avoid adding a new dependency that does more or less the same thing.

**Dependency management.** Use the top-level POM for dependency management. Child POMs should just inherit versions from
that. That way we can manage versions from a single place across the project.

## Code style

**In general, try to mirror the code style that's already there.**

**Adopt language conventions.** Your Java method names should `beLikeThis()` rather than `be_like_this()` or `belikethis()`.
There may be cases where there's a principled reason to do something a little different, so take this more as a guideline,
but someone may ask about unconventional code.

**Default IntelliJ styles.** Officially we use the default IntelliJ styles. Unofficially different developers use slightly
modified versions of this baseline, so you may see some minor divergences, but at some point we'll likely enforce this
through Checkstyle, EditorConfig or similar.

**@author tags.** Please avoid the use of Javadoc `@author` tags. They get in the way of the "shared code ownership" ethos
we're trying to promote, and anyway it's easy enough to figure out who did what by using `git blame`.

**Lombok.** AA uses [Lombok](https://projectlombok.org/). Lombok includes Java language extensions that help keep the code
streamlined and [DRY](https://en.wikipedia.org/wiki/Don%27t_repeat_yourself). We encourage you to use it in any code you
submit, and to use it idiomatically. For example, if you have a `@Data` annotation on the class, no need to include a
`@ToString`, `@EqualsAndHashCode`, `@Getter`s and `@Setter`s, etc., since `@Data` already takes care of those.

**Prefer simple, meaningful code to heavy comments.** Non-Javadoc comments should generally focus on why we're doing
something rather than what the code is doing. If you find yourself using comments to explain what you're doing, consider
breaking the code into private methods with meaningful names instead.

## Design

**General.** What counts as good design is obviously subjective, and ideally there's agreement between you and the project
maintainers about any potentially controversial design decisions before you submit the PR. But here are some guidelines:

- Simple code
- Code that's small enough to throw away
- Temporary hacks can be OK as long as they're properly isolated and easy to undo when the time is right
- [DRY code](https://en.wikipedia.org/wiki/Don%27t_repeat_yourself)
- [SOLID principles](https://itnext.io/solid-principles-explanation-and-examples-715b975dcad4)

**Kafka apps.** AA includes various Kafka apps. We see Kafka as a wrapper around core domain logic, and in principle we may
end up writing wrappers for other messaging technologies like Kinesis. So if you write a Kafka app, consider isolating the
core domain logic from the Kafka part so we don't end up having to duplicate the logic in the Kinesis version should we do
that.

Having said that, sometimes it unduly complicates the implementation to separate these two, especially if the logic is very
small. So consider this a guideline rather than a hard rule.
