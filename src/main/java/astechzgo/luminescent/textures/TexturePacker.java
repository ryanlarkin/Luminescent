package astechzgo.luminescent.textures;

import astechzgo.luminescent.utils.ImageUtils;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TexturePacker {
    private Texture atlas;
    
    private Set<AtlasMember> atlasMembers;
    private final Set<Texture> textures = new HashSet<>();
    
    public void addTextures(Texture... textures) {
        addTextures(List.of(textures));
    }
    
    public void addTextures(List<Texture> textures) {
        for(Texture texture : textures) {
            if(texture != null) {
                this.textures.add(texture);
            }
            else {
                this.textures.add(TextureList.findTexture("misc.blank"));
            }
        }
    }
    
    public void pack() {
        List<Texture> byWidth = new ArrayList<>(textures.size());
        List<Texture> byHeight = new ArrayList<>(textures.size());
        
        byWidth.addAll(textures);
        byWidth.sort((o1, o2) -> o2.getWidth() - o1.getWidth());
        
        byHeight.addAll(textures);
        byHeight.sort((o1, o2) -> o2.getHeight() - o1.getHeight());
        
        Set<AtlasMember> members = new HashSet<>();
        
        int width = Math.max(1024, byWidth.get(0).getWidth());
        int levelY = 0;
        if(byWidth.get(0).getWidth() > 1024) {
            members.add(new AtlasMember(byWidth.get(0), 0, 0));
            levelY = byWidth.get(0).getHeight();
            byHeight.remove(byWidth.get(0));
            byWidth.remove(byWidth.get(0));
        }
        
        while(byHeight.size() != 0) {
            Texture th = byHeight.get(0);
            members.add(new AtlasMember(th, 0, levelY));
            byWidth.remove(th);
            byHeight.remove(th);

            // Add extra space due to bilinear blending
            int levelX = th.getWidth() + 1;
            int i = 0;
            while(byWidth.size() != i) {
                Texture tw = byWidth.get(i);
                if(width - levelX >= tw.getWidth()) {
                    members.add(new AtlasMember(tw, levelX, levelY));
                    byWidth.remove(tw);
                    byHeight.remove(tw);
                    
                    levelX += tw.getWidth() + 1;
                }
                else {
                    i++;
                }
            }
            
            levelY += th.getHeight();
        }
        
        atlasMembers = Collections.unmodifiableSet(members);
        buildTexture(atlasMembers, width, levelY);
    }
    
    private void buildTexture(Set<AtlasMember> atlasMembers, int width, int height) {
        ByteBuffer imageData = MemoryUtil.memAlloc(width * height * 4);
        
        for(AtlasMember member : atlasMembers) {
            ImageUtils.memCopy2d(member.texture.getData(), imageData, member.width, member.height, member.x, member.y, width, 4);
        }
        
        atlas = new Texture("texture-atlas", imageData, width, height);
    }
    
    public Texture getAtlas() {
        return atlas;
    }
    
    public AtlasMember getAtlasMember(Texture texture) {
        if(atlasMembers != null) {
            for(AtlasMember member : atlasMembers) {
                if(member.texture == texture) {
                    return member;
                }
            }
            return null;
        }
        return null;
    }
    
    public Set<AtlasMember> getAtlasMembers() {
        return new HashSet<>(atlasMembers);
    }
    
    public static class AtlasMember  {
        
        private float s = -1, t = -1;
        public final int x, y;
        public final int width, height;
        public final Texture texture;
        
        private AtlasMember(Texture texture, int x, int y) {
            this.texture = texture;
            this.x = x;
            this.y = y;
            this.width = texture.getWidth();
            this.height = texture.getHeight();
        }
        
        public void setTexSize(int atlasWidth, int atlasHeight) {
            s = ((float)width) / atlasWidth;
            t = ((float)height) / atlasHeight;
        }
        
        public float getTexWidth() {
            return s;
        }
        
        public float getTexHeight() {
            return t;
        }
    }
}
