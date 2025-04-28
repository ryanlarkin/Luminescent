package astechzgo.luminescent.textures;

import static astechzgo.luminescent.utils.SystemUtils.getResourceAsURL;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

public class Texture {

	private final ImageData imageData;
	
	private final String name;

	private BufferedImage bufferedImage;
	
	public Texture(String textureName) {
		this(textureName, loadImage(textureName));
	}

	protected Texture(String textureName, ByteBuffer data, int width, int height) {
		this(textureName, new ImageData(data, width, height));
	}

	private Texture(String textureName, ImageData imageData) {
		this.name = textureName;
		this.imageData = imageData;

		TextureList.addTexture(this);
	}


	protected record ImageData(ByteBuffer data, int width, int height) {
		public void free() {
			MemoryUtil.memFree(data);
		}
	}

	/**
	 * Convert BufferedImage to ByteBuffer
	 *
	 * @param image
	 *            The BufferedImage to convert
	 * @return The converted image
	 */
	public static ByteBuffer toByteBuffer(BufferedImage image) {
		int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        ByteBuffer buffer = MemoryUtil.memAlloc(image.getWidth() * image.getHeight() * 4); //4 for RGBA, 3 for RGB

        for(int y = 0; y < image.getHeight(); y++){
            for(int x = 0; x < image.getWidth(); x++){
                int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
                buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
                buffer.put((byte) (pixel & 0xFF));               // Blue component
                buffer.put((byte) ((pixel >> 24) & 0xFF));    // Alpha component. Only for RGBA
            }
        }

        buffer.flip(); //FOR THE LOVE OF GOD DO NOT FORGET THIS

        return buffer;
	}

	protected static ImageData loadImage(String imageLoc) {
		imageLoc = imageLoc.replaceAll("\\.", "/");

        try (InputStream inputStream = getResourceAsURL("textures/" + imageLoc + ".png").openStream()) {
			byte[] dataArray = inputStream.readAllBytes();

			int[] x = new int[1];
			int[] y = new int[1];
			int[] comp = new int[1];
			ByteBuffer imageData = MemoryUtil.memAlloc(dataArray.length).put(dataArray).flip();
			ByteBuffer stbData = STBImage.stbi_load_from_memory(imageData, x, y, comp, 4);
			MemoryUtil.memFree(imageData);
			if (stbData == null) {
				throw new RuntimeException("error loading image: " + STBImage.stbi_failure_reason());
			}

			ByteBuffer data = MemoryUtil.memAlloc(y[0] * x[0] * 4);
			MemoryUtil.memCopy(stbData, data);
			STBImage.stbi_image_free(stbData);

			return new ImageData(data, x[0], y[0]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}

	protected static BufferedImage convertToBufferedImage(ImageData imageData) {
		BufferedImage image = new BufferedImage(imageData.width, imageData.height, BufferedImage.TYPE_INT_ARGB);
		for (int i = 0; i < imageData.height; i++) {
			for (int j = 0; j < imageData.width; j++) {
				// Convert to unsigned values
				int red = imageData.data.get() & 0xFF;
				int blue = imageData.data.get() & 0xFF;
				int green = imageData.data.get() & 0xFF;
				int alpha = imageData.data.get() & 0xFF;
				int colour = alpha << 24 | red << 16 | green << 8 | blue;
				image.setRGB(j, i, colour);
			}
		}
		imageData.data.rewind();
		return image;
	}
	
	public BufferedImage getAsBufferedImage() {
		if (bufferedImage == null) {
			bufferedImage = convertToBufferedImage(imageData);
		}
		return bufferedImage;
	}
	
	public ByteBuffer getAsByteBuffer() {
		return imageData.data;
	}

	public String getName() {
		return name;
	}
    
    void dispose() {
    	imageData.free();
    }
    
    public int getCurrentFrame() {
        return 0;
    }
    
    public int count() {
        return 1;
    }

	public int getWidth() {
		return imageData.width;
	}

	public int getHeight() {
		return imageData.height;
	}
}