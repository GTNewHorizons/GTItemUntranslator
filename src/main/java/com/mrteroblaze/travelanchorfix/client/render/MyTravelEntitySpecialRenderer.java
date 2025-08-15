package com.mrteroblaze.travelanchorfix.client.render;

import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import crazypants.enderio.teleport.anchor.TravelEntitySpecialRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.util.vector.Vector3f;

public class MyTravelEntitySpecialRenderer extends TravelEntitySpecialRenderer {

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
        if (te instanceof TileTravelAnchor) {
            TileTravelAnchor anchor = (TileTravelAnchor) te;

            FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
            boolean prevUnicode = fr.getUnicodeFlag();
            fr.setUnicodeFlag(true);

            super.renderTileEntityAt(te, x, y, z, partialTick);

            fr.setUnicodeFlag(prevUnicode);
        } else {
            super.renderTileEntityAt(te, x, y, z, partialTick);
        }
    }

    private Vector3f getAnchorPosition(TileEntity tileentity) {
        return new Vector3f(
                (float) tileentity.xCoord + 0.5f,
                (float) tileentity.yCoord + 0.5f,
                (float) tileentity.zCoord + 0.5f
        );
    }
}
