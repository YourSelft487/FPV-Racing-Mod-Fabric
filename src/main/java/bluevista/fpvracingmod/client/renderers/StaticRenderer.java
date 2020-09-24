package bluevista.fpvracingmod.client.renderers;

import bluevista.fpvracingmod.client.ClientInitializer;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.GlAllocationUtils;
import org.lwjgl.system.MemoryUtil;
import sun.java2d.pipe.RenderBuffer;

import java.nio.ByteBuffer;
import java.util.Random;

public class StaticRenderer {

    private static BufferBuilder prevBufferBuilder;
    private final static MinecraftClient client = ClientInitializer.client;
//    private static ByteBuffer prevByteBuffer = ByteBuffer.allocate(client.getWindow().getFramebufferHeight() * client.getWindow().getFramebufferWidth() * 16);
    private static ByteBuffer prevByteBuffer;
    private static float prevTickDelta;
    private static final float TICK_TIME = 1/20f;
    private static int count;
    private static boolean redrawPrevFrame;

    public static void render(int minRowSpacing, int maxRowSpacing, int minColumnSpacing, int maxColumnSpacing, float tickDelta) {

        boolean b;
        int width = 0,
                color = 0,
                rowCounter = 0,
                rowSpacing = 0,
                columnSpacing = 0,
                offset = 0;

        int[] lastRowColors = new int[client.getWindow().getFramebufferWidth()],
                lastRowColumnSpacing = new int[client.getWindow().getFramebufferWidth()];

        // tickDelta math
        if (tickDelta < prevTickDelta) {
            redrawPrevFrame = false;
//            System.out.println("YO");
//            prevByteBuffer = prevByteBuffer.clear();
//            prevByteBuffer = null;
//            prevBufferBuilder = null;
        }

//        if (runningDelta - prevDelta >= TICK_TIME) {
//            runningDelta = runningDelta - prevDelta;
//            prevDelta = System.currentTimeMillis() / 1000f;
//            redrawPrevFrame = false;
////            prevByteBuffer = prevByteBuffer.clear();
////            prevByteBuffer = null;
////            prevBufferBuilder = null;
//        }

        // Variable prep
//        if (prevBufferBuilder != null) {
//            prevBufferBuilder.end();
//            BufferRenderer.draw(prevBufferBuilder);
//            return;
//        }

        prevTickDelta = tickDelta;


        if (redrawPrevFrame) {
            prevBufferBuilder = new BufferBuilder(2097152);
            prevBufferBuilder.begin(0, VertexFormats.POSITION_COLOR);

//            prevByteBuffer.rewind();
//            for (int height = 0; height < client.getWindow().getFramebufferHeight(); ++height) {
//                for (int width = 0; width < client.getWindow().getFramebufferWidth(); ++width) {
//                    prevBufferBuilder.vertex(width, height, -90.0d).color(prevByteBuffer[12 + offset], prevByteBuffer[13 + offset], prevByteBuffer[14 + offset], 255);
//                }
//            }

            for (int length = 0; length < client.getWindow().getFramebufferWidth() * client.getWindow().getFramebufferHeight(); ++length) {
                prevBufferBuilder.vertex(prevByteBuffer.get(offset), prevByteBuffer.get(4 + offset), -90.0d).color(prevByteBuffer.get(12 + offset), prevByteBuffer.get(13 + offset), prevByteBuffer.get(14 + offset), 255);
                offset += 16;
            }

            prevBufferBuilder.end();
            BufferRenderer.draw(prevBufferBuilder);
//            Pair<BufferBuilder.DrawArrayParameters, ByteBuffer> pair = prevBufferBuilder.popData();
//            BufferBuilder.DrawArrayParameters drawArrayParameters = (BufferBuilder.DrawArrayParameters)pair.getFirst();
//            draw((ByteBuffer)pair.getSecond(), drawArrayParameters.getMode(), drawArrayParameters.getVertexFormat(), drawArrayParameters.getCount());
            return;
//            BufferRenderer.draw(prevByteBuffer, 0, VertexFormats.POSITION_COLOR, count);
//            draw(prevByteBuffer, 0, VertexFormats.POSITION_COLOR, count);
//            draw(prevBufferBuilder.popData().getSecond(), 0, VertexFormats.POSITION_COLOR, count);
//            return;
        }

        Random random = new Random();
        BufferBuilder bufferBuilder = new BufferBuilder(2097152);
        prevBufferBuilder = new BufferBuilder(2097152);
//        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(client.getWindow().getFramebufferHeight() * client.getWindow().getFramebufferWidth() * 16);
//        ByteBuffer byteBuffer = GlAllocationUtils.allocateByteBuffer(client.getWindow().getFramebufferHeight() * client.getWindow().getFramebufferWidth() * 16);


        // Rendering prep
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableAlphaTest();
        RenderSystem.matrixMode(5889);
        RenderSystem.loadIdentity();
        RenderSystem.ortho(0.0D, client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight(), 0.0D, 1000.0D, 3000.0D);
        RenderSystem.matrixMode(5888);

        // Rendering loop
        bufferBuilder.begin(0, VertexFormats.POSITION_COLOR);
        prevBufferBuilder.begin(0, VertexFormats.POSITION_COLOR);
        count = 0;
        for (int height = 0; height < client.getWindow().getFramebufferHeight(); ++height) {
            if (rowCounter == 0 || rowCounter >= rowSpacing) {
                rowCounter = 0;
                rowSpacing = random.nextInt(maxRowSpacing - minRowSpacing + 1) + minRowSpacing;

                lastRowColors = new int[client.getWindow().getFramebufferWidth()];
                lastRowColumnSpacing = new int[client.getWindow().getFramebufferWidth()];
            }

            width = 0;
            while (width < client.getWindow().getFramebufferWidth()) {
                if (rowCounter == 0) {
                    b = random.nextBoolean();
                    color = b ? 255 : 0;

                    if (maxColumnSpacing <= client.getWindow().getFramebufferWidth() - width) {
                        columnSpacing = random.nextInt(maxColumnSpacing - minColumnSpacing + 1) + minColumnSpacing;
                    } else if (maxColumnSpacing >= client.getWindow().getFramebufferWidth() - width &&
                               minColumnSpacing <= client.getWindow().getFramebufferWidth() - width) {
                        columnSpacing = random.nextInt(client.getWindow().getFramebufferWidth() - width - minColumnSpacing + 1) + minColumnSpacing;
                    } else {
                        columnSpacing = client.getWindow().getFramebufferWidth() - width;
                    }

                    lastRowColors[width] = color;
                    lastRowColumnSpacing[width] = columnSpacing;
                } else {
                    color = lastRowColors[width];
                    columnSpacing = lastRowColumnSpacing[width];
                }

                for (int rowSectionWidth = width; rowSectionWidth < width + columnSpacing; ++rowSectionWidth) {
                    bufferBuilder.vertex(rowSectionWidth, height, -90.0D).color(color, color, color, 255).next();
//                    prevBufferBuilder.vertex(rowSectionWidth, height, -90.0D).color(color, color, color, 255).next();
//                    byteBuffer.putFloat(0 + offset, (float) rowSectionWidth);
//                    byteBuffer.putFloat(4 + offset, (float) height);
//                    byteBuffer.putFloat(8 + offset, -90.0f);
//                    offset += 4;
//
//                    byteBuffer.put(0 + offset, (byte) color);
//                    byteBuffer.put(1 + offset, (byte) color);
//                    byteBuffer.put(2 + offset, (byte) color);
//                    byteBuffer.put(3 + offset, (byte) 255);
//                    offset += 1;
//                    ++count;
                }

                width += columnSpacing;
            }

            ++rowCounter;
        }

        // Rendering
        bufferBuilder.end();
//        BufferRenderer.draw(bufferBuilder);
//        draw(bufferBuilder.popData().getSecond(), 0, VertexFormats.POSITION_COLOR, count);
        Pair<BufferBuilder.DrawArrayParameters, ByteBuffer> pair = bufferBuilder.popData();
        BufferBuilder.DrawArrayParameters drawArrayParameters = (BufferBuilder.DrawArrayParameters)pair.getFirst();
        draw((ByteBuffer)pair.getSecond(), drawArrayParameters.getMode(), drawArrayParameters.getVertexFormat(), drawArrayParameters.getCount());
        VertexFormat m = VertexFormats.POSITION_COLOR;
//        System.out.println("Count: " + count);
//        System.out.println("Capacity: " + byteBuffer.capacity());
//        draw(byteBuffer, 0, VertexFormats.POSITION_COLOR, count);
//        prevByteBuffer = byteBuffer.duplicate();
        prevByteBuffer = pair.getSecond();
//        prevBufferBuilder = bufferBuilder;
        redrawPrevFrame = true;

        // Post-rendering resetting
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableAlphaTest();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.matrixMode(5889);
        RenderSystem.loadIdentity();
        RenderSystem.ortho(0.0D, (double) client.getWindow().getFramebufferWidth() / client.getWindow().getScaleFactor(), (double) client.getWindow().getFramebufferHeight() / client.getWindow().getScaleFactor(), 0.0D, 1000.0D, 3000.0D);
        RenderSystem.matrixMode(5888);

//        runningDelta += System.currentTimeMillis() / 1000f;
    }

    private static void draw(ByteBuffer buffer, int mode, VertexFormat vertexFormat, int count) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        buffer.clear();
        if (count > 0) {
            vertexFormat.startDrawing(MemoryUtil.memAddress(buffer));
            GlStateManager.drawArrays(mode, 0, count);
            vertexFormat.endDrawing();
        }
    }
}
