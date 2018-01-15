package notProvided.client;

import com.sun.org.apache.xml.internal.security.transforms.TransformationException;

import notProvided.geometry.Transformation;
import windowing.drawable.Drawable;
import windowing.drawable.DrawableDecorator;
import windowing.graphics.Color;

public class DepthEffectDrawable extends DrawableDecorator{
	private Color depColor;
	private double znear;
	private double zfar;
	private double deltaZ;
	public DepthEffectDrawable(Drawable delegate, Color depColor, double znear, double zfar) {
		super(delegate);
		this.depColor = depColor;
		this.znear = znear;
		this.zfar = zfar;
		deltaZ = znear - zfar;
	}
	
	public void SetParam(Color depColor, double znear, double zfar) {
		this.depColor = depColor;
		this.znear = znear;
		this.zfar = zfar;
		deltaZ = znear - zfar;
	}
	
	@Override
	public void setPixel(int x, int y, double z, int argbColor) {
		double csz = 1.0/z;
		if(csz >= znear)
			delegate.setPixel(x, y, csz, argbColor);
		else if(csz <= zfar)
			delegate.setPixel(x, y, csz, depColor.asARGB());
		else {
			double t = (csz-zfar)/deltaZ;
			Color color = depColor.blendInto(1-t, Color.fromARGB(argbColor));
			delegate.setPixel(x, y, csz, color.asARGB());
		}

	}
}
