package com.talania.classrpg;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;

/**
 * Class RPG module plugin.
 */
public final class TalaniaClassRpgPlugin extends JavaPlugin {
    private ClassRpgRuntime runtime;

    @Override
    protected void start() {
        // Registra comandos de classe
        getCommandRegistry().register(new com.talania.classrpg.commands.ClassCommands());
    }

    public TalaniaClassRpgPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        this.runtime = new ClassRpgRuntime(getDataDirectory());
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
