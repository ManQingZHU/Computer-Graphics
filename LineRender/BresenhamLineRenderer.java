package notProvided.line;

import geometry.Vertex3D;
import line.AnyOctantLineRenderer;
import line.LineRenderer;
import windowing.drawable.Drawable;

public class BresenhamLineRenderer implements LineRenderer {
	// use the static factory make() instead of constructor.
	private BresenhamLineRenderer() {}

	
	/*
	 * (non-Javadoc)
	 * @see client.LineRenderer#drawLine(client.Vertex2D, client.Vertex2D, windowing.Drawable)
	 * 
	 * @pre: p2.x >= p1.x && p2.y >= p1.y
	 */
	@Override
	public void drawLine(Vertex3D p1, Vertex3D p2, Drawable drawable) {
		int deltaX = p2.getIntX() - p1.getIntX();
		int deltaY = p2.getIntY() - p1.getIntY();
		
		int m_num = 2 * deltaY;
		int argbColor = p1.getColor().asARGB();
		int x = p1.getIntX();
		int y = p1.getIntY();
		int err = 2*deltaY - deltaX;
		int k = 2*deltaY - 2*deltaX;
		
		while(x <= p2.getIntX())
		{
			drawable.setPixel(x, y, 0.0, argbColor);
			x++;
			if(err >= 0)
			{
				err += k;
				y++;
			}
			else {
				err += m_num;
			}
		}
		
	}

	public static LineRenderer make() {
		return new AnyOctantLineRenderer(new BresenhamLineRenderer());
	}
}
