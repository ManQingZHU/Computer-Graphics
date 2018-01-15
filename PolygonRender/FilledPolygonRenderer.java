package notProvided.polygon;

import geometry.Point3DH;
import geometry.Vertex3D;
import line.LineRenderer;
import notProvided.line.DDALineRenderer;
import notProvided.shading.PhongShader;
import notProvided.shading.Shader;
import polygon.Chain;
import polygon.Polygon;
import polygon.PolygonRenderer;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

public class FilledPolygonRenderer implements PolygonRenderer{
	// assumes polygon is ccw.
	private FilledPolygonRenderer() { }

	public void drawPolygon(Polygon polygon, Drawable drawable, Shader Shader)
	{
		int i, j;
		double sameY = polygon.get(0).getY();
		for(i = 1; i < polygon.length(); i++)
		{
			if(polygon.get(i).getY() != sameY)
				break;
		}
		
		if(i == polygon.length())   // solution for y face pattern 
		{
			LineRenderer linerenderer = DDALineRenderer.make();
			for(i = 0, j = 1; j < polygon.length(); i = j, j++) {
				linerenderer.drawLine(polygon.get(i), polygon.get(j), drawable);
			}
			
			return;
		}
		Chain leftChain = polygon.leftChain();
		Chain rightChain =polygon.rightChain();
		Vertex3D bottom = leftChain.get(leftChain.length()-1);
		Vertex3D top = leftChain.get(0);
		Color color;
		
		
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
		
		Vertex3D A, B, C, vertex;
		Point3DH vector1, vector2, vector;
		double u, v, w;
		A = top;
		B = leftChain.get(1);
		C = rightChain.get(1);
		vector1 = B.getPoint3D().subtract(A.getPoint3D());
		vector2 = C.getPoint3D().subtract(A.getPoint3D());
		double area = vector1.crossMultiply2D(vector2).length();
		
		double z, r, g, b;
		Point3DH norm, nA, nB, nC;
		double csx, csy, csz;
		
		while(yT > bottom.getIntY()){
			yB = pL2.getIntY() > pR2.getIntY() ? pL2.getIntY(): pR2.getIntY();
			for(int y = (int)Math.round(yT); y > yB; y--) { 
				for(int x = (int)Math.round(xL);x < xR; x++)  
				{
					vector = new Point3DH(x, y, 1.0);
					vector = vector.subtract(A.getPoint3D());
					u = (vector.crossMultiply2D(vector2).length())/area;
					v = (vector.crossMultiply2D(vector1).length())/area;
					w = 1-u-v;
					
					z = w*A.getZ()+u*B.getZ()+v*C.getZ();
					
					r = w*A.getZ()*A.getColor().getR()+u*B.getZ()*B.getColor().getR()+v*C.getZ()*C.getColor().getR();
					b = w*A.getZ()*A.getColor().getB()+u*B.getZ()*B.getColor().getB()+v*C.getZ()*C.getColor().getB();
					g = w*A.getZ()*A.getColor().getG()+u*B.getZ()*B.getColor().getG()+v*C.getZ()*C.getColor().getG();
					color = new Color(r/z, g/z, b/z);
					
					nA = (Point3DH) A.getNormal().scale(w*A.getZ());
					nB = (Point3DH) B.getNormal().scale(u*B.getZ());
					nC = (Point3DH) C.getNormal().scale(v*C.getZ());
					norm = nA.add(nB);
					norm = norm.add(nC);
					norm = norm.scale(1.0/z);
					norm = norm.normalize();
					
					csx = w*A.getZ()*A.getCSX()+u*B.getZ()*B.getCSX()+v*C.getZ()*C.getCSX();
					csy = w*A.getZ()*A.getCSY()+u*B.getZ()*B.getCSY()+v*C.getZ()*C.getCSY();
					csx = csx/z;
					csy = csy/z;
					csz = 1.0/z;
					
					vertex = new Vertex3D(new Point3DH(x, y, z), new Point3DH(csx, csy, csz), norm, color);
					color = Shader.shade(vertex);
					drawable.setPixel(x, y, z, color.asARGB());
					
				}
				xL -= slopeL;
				xR -= slopeR;

			}

			if(pL2.getIntY() > pR2.getIntY())
			{
				pL1 = pL2;
				pL2 = leftChain.get(++i);
				if(slopeL == Double.POSITIVE_INFINITY || slopeL == Double.NEGATIVE_INFINITY)
				{
					xL = pL1.getIntX();
				}
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
				{
					xL = pL1.getIntX();				
				}
				
				deltaXL = pL1.getIntX() - pL2.getIntX();
				deltaYL = pL1.getIntY() - pL2.getIntY();
				slopeL = deltaXL/deltaYL;

				pR1 = pR2;
				j++;
				if(j >= rightChain.length())
					break;
				pR2 = rightChain.get(j);
				if(slopeR == Double.POSITIVE_INFINITY || slopeR == Double.NEGATIVE_INFINITY)
				{
					xR = pR1.getIntX();
				}
				deltaXR = pR1.getIntX() - pR2.getIntX();
				deltaYR = pR1.getIntY() - pR2.getIntY();
				slopeR = deltaXR/deltaYR;
			}
			else {
				pR1 = pR2;
				pR2 = rightChain.get(++j);
				if(slopeR == Double.POSITIVE_INFINITY || slopeR == Double.NEGATIVE_INFINITY)
				{
					xR = pR1.getIntX();
				}
				deltaXR = pR1.getIntX() - pR2.getIntX();
				deltaYR = pR1.getIntY() - pR2.getIntY();
				slopeR = deltaXR/deltaYR;
			}
			yT = yB;
		}
		
	}
	/*
	@Override
	public void drawPolygon(Polygon polygon, Drawable drawable, Shader shader)
	{
		
		int i, j;
		double sameY = polygon.get(0).getY();
		for(i = 1; i < polygon.length(); i++)
		{
			if(polygon.get(i).getY() != sameY)
				break;
		}
		
		if(i == polygon.length())   // solution for y face pattern 
		{
			LineRenderer linerenderer = DDALineRenderer.make();
			for(i = 0, j = 1; j < polygon.length(); i = j, j++) {
				linerenderer.drawLine(polygon.get(i), polygon.get(j), drawable);
			}
			
			return;
		}
		
		Vertex3D vertex;
		Chain leftChain = polygon.leftChain();
		Chain rightChain =polygon.rightChain();
		Vertex3D bottom = leftChain.get(leftChain.length()-1);
		Vertex3D top = leftChain.get(0);
		
		Vertex3D pL1, pL2, pR1, pR2;
		double deltaXL, deltaXR, deltaYL, deltaYR, deltaZL, deltaZR;
		double slopeL, slopeR, slopeZL, slopeZR;
		
		double deltaRL, deltaRR, deltaGL, deltaGR, deltaBL, deltaBR;
		double slopeRR, slopeGR, slopeBR, slopeRL, slopeGL, slopeBL;
		
		double xL, xR, yT, yB, zL, zR;
		double deltaX, deltaZ;
		double slopeZ;
		double z;
		
		double rL, gL, bL, rR, gR, bR;
		double deltaR,deltaG,deltaB;
		double slopeCr, slopeCg, slopeCb;
		double r, g, b;
		
		double deltaCXL, deltaCXR, deltaCYL, deltaCYR;
		double slopeCXL, slopeCXR, slopeCYL, slopeCYR;
		double cxL, cxR, cyL, cyR;
		double deltaCX, deltaCY;
		double slopeCX, slopeCY;
		double cx, cy, cz;
		Point3DH deltaNL, deltaNR, slopeNL, slopeNR;
		Point3DH nL, nR, deltaN, slopeN, n;

		i = 1; 
		j = 1;
		yT = top.getIntY();
		pL1 = pR1 = top;
		pL2 = leftChain.get(i);
		pR2 = rightChain.get(j);
		
		xL = pL1.getIntX();
		xR = pR1.getIntX();
		zL = pL1.getZ();
		zR = pR1.getZ();
		
		rL = pL1.getColor().getR()*zL;
		gL = pL1.getColor().getG()*zL;		
		bL = pL1.getColor().getB()*zL;
		
		rR = pR1.getColor().getR()*zR;
		gR = pR1.getColor().getG()*zR;
		bR = pR1.getColor().getB()*zR;

		deltaXL = pL1.getIntX() - pL2.getIntX();
		deltaYL = pL1.getIntY() - pL2.getIntY();
		slopeL = deltaXL/deltaYL;
		deltaZL = pL1.getZ() - pL2.getZ();
		slopeZL = deltaZL/deltaYL;
		
		deltaXR = pR1.getIntX() - pR2.getIntX();
		deltaYR = pR1.getIntY() - pR2.getIntY();
		slopeR = deltaXR/deltaYR;		
		deltaZR = pR1.getZ() - pR2.getZ();
		slopeZR = deltaZR/deltaYR;
		
		deltaRL = pL1.getColor().getR()*pL1.getZ() - pL2.getColor().getR()*pL2.getZ();
		deltaGL = pL1.getColor().getG()*pL1.getZ() - pL2.getColor().getG()*pL2.getZ();
		deltaBL = pL1.getColor().getB()*pL1.getZ() - pL2.getColor().getB()*pL2.getZ();
		slopeRL = deltaRL/deltaYL;
		slopeGL = deltaGL/deltaYL;
		slopeBL = deltaBL/deltaYL;
		
		deltaRR = pR1.getColor().getR()*pR1.getZ() - pR2.getColor().getR()*pR2.getZ();
		deltaGR = pR1.getColor().getG()*pR1.getZ() - pR2.getColor().getG()*pR2.getZ();
		deltaBR = pR1.getColor().getB()*pR1.getZ() - pR2.getColor().getB()*pR2.getZ();
		slopeRR = deltaRR/deltaYR;
		slopeGR = deltaGR/deltaYR;
		slopeBR = deltaBR/deltaYR;
		
		cxL = pL1.getCSX()*zL;
		cyL = pL1.getCSY()*zL;
		deltaCXL = pL1.getCSX()*pL1.getZ() - pL2.getCSX()*pL2.getZ();
		deltaCYL = pL1.getCSY()*pL1.getZ() - pL2.getCSY()*pL2.getZ();
		slopeCXL = deltaCXL/deltaYL;
		slopeCYL = deltaCYL/deltaYL;
		
		cxR = pR1.getCSX()*zR;
		cyR = pR1.getCSY()*zR;
		deltaCXR = pR1.getCSX()*pR1.getZ() - pR2.getCSX()*pR2.getZ();
		deltaCYR = pR1.getCSY()*pR1.getZ() - pR2.getCSY()*pR2.getZ();
		slopeCXR = deltaCXR/deltaYR;
		slopeCYR = deltaCYR/deltaYR;
		Color color;
		
		nL = (Point3DH) pL1.getNormal().scale(zL);
		deltaNL = (Point3DH) pL1.getNormal().scale(pL1.getZ()).subtract(pL2.getNormal().scale(pL2.getZ()));
		slopeNL = deltaNL.scale(1.0/deltaYL);
		
		nR = (Point3DH) pR1.getNormal().scale(zR);
		deltaNR = (Point3DH) pR1.getNormal().scale(pR1.getZ()).subtract(pR2.getNormal().scale(pR2.getZ()));
		slopeNR = deltaNR.scale(1.0/deltaYR);
		
		//TODO
		while(yT > bottom.getIntY()){
			yB = pL2.getIntY() > pR2.getIntY() ? pL2.getIntY(): pR2.getIntY();
			for(int y = (int)Math.round(yT); y > yB; y--) { 	
				deltaX = xR-xL;
				deltaZ = zR-zL;
				
				deltaR = rR-rL;
				deltaG = gR-gL;
				deltaB = bR-bL;
				slopeZ = deltaZ/deltaX;
				slopeCr = deltaR/deltaX;
				slopeCg = deltaG/deltaX;
				slopeCb = deltaB/deltaX;
				
				z = zL;
				r = rL;
				g = gL;
				b = bL;
				
				deltaN = nR.subtract(nL);
				slopeN = deltaN.scale(1.0/deltaX);
				n = nL;
				
				deltaCX = cxR - cxL;
				deltaCY = cyR - cyL;
				slopeCX = deltaCX/deltaX;
				slopeCY = deltaCY/deltaX;
				cx = cxL;
				cy = cyL;
				 
				for(int x = (int)Math.round(xL);x < xR; x++)  
				{
					cz = 1.0/z;
					
					vertex = new Vertex3D(new Point3DH(x, y, z), new Point3DH(cx/z, cy/z, cz), n.scale(1.0/z).normalize(), new Color(r/z, g/z, b/z));
					color = shader.shade(vertex);
					drawable.setPixel(x, y, z, color.asARGB());
					
					z += slopeZ;
					
					r += slopeCr;
					g += slopeCg;
					b += slopeCb;
					
					n = n.add(slopeN);
					cx += slopeCX;
					cy += slopeCY;
				}
				xL -= slopeL;
				xR -= slopeR;
				zL -= slopeZL;
				zR -= slopeZR;
				
				rL -= slopeRL;
				rR -= slopeRR;
				gL -= slopeGL;
				gR -= slopeGR;
				bL -= slopeBL;
				bR -= slopeBR;
				
				nL = nL.subtract(slopeNL);
				nR = nR.subtract(slopeNR);
				cxL -= slopeCXL;
				cyL -= slopeCYL;
				cxR -= slopeCXR;
				cyR -= slopeCYR;
			}

			if(pL2.getIntY() > pR2.getIntY())
			{
				pL1 = pL2;
				pL2 = leftChain.get(++i);
				if(slopeL == Double.POSITIVE_INFINITY || slopeL == Double.NEGATIVE_INFINITY)
				{
					xL = pL1.getIntX();
					zL = pL1.getZ();
					
					rL = pL1.getColor().getR()*zL;
					gL = pL1.getColor().getG()*zL;		
					bL = pL1.getColor().getB()*zL;	
		
					nL = (Point3DH) pL1.getNormal().scale(zL);
					
					cxL = pL1.getCSX()*zL;
					cyL = pL1.getCSY()*zL;
				}
				deltaXL = pL1.getIntX() - pL2.getIntX();
				deltaYL = pL1.getIntY() - pL2.getIntY();
				slopeL = deltaXL/deltaYL;				
				deltaZL = pL1.getZ() - pL2.getZ();
				slopeZL = deltaZL/deltaYL;
				
				deltaRL = pL1.getColor().getR()*pL1.getZ() - pL2.getColor().getR()*pL2.getZ();
				deltaGL = pL1.getColor().getG()*pL1.getZ() - pL2.getColor().getG()*pL2.getZ();
				deltaBL = pL1.getColor().getB()*pL1.getZ() - pL2.getColor().getB()*pL2.getZ();
				slopeRL = deltaRL/deltaYL;
				slopeGL = deltaGL/deltaYL;
				slopeBL = deltaBL/deltaYL;
				
				deltaNL = (Point3DH) pL1.getNormal().scale(pL1.getZ()).subtract(pL2.getNormal().scale(pL2.getZ()));
				slopeNL = deltaNL.scale(1.0/deltaYL);
				
				deltaCXL = pL1.getCSX()*pL1.getZ() - pL2.getCSX()*pL2.getZ();
				deltaCYL = pL1.getCSY()*pL1.getZ() - pL2.getCSY()*pL2.getZ();
				slopeCXL = deltaCXL/deltaYL;
				slopeCYL = deltaCYL/deltaYL;
			}
			else if(pL2.getIntY() == pR2.getIntY())
			{
				pL1 = pL2;
				i++;
				if(i >= leftChain.length())
					break;
				pL2 = leftChain.get(i);
				if(slopeL == Double.POSITIVE_INFINITY || slopeL == Double.NEGATIVE_INFINITY)
				{
					xL = pL1.getIntX();
					zL = pL1.getZ();
					
					rL = pL1.getColor().getR()*zL;
					gL = pL1.getColor().getG()*zL;		
					bL = pL1.getColor().getB()*zL;	
		
					nL = (Point3DH) pL1.getNormal().scale(zL);
					
					cxL = pL1.getCSX()*zL;
					cyL = pL1.getCSY()*zL;
				}
				deltaXL = pL1.getIntX() - pL2.getIntX();
				deltaYL = pL1.getIntY() - pL2.getIntY();
				slopeL = deltaXL/deltaYL;				
				deltaZL = pL1.getZ() - pL2.getZ();
				slopeZL = deltaZL/deltaYL;
				
				deltaRL = pL1.getColor().getR()*pL1.getZ() - pL2.getColor().getR()*pL2.getZ();
				deltaGL = pL1.getColor().getG()*pL1.getZ() - pL2.getColor().getG()*pL2.getZ();
				deltaBL = pL1.getColor().getB()*pL1.getZ() - pL2.getColor().getB()*pL2.getZ();
				slopeRL = deltaRL/deltaYL;
				slopeGL = deltaGL/deltaYL;
				slopeBL = deltaBL/deltaYL;
				
				deltaNL = (Point3DH) pL1.getNormal().scale(pL1.getZ()).subtract(pL2.getNormal().scale(pL2.getZ()));
				slopeNL = deltaNL.scale(1.0/deltaYL);
				
				deltaCXL = pL1.getCSX()*pL1.getZ() - pL2.getCSX()*pL2.getZ();
				deltaCYL = pL1.getCSY()*pL1.getZ() - pL2.getCSY()*pL2.getZ();
				slopeCXL = deltaCXL/deltaYL;
				slopeCYL = deltaCYL/deltaYL;
				
				pR1 = pR2;
				j++;
				if(j >= rightChain.length())
					break;
				pR2 = rightChain.get(j);
				if(slopeR == Double.POSITIVE_INFINITY || slopeR == Double.NEGATIVE_INFINITY)
				{
					xR = pR1.getIntX();
					zR = pR1.getZ();
					
					rR = pR1.getColor().getR()*zR;
					gR = pR1.getColor().getG()*zR;
					bR = pR1.getColor().getB()*zR;
					
					nR = (Point3DH) pR1.getNormal().scale(zR);
					
					cxR = pR1.getCSX()*zR;
					cyR = pR1.getCSY()*zR;
				}
				deltaXR = pR1.getIntX() - pR2.getIntX();
				deltaYR = pR1.getIntY() - pR2.getIntY();
				slopeR = deltaXR/deltaYR;
				
				deltaZR = pR1.getZ() - pR2.getZ();
				slopeZR = deltaZR/deltaYR;
				
				deltaRR = pR1.getColor().getR()*pR1.getZ() - pR2.getColor().getR()*pR2.getZ();
				deltaGR = pR1.getColor().getG()*pR1.getZ() - pR2.getColor().getG()*pR2.getZ();
				deltaBR = pR1.getColor().getB()*pR1.getZ() - pR2.getColor().getB()*pR2.getZ();
				slopeRR = deltaRR/deltaYR;
				slopeGR = deltaGR/deltaYR;
				slopeBR = deltaBR/deltaYR;
				
				deltaNR = (Point3DH) pR1.getNormal().scale(pR1.getZ()).subtract(pR2.getNormal().scale(pR2.getZ()));
				slopeNR = deltaNR.scale(1.0/deltaYR);
				
				deltaCXR = pR1.getCSX()*pR1.getZ() - pR2.getCSX()*pR2.getZ();
				deltaCYR = pR1.getCSY()*pR1.getZ() - pR2.getCSY()*pR2.getZ();
				slopeCXR = deltaCXR/deltaYR;
				slopeCYR = deltaCYR/deltaYR;
			}
			else {
				pR1 = pR2;
				pR2 = rightChain.get(++j);
				if(slopeR == Double.POSITIVE_INFINITY || slopeR == Double.NEGATIVE_INFINITY)
				{
					xR = pR1.getIntX();
					zR = pR1.getZ();
					
					rR = pR1.getColor().getR()*zR;
					gR = pR1.getColor().getG()*zR;
					bR = pR1.getColor().getB()*zR;
					
					nR = (Point3DH) pR1.getNormal().scale(zR);
					
					cxR = pR1.getCSX()*zR;
					cyR = pR1.getCSY()*zR;
				}
				deltaXR = pR1.getIntX() - pR2.getIntX();
				deltaYR = pR1.getIntY() - pR2.getIntY();
				slopeR = deltaXR/deltaYR;
				
				deltaZR = pR1.getZ() - pR2.getZ();
				slopeZR = deltaZR/deltaYR;
				
				deltaRR = pR1.getColor().getR()*pR1.getZ() - pR2.getColor().getR()*pR2.getZ();
				deltaGR = pR1.getColor().getG()*pR1.getZ() - pR2.getColor().getG()*pR2.getZ();
				deltaBR = pR1.getColor().getB()*pR1.getZ() - pR2.getColor().getB()*pR2.getZ();
				slopeRR = deltaRR/deltaYR;
				slopeGR = deltaGR/deltaYR;
				slopeBR = deltaBR/deltaYR;
				
				deltaNR = (Point3DH) pR1.getNormal().scale(pR1.getZ()).subtract(pR2.getNormal().scale(pR2.getZ()));
				slopeNR = deltaNR.scale(1.0/deltaYR);
				
				deltaCXR = pR1.getCSX()*pR1.getZ() - pR2.getCSX()*pR2.getZ();
				deltaCYR = pR1.getCSY()*pR1.getZ() - pR2.getCSY()*pR2.getZ();
				slopeCXR = deltaCXR/deltaYR;
				slopeCYR = deltaCYR/deltaYR;
			}
			yT = yB;
		}
		
	}
*/
	public static FilledPolygonRenderer make()
	{
		return new FilledPolygonRenderer();
	}
}

