package notProvided.client.testpages;

import geometry.Vertex3D;
import line.LineRenderer;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

public class HilightLineTest {
	private final int d = 15;
	
	private final LineRenderer renderer;
	private final Drawable panel;
	Vertex3D center;
	public HilightLineTest(Drawable panel, LineRenderer renderer)
	{
		this.panel = panel;
		this.renderer = renderer;
		
		makeCenter();
		render();
	}
	private void render() {
		double a, r, x, y;
		Vertex3D p1, p2;
		Color color = Color.RED;
		for(a = 0; a < 2*Math.PI; a += Math.PI/360)
		{
			r = d*(5+Math.sin(1*a))*(1+Math.sin(5*a));
			x = r*Math.cos(a)+center.getIntX();
			y = r*Math.sin(a)+center.getIntY();
			p1 = new Vertex3D(x, y, 0.0, color);
			
			x = r*Math.cos(Math.PI/8+a)+center.getIntX();
			y = r*Math.sin(Math.PI/8+a)+center.getIntY();
			p2 = new Vertex3D(x, y, 0.0, color);
			renderer.drawLine(p1, p2, panel);
		}
		
	}
	private void makeCenter() {
		int centerX = panel.getWidth() / 2;
		int centerY = panel.getHeight() / 2;
		center = new Vertex3D(centerX, centerY, 0, Color.RED);
	}
}
