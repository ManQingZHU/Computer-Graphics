package notProvided.client;

import windowing.drawable.Drawable;
import windowing.drawable.DrawableDecorator;
import windowing.graphics.Color;

public class zBufferingDrawable extends DrawableDecorator {
	private double zBuffer[][];
	
	public zBufferingDrawable(Drawable delegate) {
		super(delegate);
		zBuffer = new double [delegate.getWidth()+1][delegate.getHeight()+1];  // save the index error
		for(int i = 0; i <= delegate.getWidth(); i++) {
			for(int j = 0; j <= delegate.getHeight(); j++)
				zBuffer[i][j] = -Double.MAX_VALUE;
		}
	}
	
	@Override
	public void setPixel(int x, int y, double z, int argbColor) {
		if(z < 0 && z >= zBuffer[x][y])
		{
			zBuffer[x][y] = z;
			delegate.setPixel(x,  y,  z, argbColor);
		}
		else return;
	}

}
