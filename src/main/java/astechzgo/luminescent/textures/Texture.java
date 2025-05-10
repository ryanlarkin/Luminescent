package astechzgo.luminescent.textures;

import static astechzgo.luminescent.utils.SystemUtils.getResourceAsURL;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

public class Texture {

	private final ImageData imageData;
	
	private final String name;
	
	public Texture(String textureName) {
		this(textureName, loadImage(textureName));
	}

	protected Texture(String textureName, ByteBuffer data, int width, int height) {
		this(textureName, new ImageData(data, width, height));
	}

	private Texture(String textureName, ImageData imageData) {
		this.name = textureName;
		this.imageData = Objects.requireNonNull(imageData);

		TextureList.addTexture(this);
	}


	protected record ImageData(ByteBuffer data, int width, int height) {
		public void free() {
			MemoryUtil.memFree(data);
		}
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
	
	public ByteBuffer getData() {
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