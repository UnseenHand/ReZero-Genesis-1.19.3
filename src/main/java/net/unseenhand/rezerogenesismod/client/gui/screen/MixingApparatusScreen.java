package net.unseenhand.rezerogenesismod.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.TooltipFlag;
import net.unseenhand.rezerogenesismod.ReZeroGenesisMod;
import net.unseenhand.rezerogenesismod.client.gui.menu.MixingApparatusMenu;
import net.unseenhand.rezerogenesismod.client.renderer.FluidTankRenderer;
import net.unseenhand.rezerogenesismod.fluid.capability.SlotFluidHandler;
import net.unseenhand.rezerogenesismod.util.MouseUtil;

import java.util.Optional;

public class MixingApparatusScreen extends AbstractContainerScreen<MixingApparatusMenu> {
    private static final ResourceLocation MENU_TEXTURE =
            new ResourceLocation(ReZeroGenesisMod.MOD_ID, "textures/gui/mixing_apparatus_screen.png");
    private static final ResourceLocation SIDE_INVENTORY_TEXTURE =
            new ResourceLocation(ReZeroGenesisMod.MOD_ID, "textures/gui/mixing_apparatus_side_inventory.png");
    private FluidTankRenderer render;

    public MixingApparatusScreen(MixingApparatusMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);

        this.titleLabelX = 0;
        this.titleLabelY = 0;

        this.inventoryLabelX = 176;
        this.inventoryLabelY = 0;
    }

    private void assignFluidRender() {
        render = new FluidTankRenderer(12000, true, 23, 118);
    }

    @Override
    protected void init() {
        // 3 is set to move the container to the left for a little bit
        this.leftPos = (this.width - this.imageWidth) / 3;
        this.topPos = (this.height - this.imageHeight) / 3;

        assignFluidRender();
    }

    // mouseX and mouseY indicate the scaled coordinates of where the cursor is in on the screen
    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        // Render things here before widgets (background textures)
        // Custom rendering for the slot
//        renderCustomSlot(poseStack, mouseX, mouseY);



        // Then the widgets if this is a direct child of the Screen
        super.render(poseStack, mouseX, mouseY, partialTick);

        // Render things after widgets (tooltips)
        renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTicks, int pMouseX, int pMouseY) {
        // Set MenuBG
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, MENU_TEXTURE);
        int menuTexturePositionX = this.leftPos;
        int menuTexturePositionY = this.topPos;

        renderMenuContainer(pPoseStack, menuTexturePositionX, menuTexturePositionY);
        renderPlayerInventoryAsSideInventory(pPoseStack, menuTexturePositionX, menuTexturePositionY);

        renderProgressArrow(pPoseStack, menuTexturePositionX, menuTexturePositionY);
        render.render(pPoseStack, menuTexturePositionX + 110,menuTexturePositionY + 8, menu.getFluidStack());
    }

    private void renderCustomSlot(PoseStack poseStack, int mouseX, int mouseY) {
        // Adjust the rendering position and size of the slot
        int slotX = 100;
        int slotY = 8;
        int slotWidth = 20;
        int slotHeight = 50;

        // Render the custom slot using the adjusted position and size
        blit(poseStack, slotX, slotY, 0, 0, slotWidth, slotHeight);
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        super.renderLabels(pPoseStack, pMouseX, pMouseY);
        renderFluidAreaTooltips(pPoseStack, pMouseX, pMouseY, leftPos, topPos);
    }

    private void renderFluidAreaTooltips(PoseStack pPoseStack, int pMouseX, int pMouseY, int x, int y) {
        if (isMouseAboveArea(pMouseX, pMouseY, x, y, 110, 8)) {
            renderTooltip(pPoseStack,
                    render.getTooltip(menu.getFluidStack(), TooltipFlag.NORMAL),
                    Optional.empty(),
                    pMouseX - x ,
                    pMouseY - y);
        }
    }

    private boolean isMouseAboveArea(int pMouseX, int pMouseY, int x, int y, int offsetX, int offsetY) {
        return MouseUtil.isMouseOver(pMouseX, pMouseY, x + offsetX, y + offsetY, render.getWidth(), render.getHeight());
    }

    private void renderMenuContainer(PoseStack pPoseStack, int menuTexturePositionX, int menuTexturePositionY) {
        blit(
                pPoseStack,
                menuTexturePositionX,
                menuTexturePositionY,
                0,
                0,
                imageWidth,
                imageHeight + 10);
    }

    private void renderPlayerInventoryAsSideInventory(PoseStack pPoseStack,
                                                      int menuTexturePositionX,
                                                      int menuTexturePositionY) {
        // TODO: You should think about the way to set every single value as a meaningful unit
        // Set InventoryBG (Player Inventory)
        // Maybe adds textures to the Minecraft instance, so should pay attention to it
        RenderSystem.setShaderTexture(0, SIDE_INVENTORY_TEXTURE);
        int sideInventoryPositionX = menuTexturePositionX + imageWidth;
        int sideInventoryPositionY;
        sideInventoryPositionY = menuTexturePositionY;

        int inventoryWidth = 16 + 18 * 4;
        int inventoryHeight = imageHeight + 10;
        // Inventory should be : x or imageWidth x imageHeight
        //
        blit(
                pPoseStack,
                sideInventoryPositionX,
                sideInventoryPositionY,
                0,
                0,
                inventoryWidth,
                inventoryHeight);
    }

    private void renderProgressArrow(PoseStack pPoseStack, int x, int y) {
        if (menu.isCrafting()) {
            // Set ArrowTexture
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, MENU_TEXTURE);

            blit(
                    pPoseStack,
                    x + 83,
                    y + 28,
                    176,
                    0,
                    8,
                    menu.getScaledProgress());
        }
    }
}
