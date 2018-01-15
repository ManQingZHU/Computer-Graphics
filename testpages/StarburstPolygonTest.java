package notProvided.client.testpages;

import geometry.Vertex3D;
import polygon.Polygon;
import polygon.PolygonRenderer;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

public class StarburstPolygonTest {
	private static final int NUM_TRIS = 90;
	private static final double radius = 125;
	private static final double angleDifference = (2.0 * Math.PI) / NUM_TRIS;
	
	private final Drawable panel;
	private final PolygonRenderer renderer;
	Vertex3D center;
	
	public StarburstPolygonTest(Drawable panel, PolygonRenderer renderer) {
		this.panel = panel;
		this.renderer = renderer;
		
		makeCenter();
		render();
	}
	
	private void makeCenter() {
		int centerX = panel.getWidth() / 2;
		int centerY = panel.getHeight() / 2;
		center = new Vertex3D(centerX, centerY, 0, Color.WHITE);
	}
	
	private void render() {		
		double angle = 0.0;
		
		for(int tri = 0; tri < NUM_TRIS; tri++) {
			Color color = Color.random();
			Polygon radialTri = radialTriangle(angle, color);
			renderer.drawPolygon(radialTri, panel);
			
			angle += angleDifference;
		}
	}
	
	private Vertex3D radialPoint(double angle, Color color) {
		double x = center.getX() + radius * Math.cos(angle);
		double y = center.getY() + radius * Math.sin(angle);
		return new Vertex3D(x, y, 0, color);
	}
	
	private Polygon radialTriangle(double angle, Color color) {
		Vertex3D initialVertices[];
		initialVertices = new Vertex3D[3];
		initialVertices[0] = new Vertex3D(center.getX(), center.getY(), 0.0, color);
		initialVertices[1] = radialPoint(angle, color);
		initialVertices[2] = radialPoint(angle+angleDifference, color);
		
		return Polygon.make(initialVertices);
	}
}
