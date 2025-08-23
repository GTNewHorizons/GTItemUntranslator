package com.teroblaze.gtitemuntranslator;

import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;

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

    /** Глобальный флаг включения/выключения английских тултипов */
    public static boolean tooltipsEnabled = true;

    @Mod.Instance(MODID)
    public static GTItemUntranslator INSTANCE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        System.out.println("[" + NAME + "] PRE-INIT started.");
        OriginalLanguageStore.init();
        System.out.println("[" + NAME + "] PRE-INIT completed.");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println("[" + NAME + "] INIT started. Registering event handlers.");
        MinecraftForge.EVENT_BUS.register(new TooltipEventHandler());
        System.out.println("[" + NAME + "] Event handlers registered.");

        // Регистрируем Waila-интеграцию (если Waila установлена)
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
        System.out.println("[" + NAME + "] Server started. Registering /gtip command.");
        MinecraftServer server = MinecraftServer.getServer();
        ICommandManager commandManager = server.getCommandManager();
        if (commandManager instanceof ServerCommandManager) {
            ((ServerCommandManager) commandManager).registerCommand(new CommandGTIP());
            System.out.println("[" + NAME + "] /gtip command registered.");
        }

        if (!OriginalLanguageStore.isInitialized()) {
            System.err.println("[" + NAME + "] ERROR: Language store failed to initialize!");
            OriginalLanguageStore.init();
        }
    }

    @Mod.EventHandler
    public void onServerStopped(FMLServerStoppedEvent event) {
        System.out.println("[" + NAME + "] Server stopped. Unloading language store.");
        OriginalLanguageStore.unload();
        System.out.println("[" + NAME + "] Cleanup finished.");
    }
}
