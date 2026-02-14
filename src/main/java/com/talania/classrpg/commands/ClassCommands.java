package com.talania.classrpg.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.classrpg.ClassService;
import com.talania.classrpg.ClassType;
import com.talania.classrpg.config.ClassDefinitionsConfig;
import com.talania.classrpg.config.ClassRpgConfig;
import com.talania.classrpg.ui.ClassProfilePage;
import com.talania.classrpg.ui.ClassSelectionPage;
import com.talania.core.profile.TalaniaPlayerProfile;
import com.talania.core.profile.TalaniaProfileRuntime;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * RPG Class management commands for TalaniaClassRPG.
 * Inspired by Orbis_and_Dungeons.
 *
 * /class select [--player <name>]
 * /class change <class> [--player <name>]
 * /class reset [--player <name>]
 * /class info [--player <name>]
 * /class profile [--player <name>]
 */
public class ClassCommands extends AbstractCommandCollection {
    private final ClassService classService;
    private final TalaniaProfileRuntime profileRuntime;
    private final ClassDefinitionsConfig classDefinitions;
    private final ClassRpgConfig config;

    public ClassCommands(ClassService classService,
                         TalaniaProfileRuntime profileRuntime,
                         ClassDefinitionsConfig classDefinitions,
                         ClassRpgConfig config) {
        super("class", "Class management commands");
        this.classService = classService;
        this.profileRuntime = profileRuntime;
        this.classDefinitions = classDefinitions;
        this.config = config;
        addSubCommand(new SelectCommand());
        addSubCommand(new ChangeCommand());
        addSubCommand(new ResetCommand());
        addSubCommand(new InfoCommand());
        addSubCommand(new ProfileCommand());
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    // /class select
    private class SelectCommand extends AbstractPlayerCommand {
        private final OptionalArg<String> playerArg;
        public SelectCommand() {
            super("select", "Open class selection UI", false);
            this.playerArg = withOptionalArg("player", "Target player (admin only)", ArgTypes.STRING);
        }
        @Override
        protected boolean canGeneratePermission() {
            return false;
        }
        @Override
        protected void execute(@Nonnull CommandContext ctx,
                               @Nonnull Store<EntityStore> store,
                               @Nonnull Ref<EntityStore> ref,
                               @Nonnull PlayerRef playerRef,
                               @Nonnull World world) {
            String targetName = playerArg.get(ctx);
            PlayerRef targetRef;
            Player targetPlayer;
            Ref<EntityStore> targetEntityRef;
            Store<EntityStore> targetStore;

            if (targetName == null || targetName.isEmpty()) {
                targetRef = playerRef;
                targetPlayer = store.getComponent(ref, Player.getComponentType());
                targetEntityRef = ref;
                targetStore = store;
            } else {
                targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
                if (targetRef == null) {
                    ctx.sendMessage(Message.raw("Player not found."));
                    return;
                }

                UUID worldUuid = targetRef.getWorldUuid();
                if (worldUuid == null) {
                    ctx.sendMessage(Message.raw("Player is not in a world."));
                    return;
                }

                targetPlayer = (Player) Universe.get().getWorld(worldUuid).getEntity(targetRef.getUuid());
                if (targetPlayer == null) {
                    ctx.sendMessage(Message.raw("Player is not online."));
                    return;
                }

                targetEntityRef = null;
                targetStore = null;
            }

            if (targetPlayer == null) {
                ctx.sendMessage(Message.raw("Player data not available."));
                return;
            }

            PageManager pages = targetPlayer.getPageManager();
            if (pages.getCustomPage() instanceof ClassSelectionPage) {
                ctx.sendMessage(Message.raw("Class selection UI already open."));
                return;
            }

            String currentClassId = null;
            if (classService != null) {
                ClassType current = classService.getAssigned(targetRef.getUuid());
                currentClassId = current != null ? current.id() : null;
            }

            try {
                pages.openCustomPage(
                        targetEntityRef != null ? targetEntityRef : ref,
                        targetStore != null ? targetStore : store,
                        new ClassSelectionPage(targetRef, classService, classDefinitions, currentClassId, 0)
                );
                ctx.sendMessage(Message.raw("Class selection UI opened."));
            } catch (Exception e) {
                ctx.sendMessage(Message.raw("Failed to open class selection UI: " + e.getMessage()));
            }
        }
    }

    // /class change <class>
    private class ChangeCommand extends AbstractPlayerCommand {
        private final RequiredArg<String> classArg;
        private final OptionalArg<String> playerArg;
        public ChangeCommand() {
            super("change", "Change class directly", false);
            this.classArg = withRequiredArg("class", "Class to change to", ArgTypes.STRING);
            this.playerArg = withOptionalArg("player", "Target player (admin only)", ArgTypes.STRING);
        }
        @Override
        protected boolean canGeneratePermission() {
            return false;
        }
        @Override
        protected void execute(@Nonnull CommandContext ctx,
                               @Nonnull Store<EntityStore> store,
                               @Nonnull Ref<EntityStore> ref,
                               @Nonnull PlayerRef playerRef,
                               @Nonnull World world) {
            String classIdInput = classArg.get(ctx);
            String classId = classIdInput != null ? classIdInput.toLowerCase() : null;
            ClassType classType = ClassType.fromId(classId);
            if (classType == null) {
                ctx.sendMessage(Message.raw("Invalid class. Valid classes: " + listValidClasses()));
                return;
            }
            String targetName = playerArg.get(ctx);
            PlayerRef targetRef;
            Player targetPlayer;

            if (targetName == null || targetName.isEmpty()) {
                targetRef = playerRef;
                targetPlayer = store.getComponent(ref, Player.getComponentType());
            } else {
                targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
                if (targetRef == null) {
                    ctx.sendMessage(Message.raw("Player not found."));
                    return;
                }
                UUID worldUuid = targetRef.getWorldUuid();
                if (worldUuid == null) {
                    ctx.sendMessage(Message.raw("Player is not in a world."));
                    return;
                }
                targetPlayer = (Player) Universe.get().getWorld(worldUuid).getEntity(targetRef.getUuid());
            }

            if (targetPlayer == null) {
                ctx.sendMessage(Message.raw("Player is not online."));
                return;
            }

            if (classService != null) {
                classService.setAssigned(targetRef.getUuid(), classType);
            } else if (profileRuntime != null) {
                TalaniaPlayerProfile profile = profileRuntime.load(targetRef.getUuid());
                if (profile != null) {
                    profile.setClassId(classType.id());
                }
            }

            ctx.sendMessage(Message.raw("Class changed to " + classType.displayName() + "."));
        }
    }

    // /class reset
    private class ResetCommand extends AbstractPlayerCommand {
        private final OptionalArg<String> playerArg;
        public ResetCommand() {
            super("reset", "Reset class to none", false);
            this.playerArg = withOptionalArg("player", "Target player (admin only)", ArgTypes.STRING);
        }
        @Override
        protected boolean canGeneratePermission() {
            return false;
        }
        @Override
        protected void execute(@Nonnull CommandContext ctx,
                               @Nonnull Store<EntityStore> store,
                               @Nonnull Ref<EntityStore> ref,
                               @Nonnull PlayerRef playerRef,
                               @Nonnull World world) {
            String targetName = playerArg.get(ctx);
            PlayerRef targetRef;
            Player targetPlayer;

            if (targetName == null || targetName.isEmpty()) {
                targetRef = playerRef;
                targetPlayer = store.getComponent(ref, Player.getComponentType());
            } else {
                targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
                if (targetRef == null) {
                    ctx.sendMessage(Message.raw("Player not found."));
                    return;
                }
                UUID worldUuid = targetRef.getWorldUuid();
                if (worldUuid == null) {
                    ctx.sendMessage(Message.raw("Player is not in a world."));
                    return;
                }
                targetPlayer = (Player) Universe.get().getWorld(worldUuid).getEntity(targetRef.getUuid());
            }

            if (targetPlayer == null) {
                ctx.sendMessage(Message.raw("Player is not online."));
                return;
            }

            if (classService != null) {
                classService.clear(targetRef.getUuid());
            } else if (profileRuntime != null) {
                TalaniaPlayerProfile profile = profileRuntime.load(targetRef.getUuid());
                if (profile != null) {
                    profile.setClassId(null);
                }
            }

            ctx.sendMessage(Message.raw("Class reset."));

            try {
                PageManager pages = targetPlayer.getPageManager();
                pages.openCustomPage(ref, store,
                        new ClassSelectionPage(targetRef, classService, classDefinitions, null, 0));
            } catch (Exception ignored) {
            }
        }
    }

    // /class info
    private class InfoCommand extends AbstractPlayerCommand {
        private final OptionalArg<String> playerArg;
        public InfoCommand() {
            super("info", "Show current class info", false);
            this.playerArg = withOptionalArg("player", "Target player (admin only)", ArgTypes.STRING);
        }
        @Override
        protected boolean canGeneratePermission() {
            return false;
        }
        @Override
        protected void execute(@Nonnull CommandContext ctx,
                               @Nonnull Store<EntityStore> store,
                               @Nonnull Ref<EntityStore> ref,
                               @Nonnull PlayerRef playerRef,
                               @Nonnull World world) {
            String targetName = playerArg.get(ctx);
            PlayerRef targetRef;
            Player targetPlayer;

            if (targetName == null || targetName.isEmpty()) {
                targetRef = playerRef;
                targetPlayer = store.getComponent(ref, Player.getComponentType());
            } else {
                targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
                if (targetRef == null) {
                    ctx.sendMessage(Message.raw("Player not found."));
                    return;
                }
                UUID worldUuid = targetRef.getWorldUuid();
                if (worldUuid == null) {
                    ctx.sendMessage(Message.raw("Player is not in a world."));
                    return;
                }
                targetPlayer = (Player) Universe.get().getWorld(worldUuid).getEntity(targetRef.getUuid());
            }

            if (targetPlayer == null) {
                ctx.sendMessage(Message.raw("Player is not online."));
                return;
            }

            String classId = null;
            if (profileRuntime != null) {
                TalaniaPlayerProfile profile = profileRuntime.load(targetRef.getUuid());
                classId = profile != null ? profile.classId() : null;
            } else if (classService != null) {
                ClassType current = classService.getAssigned(targetRef.getUuid());
                classId = current != null ? current.id() : null;
            }

            ctx.sendMessage(Message.raw("Current class: " + (classId != null ? classId : "none")));
        }
    }

    // /class profile
    private class ProfileCommand extends AbstractPlayerCommand {
        private final OptionalArg<String> playerArg;

        public ProfileCommand() {
            super("profile", "Show RPG profile UI", false);
            this.playerArg = withOptionalArg("player", "Target player (admin only)", ArgTypes.STRING);
        }

        @Override
        protected boolean canGeneratePermission() {
            return false;
        }

        @Override
        protected void execute(@Nonnull CommandContext ctx,
                               @Nonnull Store<EntityStore> store,
                               @Nonnull Ref<EntityStore> ref,
                               @Nonnull PlayerRef playerRef,
                               @Nonnull World world) {
            String targetName = playerArg.get(ctx);
            PlayerRef targetRef;
            Player targetPlayer;
            Ref<EntityStore> targetEntityRef;
            Store<EntityStore> targetStore;

            if (targetName == null || targetName.isEmpty()) {
                targetRef = playerRef;
                targetPlayer = store.getComponent(ref, Player.getComponentType());
                targetEntityRef = ref;
                targetStore = store;
            } else {
                targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
                if (targetRef == null) {
                    ctx.sendMessage(Message.raw("Player not found."));
                    return;
                }
                UUID worldUuid = targetRef.getWorldUuid();
                if (worldUuid == null) {
                    ctx.sendMessage(Message.raw("Player is not in a world."));
                    return;
                }
                targetPlayer = (Player) Universe.get().getWorld(worldUuid).getEntity(targetRef.getUuid());
                if (targetPlayer == null) {
                    ctx.sendMessage(Message.raw("Player is not online."));
                    return;
                }
                targetEntityRef = null;
                targetStore = null;
            }

            if (targetPlayer == null) {
                ctx.sendMessage(Message.raw("Player data not available."));
                return;
            }

            try {
                targetPlayer.getPageManager().openCustomPage(
                        targetEntityRef != null ? targetEntityRef : ref,
                        targetStore != null ? targetStore : store,
                        new ClassProfilePage(targetRef, classService, profileRuntime, config, classDefinitions)
                );
                ctx.sendMessage(Message.raw("Profile UI opened."));
            } catch (Exception e) {
                ctx.sendMessage(Message.raw("Failed to open profile UI: " + e.getMessage()));
            }
        }
    }

    private static String listValidClasses() {
        return Arrays.stream(ClassType.values())
                .map(ClassType::id)
                .collect(Collectors.joining(", "));
    }
}
