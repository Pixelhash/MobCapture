package de.codehat.mobcapture.dependencies;

import de.codehat.mobcapture.MobCapture;

public abstract class MCDependency {

    private MobCapture plugin;

    public MCDependency(MobCapture plugin) {
        this.plugin = plugin;
    }

    public MobCapture getPlugin() {
        return this.plugin;
    }

}
