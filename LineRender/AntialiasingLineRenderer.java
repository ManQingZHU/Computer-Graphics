package notProvided.line;

import geometry.Vertex3D;
import line.AnyOctantLineRenderer;
import line.LineRenderer;
import windowing.drawable.Drawable;

public class AntialiasingLineRenderer implements LineRenderer {
	// use the static factory make() instead of constructor.
	private AntialiasingLineRenderer() {}

	
	/*
	 * (non-Javadoc)
	 * @see client.LineRenderer#drawLine(client.Vertex2D, client.Vertex2D, windowing.Drawable)
	 * 
	 * @pre: p2.x >= p1.x && p2.y >= p1.y
	 */
	@Override
	public void drawLine(Vertex3D p1, Vertex3D p2, Drawable drawable) {
		double deltaX = p2.getIntX() - p1.getIntX();
		double deltaY = p2.getIntY() - p1.getIntY();
		
		double slope = deltaY / deltaX;
		double range = Math.sqrt(1+Math.pow(slope, 2));
		double y_top = p1.getIntY() + range;
		double y_button = p1.getIntY() - range;
		double dis = 0, d =0;
		double frac;
		int argbColor = p1.getColor().asARGB();
		
		for(int x = p1.getIntX(); x <= p2.getIntX(); x++)
		{
			for(int y = (int)Math.round(y_button); y <= (int)Math.round(y_top); y++)
			{
				dis = Math.abs(deltaY*x-deltaX*y+p2.getIntX()*p1.getIntY()-p1.getIntX()*p2.getIntY())/Math.sqrt(Math.pow(deltaY, 2)+Math.pow(deltaX, 2));
				d = Math.abs(dis-0.5);
				
				if(dis <= 0.5)
					frac = 1-(Math.acos(d)-d*Math.sqrt(1-Math.pow(d, 2)))/Math.PI;
				else frac = (Math.acos(d)-d*Math.sqrt(1-Math.pow(d, 2)))/Math.PI;
				
				drawable.setPixelWithCoverage(x, y, 0.0, argbColor, frac);
			}
			y_top += slope;
			y_button += slope;
		}
	}

	public static LineRenderer make() {
		return new AnyOctantLineRenderer(new AntialiasingLineRenderer());
	}
}