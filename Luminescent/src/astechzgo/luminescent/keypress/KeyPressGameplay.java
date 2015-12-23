package astechzgo.luminescent.keypress;

import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;

import astechzgo.luminescent.entity.Player;
import astechzgo.luminescent.entity.Projectile;
import astechzgo.luminescent.gameobject.Room;
import astechzgo.luminescent.utils.Constants;
import astechzgo.luminescent.utils.KeyboardUtils;

public class KeyPressGameplay {
	
	public static ArrayList<Projectile> projectiles = new ArrayList<>();
	private static double lastShot;
	
	public static void checkGameActions(Player thePlayer, Room room) {
		
		double deltaShot = (GLFW.glfwGetTime() * 1000) - lastShot;
		
		if(KeyboardUtils.isKeyDown(Constants.KEYS_ACTION_SHOOT) && deltaShot > 250) {
			// Creates Projectile and adds it to array list
			Projectile projectile = new Projectile((int)thePlayer.getPosX(),(int) thePlayer.getPosY());
			projectiles.add(projectile);	
			
			lastShot = (GLFW.glfwGetTime() * 1000);
		}
		
		// For every shot render it and keep shooting it forwards
		for(int i = 0; i < projectiles.size(); i++) {
			Projectile m = (Projectile) projectiles.get(i);
			m.fireBullet();
			
			if(room.doesContain((int)m.getX(), (int)m.getY())) {
				// If the bullet is in the room render it
				m.render();
			}
			else if(!room.doesContain((int)m.getX(),(int)m.getY())) {
				// If the bullet is not it the room delete it
				projectiles.remove(i);
			}
		}
	}
}
