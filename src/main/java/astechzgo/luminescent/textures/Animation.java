package astechzgo.luminescent.textures;

import astechzgo.luminescent.utils.ImageUtils;
import org.lwjgl.system.MemoryUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Animation extends Texture {
	
	private static int idx = 0;
	
	private static final Timer t;
	
	static {		
		TimerTask tt = new TimerTask() {
			@Override
			public void run() {
				updateIndex();
			}
		};
		
		t = new Timer();
		t.scheduleAtFixedRate(tt, 100, 100);
	}
	
	public static void cleanup() {
		t.cancel();
	}
	
	private static void updateIndex() {
		idx++;
	}
	
	private final List<Texture> frames = new ArrayList<>();
	
	public Animation(String textureName, int count) {
		this(textureName, count, toCombinedImage(textureName, count));
	}

	private Animation(String textureName, int count, ImageData combinedImage) {
		super(textureName, combinedImage.data(), combinedImage.width(), combinedImage.height());

		for(int i = 0; i < count; i++) {
			frames.add(TextureList.findTexture(textureName + "$" + i));
		}
	}
	
	private static ImageData toCombinedImage(String imageLoc, int count) {
		if (count <= 0) {
			throw new IllegalArgumentException("Animation image count must be positive");
		}

		ImageData[] images = new ImageData[count];

        for(int i = 0; i < count; i++) {
			images[i] = loadImage(imageLoc + "$" + i);
		}
        
        int width = images[0].width() * count;
        int height = images[0].height();

		ImageData newImage = new ImageData(MemoryUtil.memAlloc(width * height * 4), width, height);
		for (int i = 0; i < count; i++) {
			ImageUtils.memCopy2d(images[i].data(), newImage.data(), images[0].width(), height, i * images[0].width(), width, 4);
			images[i].free();
		}

		return newImage;
	}
	
	public Texture getCurrent() {
		return frames.get(getCurrentFrame());
	}
	
	@Override
	public int getCurrentFrame() {
	    return idx % count();
	}
	
	@Override
	public int count() {
	    return frames.size();
	}
}
