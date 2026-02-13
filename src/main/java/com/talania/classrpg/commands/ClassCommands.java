package com.talania.classrpg.commands;

import com.talania.classrpg.ClassType;
import com.talania.classrpg.ui.ClassSelectionPage;
import com.talania.core.profile.TalaniaProfileRuntime;
import com.talania.core.profile.TalaniaPlayerProfile;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * RPG Class management commands for TalaniaClassRPG.
 * Inspired by Orbis_and_Dungeons.
 *
 * /class select [--player <name>]
 * /class change <class> [--player <name>]
 * /class reset [--player <name>]
 * /class info [--player <name>]
 */
public class ClassCommands extends AbstractCommandCollection {
    public ClassCommands() {
        super("class", "Class management commands");
        addSubCommand(new SelectCommand());
        addSubCommand(new ChangeCommand());
        addSubCommand(new ResetCommand());
        addSubCommand(new InfoCommand());
    }

    // /class select
    private static class SelectCommand extends AbstractPlayerCommand {
        private final OptionalArg<String> playerArg;
        public SelectCommand() {
            super("select", "Open class selection UI", false);
            this.playerArg = withOptionalArg("player", "Target player (admin only)", ArgTypes.STRING);
        }
        @Override
        protected void execute(@Nonnull CommandContext ctx, @Nonnull EntityStore store, @Nonnull com.hypixel.hytale.component.Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
            String targetName = playerArg.get(ctx);
            PlayerRef targetRef = (targetName == null) ? playerRef : Universe.get().getPlayerByUsername(targetName);
            if (targetRef == null) {
                ctx.sendMessage("Player not found.");
                return;
            }
            List<ClassType> classList = Arrays.asList(ClassType.values());
            // Open UI (integration with UI system required)
            new ClassSelectionPage(targetRef, classList, null, 0).build();
            ctx.sendMessage("Class selection UI opened.");
        }
    }

    // /class change <class>
    private static class ChangeCommand extends AbstractPlayerCommand {
        private final RequiredArg<String> classArg;
        private final OptionalArg<String> playerArg;
        public ChangeCommand() {
            super("change", "Change class directly", false);
            this.classArg = withRequiredArg("class", "Class to change to", ArgTypes.STRING);
            this.playerArg = withOptionalArg("player", "Target player (admin only)", ArgTypes.STRING);
        }
        @Override
        protected void execute(@Nonnull CommandContext ctx, @Nonnull EntityStore store, @Nonnull com.hypixel.hytale.component.Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
            String classId = classArg.get(ctx);
            String targetName = playerArg.get(ctx);
            PlayerRef targetRef = (targetName == null) ? playerRef : Universe.get().getPlayerByUsername(targetName);
            if (targetRef == null) {
                ctx.sendMessage("Player not found.");
                return;
            }
            TalaniaPlayerProfile profile = TalaniaProfileRuntime.get().load(targetRef.getUuid());
            profile.setClassId(classId);
            ctx.sendMessage("Class changed to " + classId + ".");
        }
    }

    // /class reset
    private static class ResetCommand extends AbstractPlayerCommand {
        private final OptionalArg<String> playerArg;
        public ResetCommand() {
            super("reset", "Reset class to none", false);
            this.playerArg = withOptionalArg("player", "Target player (admin only)", ArgTypes.STRING);
        }
        @Override
        protected void execute(@Nonnull CommandContext ctx, @Nonnull EntityStore store, @Nonnull com.hypixel.hytale.component.Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
            String targetName = playerArg.get(ctx);
            PlayerRef targetRef = (targetName == null) ? playerRef : Universe.get().getPlayerByUsername(targetName);
            if (targetRef == null) {
                ctx.sendMessage("Player not found.");
                return;
            }
            TalaniaPlayerProfile profile = TalaniaProfileRuntime.get().load(targetRef.getUuid());
            profile.setClassId(null);
            ctx.sendMessage("Class reset.");
        }
    }

    // /class info
    private static class InfoCommand extends AbstractPlayerCommand {
        private final OptionalArg<String> playerArg;
        public InfoCommand() {
            super("info", "Show current class info", false);
            this.playerArg = withOptionalArg("player", "Target player (admin only)", ArgTypes.STRING);
        }
        @Override
        protected void execute(@Nonnull CommandContext ctx, @Nonnull EntityStore store, @Nonnull com.hypixel.hytale.component.Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
            String targetName = playerArg.get(ctx);
            PlayerRef targetRef = (targetName == null) ? playerRef : Universe.get().getPlayerByUsername(targetName);
            if (targetRef == null) {
                ctx.sendMessage("Player not found.");
                return;
            }
            TalaniaPlayerProfile profile = TalaniaProfileRuntime.get().load(targetRef.getUuid());
            String classId = profile.classId();
            ctx.sendMessage("Current class: " + (classId != null ? classId : "none"));
        }
    }
}
