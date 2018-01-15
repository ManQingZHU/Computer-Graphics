package notProvided.line;

import geometry.Vertex3D;
import line.AnyOctantLineRenderer;
import line.LineRenderer;
import windowing.drawable.Drawable;

public class DDALineRenderer implements LineRenderer {
	// use the static factory make() instead of constructor.
		private DDALineRenderer() {}

		
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
			int argbColor = p1.getColor().asARGB();
			double y = p1.getIntY();
			
			for(int x = p1.getIntX(); x <= p2.getIntX(); x++) {
				drawable.setPixel(x, (int)Math.round(y), 0.0, argbColor);
				y += slope;
			}
		}

		public static LineRenderer make() {
			return new AnyOctantLineRenderer(new DDALineRenderer());
		}
}

