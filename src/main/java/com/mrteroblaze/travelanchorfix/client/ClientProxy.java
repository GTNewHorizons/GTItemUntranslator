package com.mrteroblaze.travelanchorfix.client;

import com.mrteroblaze.travelanchorfix.client.render.TravelAnchorTESRFix;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import crazypants.enderio.teleport.TravelController;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public class ClientProxy {

    private KeyBinding teleportKey;

    public void preInit(FMLPreInitializationEvent event) {
        teleportKey = new KeyBinding("key.travelanchorfix.teleport", Keyboard.KEY_SPACE, "Travel Anchor Fix");
        ClientRegistry.registerKeyBinding(teleportKey);

        ClientRegistry.bindTileEntitySpecialRenderer(TileTravelAnchor.class, new TravelAnchorTESRFix());
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void init(FMLInitializationEvent event) {}

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        if (teleportKey.isPressed()) {
            TravelController.instance.activateSelectedTravelTarget(Minecraft.getMinecraft().thePlayer);
        }
    }
}
