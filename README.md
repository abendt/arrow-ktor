Kotlin single module project blueprint
======================================

Buildtool
* Gradle (Kotlin DSL)

Basic dependencies
* Kotlin JVM
* Kotlin Logging
* slf4j + logback

Testing
* Kotest
* mockk

## pre-commit hooks

Dependencies: https://pre-commit.com/#installation

* Install git hook: https://pre-commit.com/#3-install-the-git-hook-scripts
```bash
pre-commit install
```
* Test hook:
```bash
pre-commit run --all-files
```
