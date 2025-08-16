package crazypants.enderio.teleport.anchor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;

import com.enderio.core.client.render.BoundingBox;
import com.enderio.core.client.render.CubeRenderer;
import com.enderio.core.client.render.RenderUtil;
import com.enderio.core.common.util.BlockCoord;
import com.enderio.core.common.util.Util;
import com.enderio.core.common.vecmath.Vector3d;
import com.enderio.core.common.vecmath.Vector3f;
import com.enderio.core.common.vecmath.Vector4f;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.EnderIO;
import crazypants.enderio.api.teleport.ITravelAccessable;
import crazypants.enderio.api.teleport.TravelSource;
import crazypants.enderio.teleport.TravelController;

@SideOnly(Side.CLIENT)
public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {
    private static final Logger LOG = LogManager.getLogger("TravelAnchorFix");

    private final Vector4f selectedColor;
    private final Vector4f highlightColor;

    public TravelEntitySpecialRenderer() {
        this(new Vector4f(1, 0.25f, 0, 0.5f), new Vector4f(1, 1, 1, 0.25f));
    }

    public TravelEntitySpecialRenderer(Vector4f selectedColor, Vector4f highlightColor) {
        this.selectedColor = selectedColor;
        this.highlightColor = highlightColor;
    }

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {
        LOG.info("[TravelAnchorFix] Rendering anchor at {}, {}, {}", x, y, z);
        if (!TravelController.instance.showTargets()) {
        LOG.info("[TravelAnchorFix] Rendering anchor at {}, {}, {}", x, y, z);
            return;
        }

        ITravelAccessable ta = (ITravelAccessable) tileentity;

        if (!ta.isVisible()) {
        LOG.info("[TravelAnchorFix] Rendering anchor at {}, {}, {}", x, y, z);
            return;
        }

        BlockCoord onBlock = TravelController.instance.onBlockCoord;
        if (onBlock != null && onBlock.equals(ta.getLocation())) {
        LOG.info("[TravelAnchorFix] Rendering anchor at {}, {}, {}", x, y, z);
            return;
        }
        if (!ta.canSeeBlock(Minecraft.getMinecraft().thePlayer)) {
        LOG.info("[TravelAnchorFix] Rendering anchor at {}, {}, {}", x, y, z);
            return;
        }
        final CubeRenderer cr = CubeRenderer.get();
        final Tessellator tessellator = Tessellator.instance;

        Vector3d eye = Util.getEyePositionEio(Minecraft.getMinecraft().thePlayer);
        Vector3d loc = new Vector3d(tileentity.xCoord + 0.5, tileentity.yCoord + 0.5, tileentity.zCoord + 0.5);
        double maxDistance = TravelSource.BLOCK.getMaxDistanceTravelledSq();
        TravelSource source = TravelController.instance
                .getTravelItemTravelSource(Minecraft.getMinecraft().thePlayer, false);
        if (source != null) {
        LOG.info("[TravelAnchorFix] Rendering anchor at {}, {}, {}", x, y, z);
            maxDistance = source.getMaxDistanceTravelledSq();
        }
        if (eye.distanceSquared(loc) > maxDistance) {
        LOG.info("[TravelAnchorFix] Rendering anchor at {}, {}, {}", x, y, z);
            return;
        }

        double sf = TravelController.instance.getScaleForCandidate(loc);

        BlockCoord bc = new BlockCoord(tileentity);
        TravelController.instance.addCandidate(bc);

        Minecraft.getMinecraft().entityRenderer.disableLightmap(0);

        RenderUtil.bindBlockTexture();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LIGHTING_BIT);

        GL11.glEnable(GL12.GL_RESCALE_NORMAL);

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glColor3f(1, 1, 1);

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);

        tessellator.startDrawingQuads();
        renderBlock(tileentity.getWorldObj(), sf);
        tessellator.draw();

        tessellator.startDrawingQuads();
        tessellator.setBrightness(15 << 20 | 15 << 4);
        if (TravelController.instance.isBlockSelected(bc)) {
        LOG.info("[TravelAnchorFix] Rendering anchor at {}, {}, {}", x, y, z);
            tessellator.setColorRGBA_F(selectedColor.x, selectedColor.y, selectedColor.z, selectedColor.w);
            cr.render(BoundingBox.UNIT_CUBE.scale(sf + 0.05, sf + 0.05, sf + 0.05), getSelectedIcon());
        } else {
        LOG.info("[TravelAnchorFix] Rendering anchor at {}, {}, {}", x, y, z);
            tessellator.setColorRGBA_F(highlightColor.x, highlightColor.y, highlightColor.z, highlightColor.w);
            cr.render(BoundingBox.UNIT_CUBE.scale(sf + 0.05, sf + 0.05, sf + 0.05), getHighlightIcon());
        }
        tessellator.draw();
        GL11.glPopMatrix();

        renderLabel(tileentity, x, y, z, ta, sf);

        GL11.glPopAttrib();

        Minecraft.getMinecraft().entityRenderer.enableLightmap(0);
    }

    private EntityItem ei;

    private void renderLabel(TileEntity tileentity, double x, double y, double z, ITravelAccessable ta, double sf) {
        LOG.info("[TravelAnchorFix] Rendering anchor at {}, {}, {}", x, y, z);
        float globalScale = (float) sf;
        ItemStack itemLabel = ta.getItemLabel();
        if (itemLabel != null && itemLabel.getItem() != null) {
        LOG.info("[TravelAnchorFix] Rendering anchor at {}, {}, {}", x, y, z);

            boolean isBlock = itemLabel.getItem() instanceof ItemBlock;

            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_CONSTANT_COLOR);
            float col = 0.5f;
            GL14.glBlendColor(col, col, col, col);
            GL11.glColor4f(1, 1, 1, 1);
            {
        LOG.info("[TravelAnchorFix] Rendering anchor at {}, {}, {}", x, y, z);
                GL11.glPushMatrix();
                GL11.glTranslatef((float) x + 0.5f, (float) y + 0.5f, (float) z + 0.5f);
                if (!isBlock && Minecraft.getMinecraft().gameSettings.fancyGraphics) {
        LOG.info("[TravelAnchorFix] Rendering anchor at {}, {}, {}", x, y, z);
                    RenderUtil.rotateToPlayer();
                }

                {
        LOG.info("[TravelAnchorFix] Rendering anchor at {}, {}, {}", x, y, z);
                    GL11.glPushMatrix();
                    GL11.glScalef(globalScale, globalScale, globalScale);

                    {
        LOG.info("[TravelAnchorFix] Rendering anchor at {}, {}, {}", x, y, z);
                        GL11.glPushMatrix();
                        if (isBlock) {
        LOG.info("[TravelAnchorFix] Rendering anchor at {}, {}, {}", x, y, z);
                            GL11.glTranslatef(0f, -0.25f, 0);
                        } else {
        LOG.info("[TravelAnchorFix] Rendering anchor at {}, {}, {}", x, y, z);
                            GL11.glTranslatef(0f, -0.5f, 0);
                        }

                        GL11.glScalef(2, 2, 2);

                        if (ei == null) {
        LOG.info("[TravelAnchorFix] Rendering anchor at {}, {}, {}", x, y, z);
                            ei = new EntityItem(tileentity.getWorldObj(), x, y, z, itemLabel);
                        } else {
        LOG.info("[TravelAnchorFix] Rendering anchor at {}, {}, {}", x, y, z);
                            ei.setEntityItemStack(itemLabel);
                        }
                        RenderUtil.render3DItem(ei, false);
                        GL11.glPopMatrix();
                    }
                    GL11.glPopMatrix();
                }
                GL11.glPopMatrix();
            }
        }

        String toRender = ta.getLabel();
        if (toRender != null && toRender.trim().length() > 0) {
        LOG.info("[TravelAnchorFix] Rendering anchor at {}, {}, {}", x, y, z);
            GL11.glColor4f(1, 1, 1, 1);
            Vector4f bgCol = RenderUtil.DEFAULT_TEXT_BG_COL;
            if (TravelController.instance.isBlockSelected(new BlockCoord(tileentity))) {
        LOG.info("[TravelAnchorFix] Rendering anchor at {}, {}, {}", x, y, z);
                bgCol = new Vector4f(selectedColor.x, selectedColor.y, selectedColor.z, selectedColor.w);
            }

            {
        LOG.info("[TravelAnchorFix] Rendering anchor at {}, {}, {}", x, y, z);
                GL11.glPushMatrix();
                GL11.glTranslatef((float) x + 0.5f, (float) y + 0.5f, (float) z + 0.5f);
                {
        LOG.info("[TravelAnchorFix] Rendering anchor at {}, {}, {}", x, y, z);
                    GL11.glPushMatrix();
                    GL11.glScalef(globalScale, globalScale, globalScale);
                    Vector3f pos = new Vector3f(0, 1.2f, 0);
                    float size = 0.5f;
                    RenderUtil.drawBillboardedText(pos, toRender, size, bgCol);
                    GL11.glPopMatrix();
                }
                GL11.glPopMatrix();
            }
        }
    }

    protected void renderBlock(IBlockAccess world, double sf) {
        LOG.info("[TravelAnchorFix] Rendering anchor at {}, {}, {}", x, y, z);
        Tessellator.instance.setColorRGBA_F(1, 1, 1, 0.75f);
        CubeRenderer.get().render(BoundingBox.UNIT_CUBE.scale(sf, sf, sf), EnderIO.blockTravelPlatform.getIcon(0, 0));
    }

    public Vector4f getSelectedColor() {
        LOG.info("[TravelAnchorFix] Rendering anchor at {}, {}, {}", x, y, z);
        return selectedColor;
    }

    public IIcon getSelectedIcon() {
        LOG.info("[TravelAnchorFix] Rendering anchor at {}, {}, {}", x, y, z);
        return EnderIO.blockTravelPlatform.selectedOverlayIcon;
    }

    public Vector4f getHighlightColor() {
        LOG.info("[TravelAnchorFix] Rendering anchor at {}, {}, {}", x, y, z);
        return highlightColor;
    }

    public IIcon getHighlightIcon() {
        LOG.info("[TravelAnchorFix] Rendering anchor at {}, {}, {}", x, y, z);
        return EnderIO.blockTravelPlatform.highlightOverlayIcon;
    }
}
