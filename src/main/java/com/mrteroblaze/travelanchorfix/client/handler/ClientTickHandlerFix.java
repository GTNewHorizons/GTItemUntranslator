package com.mrteroblaze.travelanchorfix.client.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import crazypants.enderio.teleport.TravelController;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;

public class ClientTickHandlerFix {

    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.theWorld == null || mc.thePlayer == null) {
            return;
        }

        EntityPlayer player = mc.thePlayer;

        boolean holdingStaff = TravelController.instance.isTravelItemEquipped(player);
        boolean standingOnAnchor = false;

        if (mc.theWorld.getTileEntity(
                (int) Math.floor(player.posX),
                (int) Math.floor(player.posY - 1),
                (int) Math.floor(player.posZ)
        ) instanceof TileTravelAnchor) {
            standingOnAnchor = true;
        }

        if (holdingStaff || standingOnAnchor) {
            // Обновляем список доступных якорей
            TravelController.instance.onClientTick(new TickEvent.ClientTickEvent(TickEvent.Phase.END));
        }
    }
}
