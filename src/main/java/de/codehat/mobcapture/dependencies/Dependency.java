package de.codehat.mobcapture.dependencies;

import de.codehat.mobcapture.MobCapture;

public abstract class Dependency {

    private MobCapture plugin;

    public Dependency(MobCapture plugin) {
        this.plugin = plugin;
    }

    public MobCapture getPlugin() {
        return this.plugin;
    }

}
