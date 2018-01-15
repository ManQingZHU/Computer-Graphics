package notProvided.polygon;

import geometry.Vertex3D;
import line.LineRenderer;
import notProvided.line.DDALineRenderer;
import notProvided.shading.Shader;
import polygon.Chain;
import polygon.Polygon;
import polygon.PolygonRenderer;
import windowing.drawable.Drawable;

public class WireframePolygonRenderer implements PolygonRenderer{
	private static LineRenderer edgeLineRenderer;
	private WireframePolygonRenderer() { 
		edgeLineRenderer = DDALineRenderer.make();
	}
	
	@Override
	public void drawPolygon(Polygon polygon, Drawable drawable, Shader shader) {
		int i, j;
		double sameY = polygon.get(0).getY();
		for(i = 1; i < polygon.length(); i++)
		{
			if(polygon.get(i).getY() != sameY)
				break;
		}
		
		if(i == polygon.length())   // solution for y=0 face pattern 
		{
			LineRenderer linerenderer = DDALineRenderer.make();
			for(i = 0, j = 1; j < polygon.length(); i = j, j++) {
				linerenderer.drawLine(polygon.get(i), polygon.get(j), drawable);
			}
			
			return;
		}
		
		Chain leftChain = polygon.leftChain();
		Chain rightChain =polygon.rightChain();
		Vertex3D top = leftChain.get(0);
		
		Vertex3D p1, p2;
		for(p1 = top, i = 1; i < leftChain.length(); p1 = p2, i++)
		{
			p2 = leftChain.get(i);
			edgeLineRenderer.drawLine(p1, p2, drawable);
		}
		
		for(p1 = top, i = 1; i < rightChain.length(); p1 = p2, i++)
		{
			p2 = rightChain.get(i);
			edgeLineRenderer.drawLine(p1, p2, drawable);
		}
	}

	public static WireframePolygonRenderer make()
	{
		return new WireframePolygonRenderer();
	}
}
