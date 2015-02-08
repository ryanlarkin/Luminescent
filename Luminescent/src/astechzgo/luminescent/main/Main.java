package astechzgo.luminescent.main;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

public class Main
{
	public static void main(String[] args)
	{
		new Main().Run();
	}
	
	public void Run()
	{
		Init();
		
		while(!Display.isCloseRequested())
		{
			Tick();
		}
		
		Shutdown();
	}
	
	public void Init()
	{
		try
		{
			Display.setDisplayMode(new DisplayMode(800, 480));
			Display.create();
		}
		catch (LWJGLException e)
		{
			e.printStackTrace();
			System.exit(0);
		}
		
		Display.setTitle("Luminescent");
	}
	
	public void Shutdown()
	{
		Display.destroy();
	}
	
	public void Tick()
	{
		Display.update();
		
		if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
		{
			Display.destroy();
			System.exit(0);
		}
	}
	
}