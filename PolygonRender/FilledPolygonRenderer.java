package notProvided.polygon;

import geometry.Vertex3D;
import polygon.Chain;
import polygon.Polygon;
import polygon.PolygonRenderer;
import polygon.Shader;
import windowing.drawable.Drawable;

public class FilledPolygonRenderer implements PolygonRenderer{
	// assumes polygon is ccw.
	private FilledPolygonRenderer() { }
	
	@Override
	public void drawPolygon(Polygon polygon, Drawable drawable, Shader vertexShader)
	{
		Chain leftChain = polygon.leftChain();
		Chain rightChain =polygon.rightChain();
		Vertex3D bottom = leftChain.get(leftChain.length()-1);
		Vertex3D top = leftChain.get(0);
		int argbColor = top.getColor().asARGB();
		
		int i, j;
		Vertex3D pL1, pL2, pR1, pR2;
		double deltaXL, deltaXR, deltaYL, deltaYR, slopeL, slopeR;
		double xL, xR, yT, yB;

		i = 1; 
		j = 1;
		yT = top.getIntY();
		pL1 = pR1 = top;
		xL = pL1.getIntX();
		xR = pR1.getIntX();
		pL2 = leftChain.get(i);
		pR2 = rightChain.get(j);

		deltaXL = pL1.getIntX() - pL2.getIntX();
		deltaYL = pL1.getIntY() - pL2.getIntY();
		slopeL = deltaXL/deltaYL;
		
		deltaXR = pR1.getIntX() - pR2.getIntX();
		deltaYR = pR1.getIntY() - pR2.getIntY();
		slopeR = deltaXR/deltaYR;
		
		while(yT > bottom.getIntY()){
			yB = pL2.getIntY() > pR2.getIntY() ? pL2.getIntY(): pR2.getIntY();
			for(int y = (int)Math.round(yT); y >= (int)Math.round(yB+1); y--) {
				for(int x = (int)Math.round(xL); x <= (int)Math.round(xR-1); x++)
				{
					drawable.setPixel(x, y, 0.0, argbColor); 
				}
				xL -= slopeL;
				xR -= slopeR;
			}

			if(pL2.getIntY() > pR2.getIntY())
			{
				pL1 = pL2;
				pL2 = leftChain.get(++i);
				if(slopeL == Double.POSITIVE_INFINITY || slopeL == Double.NEGATIVE_INFINITY)
					xL = pL1.getIntX();
				deltaXL = pL1.getIntX() - pL2.getIntX();
				deltaYL = pL1.getIntY() - pL2.getIntY();
				slopeL = deltaXL/deltaYL;
			}
			else if(pL2.getIntY() == pR2.getIntY())
			{
				pL1 = pL2;
				i++;
				if(i >= leftChain.length())
					break;
				pL2 = leftChain.get(i);
				if(slopeL == Double.POSITIVE_INFINITY || slopeL == Double.NEGATIVE_INFINITY)
					xL = pL1.getIntX();
				
				deltaXL = pL1.getIntX() - pL2.getIntX();
				deltaYL = pL1.getIntY() - pL2.getIntY();
				slopeL = deltaXL/deltaYL;
				
				pR1 = pR2;
				j++;
				if(j >= rightChain.length())
					break;
				pR2 = rightChain.get(j);
				if(slopeR == Double.POSITIVE_INFINITY || slopeR == Double.NEGATIVE_INFINITY)
					xR = pR1.getIntX();
				deltaXR = pR1.getIntX() - pR2.getIntX();
				deltaYR = pR1.getIntY() - pR2.getIntY();
				slopeR = deltaXR/deltaYR;
			}
			else {
				pR1 = pR2;
				pR2 = rightChain.get(++j);
				if(slopeR == Double.POSITIVE_INFINITY || slopeR == Double.NEGATIVE_INFINITY)
					xR = pR1.getIntX();
				deltaXR = pR1.getIntX() - pR2.getIntX();
				deltaYR = pR1.getIntY() - pR2.getIntY();
				slopeR = deltaXR/deltaYR;
			}
			yT = yB;
		}
		
	}
	
	public static FilledPolygonRenderer make()
	{
		return new FilledPolygonRenderer();
	}
}

