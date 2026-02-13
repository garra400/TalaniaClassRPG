# TalaniaClassRPG

Standalone class system mod built on top of TalaniaCore for Hytale.

## Features

- Per-class leveling (configurable max level) with linear XP curve
- Class definitions with active skills mapped to E/R keys
- Active skill cooldown handling via TalaniaCore's `AbilityCooldownService`
- Combat XP from NPC/Player kills
- Skill effects: area damage, healing, projectiles, stat modifiers, movement, teleport, etc.
- Persistent class progress saved in player profiles

## Architecture

```
TalaniaClassRPG/
├── src/main/java/com/talania/classrpg/
│   ├── TalaniaClassRpgPlugin.java   # Plugin entry point
│   ├── ClassRpgRuntime.java         # Service wiring
│   ├── ClassService.java            # Class assignment (assign/unassign per player)
│   ├── ClassProgressionService.java # XP + leveling per class
│   ├── ActiveSkillService.java      # Input action -> skill activation
│   ├── ActiveSkillEvent.java        # Event published when a skill activates
│   ├── ClassType.java               # Class definitions (warrior, mage, etc.)
│   ├── ActiveSkill.java             # Skill definition (id, cooldown, params)
│   ├── SkillSlot.java               # E/R slot mapping
│   ├── combat/
│   │   ├── ClassRpgCombatSystem.java  # Combat integration with stats
│   │   └── CombatXpService.java       # XP rewards from combat
│   ├── config/
│   │   └── ClassRpgConfig.java        # JSON config (maxLevel, baseXp, stepXp, etc.)
│   ├── effects/                       # Skill effect implementations
│   │   ├── ArcaneTorrentEffect.java
│   │   ├── LeadStormEffect.java
│   │   ├── MovementFreezeEffect.java
│   │   ├── MovementModifierEffect.java
│   │   ├── PoisonEffect.java
│   │   ├── PullEffect.java
│   │   ├── RadianceEffect.java
│   │   ├── RainOfVengeanceEffect.java
│   │   ├── TimedFlightEffect.java
│   │   ├── TimedInvulnerabilityEffect.java
│   │   └── TimedStatModifierEffect.java
│   ├── skills/
│   │   └── ActiveSkillExecutor.java   # Executes skill logic
│   └── state/
│       └── ClassRpgState.java         # Runtime state tracking
└── src/main/resources/
    └── manifest.json                  # Hytale mod manifest
```

## How It Works

### Runtime Flow

1. `TalaniaClassRpgPlugin.start()` creates `ClassRpgRuntime`
2. `ClassRpgRuntime` initializes all services and subscribes to events:
   - `InputActionEvent` -> `ActiveSkillService` (handles E/R presses)
   - `ActiveSkillEvent` -> `ActiveSkillExecutor` (executes skill logic)
   - `NpcDeathEvent` / `PlayerDeathEvent` -> `CombatXpService` (awards XP)
3. Player profile and class progress are managed via TalaniaCore's `TalaniaProfileRuntime`

### Key Classes

| Class | Purpose |
|-------|---------|
| `ClassRpgRuntime` | Wires all services; reads config; sets up event subscriptions |
| `ClassService` | Assigns/unassigns classes to players via their profile |
| `ClassProgressionService` | Adds XP and handles level-ups using `LevelingService` |
| `ActiveSkillService` | Listens for `InputActionEvent`, checks cooldowns, fires `ActiveSkillEvent` |
| `ActiveSkillExecutor` | Reads skill config and executes the appropriate effect (damage, heal, teleport, etc.) |
| `CombatXpService` | Awards class XP when NPCs or players die |

## Dependencies

| Dependency | Type | Purpose |
|------------|------|---------|
| **TalaniaCore** | `implementation` | Stats, events, profiles, progression, input, combat utils |
| **HytaleServer.jar** | `compileOnly` | Hytale server API (provided at runtime) |

### TalaniaCore Systems Used

- `EventBus` - Event publish/subscribe
- `TalaniaProfileRuntime` / `TalaniaPlayerProfile` - Player data persistence
- `LevelingService` / `LinearLevelingCurve` - XP and leveling
- `InputActionEvent` / `InputAction` - Skill key bindings (E/R)
- `AbilityCooldownService` - Cooldown management
- `EntityEffectService` - Hytale visual effects
- `AreaOfEffect` / `AreaDamage` / `AreaHealing` - Combat utilities
- `EntityAnimationManager` / `EntityAnimationEffect` - Animation control
- `StatsManager` / `StatModifier` - Stat buffs/debuffs
- `TeleportUtil` - Dash/blink skills
- `ProjectileTargetingUtil` / `RainOfArrowsUtil` - Projectile skills
- `ConfigManager` - JSON configuration loading

## Configuration

Config file: `class_rpg_config.json`

```json
{
  "maxLevel": 100,
  "baseXp": 100,
  "stepXp": 50,
  "combatXp": {
    "npcKillXp": 25,
    "playerKillXp": 100
  },
  "skills": {}
}
```

## Build

### Prerequisites

- Java 25+
- Gradle 9.3+ (wrapper included)
- TalaniaCore published to Maven local

### Build Steps

```bash
# 1. First, build and publish TalaniaCore
cd ../TalaniaCore
./gradlew :core:publishToMavenLocal

# 2. Then build TalaniaClassRPG
cd ../TalaniaClassRPG
./gradlew build
```

### Deploying

Copy both JARs to the Hytale `mods/` folder:
- `TalaniaCore/core/build/libs/TalaniaCore-0.1.0.jar`
- `TalaniaClassRPG/build/libs/TalaniaClassRPG-0.1.0.jar`

The `manifest.json` declares `TalaniaCore` as a dependency, so Hytale will load it first automatically.

## License

See [LICENSE](LICENSE).
