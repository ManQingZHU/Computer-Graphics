package notProvided.client;

import windowing.drawable.Drawable;
import windowing.drawable.DrawableDecorator;
import windowing.graphics.Color;

public class DepthCueingDrawable extends DrawableDecorator {
	private int frontZ, backZ;
	private Color nearColor;
	public DepthCueingDrawable(Drawable delegate, int frontZ, int backZ,Color color) {
		super(delegate);
		this.frontZ = frontZ;
		this.backZ = backZ;
		this.nearColor = color;
	}
	
	@Override
	public void setPixel(int x, int y, double z, int argbColor) {
		double deltaZ = frontZ-backZ;
		double scaler = (z-backZ)/deltaZ;
		argbColor = nearColor.scale(scaler).asARGB();
		delegate.setPixel(x,  y,  z, argbColor);   
	}
	
}
