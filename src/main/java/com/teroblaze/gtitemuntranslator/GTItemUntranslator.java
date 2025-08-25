package com.teroblaze.gtitemuntranslator;

import java.io.File;

import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;

@Mod(
    modid = GTItemUntranslator.MODID,
    name = GTItemUntranslator.NAME,
    version = GTItemUntranslator.VERSION,
    dependencies = "required-after:gregtech;after:Waila",
    acceptedMinecraftVersions = "[1.7.10]")
public class GTItemUntranslator {

    public static final String MODID = "gtitemuntranslator";
    public static final String NAME = "GT Item Untranslator";
    public static final String VERSION = "1.0.0";

    /** Глобальные флаги включения/выключения */
    public static boolean tooltipsEnabled = true;
    public static boolean wailaEnabled = true;

    /** Конфиг Forge */
    public static Configuration config;

    @Mod.Instance(MODID)
    public static GTItemUntranslator INSTANCE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        System.out.println("[" + NAME + "] PRE-INIT started.");

        File configFile = event.getSuggestedConfigurationFile();
        config = new Configuration(configFile);
        config.load();

        tooltipsEnabled = config
            .getBoolean("tooltipsEnabled", Configuration.CATEGORY_GENERAL, true, "Show English tooltips in inventory");
        wailaEnabled = config
            .getBoolean("wailaEnabled", Configuration.CATEGORY_GENERAL, true, "Show English tooltips in Waila");

        OriginalLanguageStore.init();
        System.out.println("[" + NAME + "] PRE-INIT completed.");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println("[" + NAME + "] INIT started. Registering event handlers.");
        MinecraftForge.EVENT_BUS.register(new TooltipEventHandler());
        System.out.println("[" + NAME + "] Event handlers registered.");

        try {
            FMLInterModComms
                .sendMessage("Waila", "register", "com.teroblaze.gtitemuntranslator.waila.WailaRegister.register");
            System.out.println("[" + NAME + "] Waila integration registered.");
        } catch (Throwable t) {
            System.err.println("[" + NAME + "] Waila not found or integration failed.");
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        System.out.println("[" + NAME + "] POST-INIT completed.");
    }

    @Mod.EventHandler
    public void onServerStarted(FMLServerStartedEvent event) {
        System.out.println("[" + NAME + "] Server started. Registering commands.");
        MinecraftServer server = MinecraftServer.getServer();
        ICommandManager commandManager = server.getCommandManager();
        if (commandManager instanceof ServerCommandManager) {
            ((ServerCommandManager) commandManager).registerCommand(new CommandTIPP());
            ((ServerCommandManager) commandManager).registerCommand(new CommandWTIPP());
            System.out.println("[" + NAME + "] /tipp and /wtipp commands registered.");
        }

        if (!OriginalLanguageStore.isInitialized()) {
            System.err.println("[" + NAME + "] ERROR: Language store failed to initialize!");
            OriginalLanguageStore.init();
        }
    }

    @Mod.EventHandler
    public void onServerStopped(FMLServerStoppedEvent event) {
        System.out.println("[" + NAME + "] Server stopped. Saving config and unloading language store.");

        config.get(Configuration.CATEGORY_GENERAL, "tooltipsEnabled", tooltipsEnabled)
            .set(tooltipsEnabled);
        config.get(Configuration.CATEGORY_GENERAL, "wailaEnabled", wailaEnabled)
            .set(wailaEnabled);
        config.save();

        OriginalLanguageStore.unload();
        System.out.println("[" + NAME + "] Cleanup finished.");
    }
}
