# TalaniaClassRPG

Standalone class system mod built on top of TalaniaCore.

## Features

- Per-class leveling (0-100) with configurable XP curve
- Class definitions with active skills (E/R) and passive skills
- Active skill cooldown handling via TalaniaCore

## Dependencies

- TalaniaCore (included via composite build)
- Hytale Server API for compilation

## Build

This project uses a composite build to pull TalaniaCore from `../TalaniaCore`.

```
./gradlew build
```

If you need to point to a different TalaniaCore path, update `settings.gradle`.
