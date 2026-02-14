package com.talania.classrpg;

import com.hypixel.hytale.server.core.command.system.CommandRegistry;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.talania.core.localization.TranslationManager;

import javax.annotation.Nonnull;

/**
 * Class RPG module plugin.
 */
public final class TalaniaClassRpgPlugin extends JavaPlugin {
    private ClassRpgRuntime runtime;

    @Override
    protected void start() {
        if (runtime == null) {
            this.runtime = new ClassRpgRuntime(getDataDirectory());
        }

        TranslationManager.initialize(getDataDirectory());
        TranslationManager.registerBundledLanguages(TalaniaClassRpgPlugin.class, "en", "pt_br");

        // Registra comandos de classe
        CommandRegistry commands = getCommandRegistry();
        commands.registerCommand(new com.talania.classrpg.commands.ClassCommands(
                runtime.classService(),
                runtime.profileRuntime(),
                runtime.classDefinitions(),
                runtime.config()));
    }

    public TalaniaClassRpgPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        if (runtime == null) {
            this.runtime = new ClassRpgRuntime(getDataDirectory());
        }
        getEntityStoreRegistry().registerSystem(
                new com.talania.classrpg.combat.ClassRpgCombatSystem(
                        runtime.classService(),
                        runtime.state(),
                        runtime.combatXpService(),
                        runtime.config().skills));
    }

    public ClassRpgRuntime runtime() {
        return runtime;
    }
}
