package notProvided.client;

import windowing.drawable.Drawable;
import windowing.drawable.DrawableDecorator;

public class ColoredDrawable extends DrawableDecorator{
	private int backColor;
	
	public ColoredDrawable(Drawable delegate, int argbColor) {
		super(delegate);
		backColor = argbColor;
	}
	
	@Override
	public void setPixel(int x, int y, double z, int argbColor) {
//		if(x < 50 || x >= 350 && x < 400 || x>= 700 || y < 50 || y >= 350 && y < 400 || y>= 700)
//			delegate.setPixel(x,  y,  z, backColor);
//		else
//			delegate.setPixel(x,  y,  z, argbColor);
		
		if(x < 50 || x>= 700 || y < 50 || y>= 700)
			delegate.setPixel(x,  y,  z, backColor);
		else
			delegate.setPixel(x,  y,  z, argbColor);
	}
}
