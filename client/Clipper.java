package notProvided.client;

import java.util.Iterator;

import geometry.Point3DH;
import geometry.Vertex3D;
import polygon.Polygon;
import sun.net.www.content.text.plain;
import windowing.graphics.Color;

public class Clipper {
	private double znear;
	private double zfar;
	
	private double x[];
	private double y[];
	private final int NEAR_LOW = 0;
	private final int NEAR_HIGH = 1;
	private final int FAR_LOW = 2;
	private final int FAR_HIGH = 3; 
	
	private double A[];
	private double B[];
	private double C[];
	private double D[];
	private final int RIGHT = 0;
	private final int LEFT = 1;
	private final int TOP = 2;
	private final int DOWN = 3; 
	
	public Clipper() {
		x = new double[4];
		y = new double[4];
		
		A = new double[4];
		B = new double[4];
		C = new double[4];
		D = new double[4];
	}
	public void setClipperParam(double csz_near, double csz_far, double vwx_low, double vwx_high, double vwy_low, double vwy_high) {
		znear = csz_near;
		zfar = csz_far;
		x[NEAR_LOW] = -znear*vwx_low;
		x[FAR_LOW] = -zfar*vwx_low;
		x[NEAR_HIGH] = -znear*vwx_high;
		x[FAR_HIGH] = -zfar*vwx_high;
		
		y[NEAR_LOW] = -znear*vwy_low;
		y[FAR_LOW] = -zfar*vwy_low;
		y[NEAR_HIGH] = -znear*vwy_high;
		y[FAR_HIGH] = -zfar*vwy_high;
		
		A[RIGHT] = (y[NEAR_HIGH]-y[NEAR_LOW])*(znear-zfar);
		B[RIGHT] = 0;
		C[RIGHT] = (y[NEAR_HIGH]-y[NEAR_LOW])*(x[FAR_HIGH]-x[NEAR_HIGH]);
		D[RIGHT] = -A[RIGHT]*x[NEAR_HIGH]-C[RIGHT]*znear;
		
		A[LEFT] = -(y[NEAR_HIGH]-y[NEAR_LOW])*(znear-zfar);
		B[LEFT] = 0;
		C[LEFT] = -(y[NEAR_HIGH]-y[NEAR_LOW])*(x[FAR_LOW]-x[NEAR_LOW]);
		D[LEFT] = -A[LEFT]*x[NEAR_LOW]-C[LEFT]*znear;
		
		A[TOP] = 0;
		B[TOP] = (x[NEAR_HIGH]-x[NEAR_LOW])*(znear-zfar);
		C[TOP] = (x[NEAR_HIGH]-x[NEAR_LOW])*(y[FAR_HIGH]-y[NEAR_HIGH]);
		D[TOP] = -B[TOP]*y[NEAR_HIGH]-C[TOP]*znear;
		
		A[DOWN] = 0;
		B[DOWN] = -(x[NEAR_HIGH]-x[NEAR_LOW])*(znear-zfar);
		C[DOWN] = -(x[NEAR_HIGH]-x[NEAR_LOW])*(y[FAR_LOW]-y[NEAR_LOW]);
		D[DOWN] = -B[DOWN]*y[NEAR_LOW]-C[DOWN]*znear;
	}
	
	public Vertex3D[] LineClipping3D(Vertex3D p1, Vertex3D p2) {
		Vertex3D CULLED[] = null;
		double t;
		double x1 = p1.getX();
		double y1 = p1.getY();
		double z1 = p1.getZ();
		double x2 = p2.getX();
		double y2 = p2.getY();
		double z2 = p2.getZ();
		double deltaX = x2 - x1;
		double deltaY = y2 - y1;
		double deltaZ = z2 - z1;
		
		double r1, r2, g1, g2, b1, b2;
		r1 = p1.getColor().getR();
		r2 = p2.getColor().getR();
		g1 = p1.getColor().getG();
		g2 = p2.getColor().getG();
		b1 = p1.getColor().getB();
		b2 = p2.getColor().getB();
		double deltaR, deltaG, deltaB;
		deltaR = r2 - r1;
		deltaG = g2 - g1;
		deltaB = b2 - b1;
		
		// front and back
		if(z1 < zfar) {
			if(z2 < zfar)
				return CULLED;
			
			t = (zfar-z1)/deltaZ;
			z1 = znear;
			y1 += t*deltaY;
			x1 += t*deltaX;
			r1 += t*deltaR;
			g1 += t*deltaG;
			b1 += t*deltaB;
			
			if(znear < z2)
			{
				t = (znear - z1)/deltaZ;
				z2 = znear;
				y2 = y1+ t*deltaY;
				x2 = x1+ t*deltaX;
				r2 = r1+ t*deltaR;
				g2 = g1+ t*deltaG;
				b2 = b1+ t*deltaB;
			}
		}
		else if(z1 <= znear) {
			if(z2 < zfar) {
				t = (zfar-z1)/deltaZ;
				z2 = zfar;
				y2 = y1 + t*deltaY;
				x2 = x1 + t*deltaX;
				r2 = r1+ t*deltaR;
				g2 = g1+ t*deltaG;
				b2 = b1+ t*deltaB;
			}
			else if(znear < z2) {
				t = (znear - z1)/deltaZ;
				z2 = znear;
				y2 = y1+ t*deltaY;
				x2 = x1+ t*deltaX;
				r2 = r1+ t*deltaR;
				g2 = g1+ t*deltaG;
				b2 = b1+ t*deltaB;
			}
		}
		else { 
			if(znear < z2)
				return CULLED;
			
			t = (znear - z1)/deltaZ;
			z1 = znear;
			y1 += t*deltaY;
			x1 += t*deltaX;
			r1 += t*deltaR;
			g1 += t*deltaG;
			b1 += t*deltaB;
			
			if(z2 < zfar) {
				t = (zfar-z1)/deltaZ;
				z2 = zfar;
				y2 = y1 + t*deltaY;
				x2 = x1 + t*deltaX;
				r2 = r1+ t*deltaR;
				g2 = g1+ t*deltaG;
				b2 = b1+ t*deltaB;
			}
		}
		
		// right and left
		
		if(A[LEFT]*x1+C[LEFT]*z1+D[LEFT] > 0)
		{
			if(A[LEFT]*x2+C[LEFT]*z2+D[LEFT] > 0)
				return CULLED;
			
			t = GetT(deltaX, deltaY, deltaZ, x1, y1, z1, LEFT);
			x1 += t*deltaX;
			y1 += t*deltaY;
			z1 += t*deltaZ;
			r1 += t*deltaR;
			g1 += t*deltaG;
			b1 += t*deltaB;
			
			if(A[RIGHT]*x2+C[RIGHT]*z2+D[RIGHT] > 0) {
				t = GetT(deltaX, deltaY, deltaZ, x1, y1, z1, RIGHT);
				x2 = x1 +t*deltaX;
				y2 = y1+ t*deltaY;
				z2 = z1+ t*deltaZ;
				r2 = r1+ t*deltaR;
				g2 = g1+ t*deltaG;
				b2 = b1+ t*deltaB;
			}
			
		}
		else if(A[RIGHT]*x1+C[RIGHT]*z1+D[RIGHT] <= 0) {
			if(A[LEFT]*x2+C[LEFT]*z2+D[LEFT] > 0) {
				t = GetT(deltaX, deltaY, deltaZ, x1, y1, z1, LEFT);
				x2 = x1 +t*deltaX;
				y2 = y1+ t*deltaY;
				z2 = z1+ t*deltaZ;
				r2 = r1+ t*deltaR;
				g2 = g1+ t*deltaG;
				b2 = b1+ t*deltaB;
			}
			else if(A[RIGHT]*x2+C[RIGHT]*z2+D[RIGHT] > 0) {
				t = GetT(deltaX, deltaY, deltaZ, x1, y1, z1, RIGHT);
				x2 = x1 +t*deltaX;
				y2 = y1+ t*deltaY;
				z2 = z1+ t*deltaZ;
				r2 = r1+ t*deltaR;
				g2 = g1+ t*deltaG;
				b2 = b1+ t*deltaB;
			}
		}
		else {
			if(A[RIGHT]*x2+C[RIGHT]*z2+D[RIGHT] > 0)
				return CULLED;
			
			t = GetT(deltaX, deltaY, deltaZ, x1, y1, z1, RIGHT);
			x1 += t*deltaX;
			y1 += t*deltaY;
			z1 += t*deltaZ;
			r1 += t*deltaR;
			g1 += t*deltaG;
			b1 += t*deltaB;
			
			if(A[LEFT]*x2+C[LEFT]*z2+D[LEFT] > 0) {
				t = GetT(deltaX, deltaY, deltaZ, x1, y1, z1, LEFT);
				x2 = x1 +t*deltaX;
				y2 = y1+ t*deltaY;
				z2 = z1+ t*deltaZ;
				r2 = r1+ t*deltaR;
				g2 = g1+ t*deltaG;
				b2 = b1+ t*deltaB;
			}
		}

		// top and buttom
		if(B[DOWN]*y1+C[DOWN]*z1+D[DOWN] > 0) {
			if(B[DOWN]*y2+C[DOWN]*z2+D[DOWN] > 0)
				return CULLED;
			
			t = GetT(deltaX, deltaY, deltaZ, x1, y1, z1, DOWN);
			x1 += t*deltaX;
			y1 += t*deltaY;
			z1 += t*deltaZ;
			r1 += t*deltaR;
			g1 += t*deltaG;
			b1 += t*deltaB;
			
			if(B[TOP]*y2+C[TOP]*z2+D[TOP] > 0) {
				t = GetT(deltaX, deltaY, deltaZ, x1, y1, z1, TOP);
				x2 = x1 +t*deltaX;
				y2 = y1+ t*deltaY;
				z2 = z1+ t*deltaZ;
				r2 = r1+ t*deltaR;
				g2 = g1+ t*deltaG;
				b2 = b1+ t*deltaB;
			}
		}
		else if(B[TOP]*y1+C[TOP]*z1+D[TOP] <= 0) {
			if(B[DOWN]*y2+C[DOWN]*z2+D[DOWN] > 0) {
				t = GetT(deltaX, deltaY, deltaZ, x1, y1, z1, DOWN);
				x2 = x1 +t*deltaX;
				y2 = y1+ t*deltaY;
				z2 = z1+ t*deltaZ;
				r2 = r1+ t*deltaR;
				g2 = g1+ t*deltaG;
				b2 = b1+ t*deltaB;
			}
			else if(B[TOP]*y2+C[TOP]*z2+D[TOP] > 0) {
				t = GetT(deltaX, deltaY, deltaZ, x1, y1, z1, TOP);
				x2 = x1 +t*deltaX;
				y2 = y1+ t*deltaY;
				z2 = z1+ t*deltaZ;
				r2 = r1+ t*deltaR;
				g2 = g1+ t*deltaG;
				b2 = b1+ t*deltaB;
			}
		}
		else {
			if(B[TOP]*y2+C[TOP]*z2+D[TOP] > 0)
				return CULLED;
			
			t = GetT(deltaX, deltaY, deltaZ, x1, y1, z1, TOP);
			x1 += t*deltaX;
			y1 += t*deltaY;
			z1 += t*deltaZ;
			r1 += t*deltaR;
			g1 += t*deltaG;
			b1 += t*deltaB;
			
			if(B[TOP]*y2+C[TOP]*z2+D[TOP] > 0) {
				t = GetT(deltaX, deltaY, deltaZ, x1, y1, z1, TOP);
				x2 = x1 +t*deltaX;
				y2 = y1+ t*deltaY;
				z2 = z1+ t*deltaZ;
				r2 = r1+ t*deltaR;
				g2 = g1+ t*deltaG;
				b2 = b1+ t*deltaB;
			}
		}

		p1 = new Vertex3D(new Point3DH(x1, y1, z1), p1.getColor());
		p2 = new Vertex3D(new Point3DH(x2, y2, z2), p2.getColor());
		Vertex3D vertices[] = new Vertex3D[2];
		vertices[0] = p1;
		vertices[1] = p2;
		
		return vertices;
	}
	
	private double GetT(double deltaX,double deltaY, double deltaZ, double x1, double y1, double z1, int dir) {
		return -(A[dir]*x1+B[dir]*y1+C[dir]*z1+D[dir])/(A[dir]*deltaX+B[dir]*deltaY+C[dir]*deltaZ);
	}
	
	public Vertex3D[] LineClipping(Vertex3D p1, Vertex3D p2, int xL, int xR, int yB, int yT, int zB, int zF) {
		Vertex3D CULLED[] = null;
		double t;
		double x1 = p1.getX();
		double y1 = p1.getY();
		double z1 = p1.getZ();
		double x2 = p2.getX();
		double y2 = p2.getY();
		double z2 = p2.getZ();
		double r1, r2, g1, g2, b1, b2;
		r1 = p1.getColor().getR();
		r2 = p2.getColor().getR();
		g1 = p1.getColor().getG();
		g2 = p2.getColor().getG();
		b1 = p1.getColor().getB();
		b2 = p2.getColor().getB();
		double deltaX = x2 - x1;
		double deltaY = y2 - y1;
		double deltaZ = z2 - z1;
		double deltaR, deltaG, deltaB;
		deltaR = r2 - r1;
		deltaG = g2 - g1;
		deltaB = b2 - b1;
		if(x1 < xL) {
			if(x2 < xL)
				return CULLED;
			
			t = (xL-x1)/deltaX;
			x1 = xL;
			y1 += t*deltaY;
			z1 += t*deltaZ;
			r1 += t*deltaR;
			g1 += t*deltaG;
			b1 += t*deltaB;
			if(xR < x2)
			{
				t = (xR - x1)/deltaX;
				x2 = xR;
				y2 = y1+ t*deltaY;
				z2 = z1+ t*deltaZ;
				r2 = r1+ t*deltaR;
				g2 = g1+ t*deltaG;
				b2 = b1+ t*deltaB;
			}
		}
		else if(x1 <= xR) {
			if(x2 < xL) {
				t = (xL-x1)/deltaX;
				x2 = xL;
				y2 = y1 + t*deltaY;
				z2 = z1 + t*deltaZ;
				r2 = r1+ t*deltaR;
				g2 = g1+ t*deltaG;
				b2 = b1+ t*deltaB;
			}
			else if(xR < x2) {
				t = (xR - x1)/deltaX;
				x2 = xR;
				y2 = y1+ t*deltaY;
				z2 = z1+ t*deltaZ;
				r2 = r1+ t*deltaR;
				g2 = g1+ t*deltaG;
				b2 = b1+ t*deltaB;
			}
		}
		else { // xR < x1
			if(xR < x2)
				return CULLED;
			
			t = (xR - x1)/deltaX;
			x1 = xR;
			y1 += t*deltaY;
			z1 += t*deltaZ;
			r1 += t*deltaR;
			g1 += t*deltaG;
			b1 += t*deltaB;
			
			if(x2 < xL) {
				t = (xL-x1)/deltaX;
				x2 = xL;
				y2 = y1 + t*deltaY;
				z2 = z1 + t*deltaZ;
				r2 = r1+ t*deltaR;
				g2 = g1+ t*deltaG;
				b2 = b1+ t*deltaB;
			}
		}
		
		if(y1 < yB) {
			if(y2 < yB)
				return CULLED;
			
			t = (yB-y1)/deltaY;
			y1 = yB;
			x1 += t*deltaX;
			z1 += t*deltaZ;
			r1 += t*deltaR;
			g1 += t*deltaG;
			b1 += t*deltaB;
			
			if(yT < y2)
			{
				t = (yT - y1)/deltaY;
				y2 = yT;
				x2 = x1+ t*deltaX;
				z2 = z1+ t*deltaZ;
				r2 = r1+ t*deltaR;
				g2 = g1+ t*deltaG;
				b2 = b1+ t*deltaB;
			}
		}
		else if(y1 <= yT) {
			if(y2 < yB) {
				t = (yB-y1)/deltaY;
				y2 = yB;
				x2 = x1 + t*deltaX;
				z2 = z1 + t*deltaZ;
				r2 = r1+ t*deltaR;
				g2 = g1+ t*deltaG;
				b2 = b1+ t*deltaB;
			}
			else if(yT < y2) {
				t = (yT - y1)/deltaY;
				y2 = yT;
				x2 = x1+ t*deltaX;
				z2 = z1+ t*deltaZ;
				r2 = r1+ t*deltaR;
				g2 = g1+ t*deltaG;
				b2 = b1+ t*deltaB;
			}
		}
		else { // yT < y1
			if(yT < y2)
				return CULLED;
			
			t = (yT - y1)/deltaY;
			y1 = yT;
			x1 += t*deltaX;
			z1 += t*deltaZ;
			r1 += t*deltaR;
			g1 += t*deltaG;
			b1 += t*deltaB;
			
			if(y2 < yB) {
				t = (yB-y1)/deltaY;
				y2 = yB;
				x2 = x1 + t*deltaX;
				z2 = z1 + t*deltaZ;
				r2 = r1+ t*deltaR;
				g2 = g1+ t*deltaG;
				b2 = b1+ t*deltaB;
			}
		}
		
		if(z1 < zB) {
			if(z2 < zB)
				return CULLED;
			
			t = (zB-z1)/deltaZ;
			z1 = zF;
			y1 += t*deltaY;
			x1 += t*deltaX;
			r1 += t*deltaR;
			g1 += t*deltaG;
			b1 += t*deltaB;
			
			if(zF < z2)
			{
				t = (zF - z1)/deltaZ;
				z2 = zF;
				y2 = y1+ t*deltaY;
				x2 = x1+ t*deltaX;
				r2 = r1+ t*deltaR;
				g2 = g1+ t*deltaG;
				b2 = b1+ t*deltaB;
			}
		}
		else if(z1 <= zF) {
			if(z2 < zB) {
				t = (zB-z1)/deltaZ;
				z2 = zB;
				y2 = y1 + t*deltaY;
				x2 = x1 + t*deltaX;
				r2 = r1+ t*deltaR;
				g2 = g1+ t*deltaG;
				b2 = b1+ t*deltaB;
			}
			else if(zF < z2) {
				t = (zF - z1)/deltaZ;
				z2 = zF;
				y2 = y1+ t*deltaY;
				x2 = x1+ t*deltaX;
				r2 = r1+ t*deltaR;
				g2 = g1+ t*deltaG;
				b2 = b1+ t*deltaB;
			}
		}
		else { // zF < z1
			if(zF < z2)
				return CULLED;
			
			t = (zF - z1)/deltaZ;
			z1 = zF;
			y1 += t*deltaY;
			x1 += t*deltaX;
			r1 += t*deltaR;
			g1 += t*deltaG;
			b1 += t*deltaB;
			
			if(z2 < zB) {
				t = (zB-z1)/deltaZ;
				z2 = zB;
				y2 = y1 + t*deltaY;
				x2 = x1 + t*deltaX;
				r2 = r1+ t*deltaR;
				g2 = g1+ t*deltaG;
				b2 = b1+ t*deltaB;
			}
		}
		
		p1 = new Vertex3D(new Point3DH(x1, y1, z1), new Color(r1, g1, b1));
		p2 = new Vertex3D(new Point3DH(x2, y2, z2), new Color(r2, g2, b2));
		Vertex3D vertices[] = new Vertex3D[2];
		vertices[0] = p1;
		vertices[1] = p2;
		
		return vertices;
	}	
	
	public Polygon PolygonClipping3D(Polygon polygon) {
		Polygon CULLED = Polygon.makeEmpty();
		int k1, k2, i;
		Vertex3D q1, q2;
		
		Point3DH norm;
		Vertex3D[] Vertices = new Vertex3D[polygon.length()];
		
		for(i = 0; i < polygon.length(); i++) {
			norm = (Point3DH) polygon.get(i).getNormal();
			if(norm.IsEmpty())
				norm = polygon.getNorm();
			
			Vertices[i] = polygon.get(i).replaceNormal(norm);
		}
		polygon = Polygon.make(Vertices);
		
		// back
		if(polygon.get(0).getZ() < zfar) {   // v0 outside
			for(i = 1; i < polygon.length(); i++) {
				if(polygon.get(i).getZ() >= zfar)
					break;
			}
			if(i == polygon.length())
				return CULLED;
			
			k1 = i-1;
			q1 = IntersectPointZ(polygon.get(k1), polygon.get(i), zfar);
			for(; i < k1+polygon.length(); i++) {
				if(polygon.get(i).getZ() < zfar)
					break;
			}
			k2 =  i-1;
			q2 = IntersectPointZ(polygon.get(k2), polygon.get(i), zfar);
			
			Vertex3D newVertices[] = new Vertex3D[k2-k1+2];
			newVertices[0] = q1;
			for(i = k1+1; i < k2+1;i++)
				newVertices[i-k1] = polygon.get(i);
			newVertices[k2-k1+1] = q2;
			polygon = Polygon.make(newVertices);
		}
		else {// polygon.get(0).getZ() >= zB  vo inside
			for(i = 1; i < polygon.length(); i++) {
				if(polygon.get(i).getZ() < zfar)
					break;
			}
			if(i == polygon.length()) 
				;  // all are inside, do nothing
			else {
				k1 = i-1;
				q1 = IntersectPointZ(polygon.get(k1), polygon.get(i), zfar);
				
				for(; i < k1+polygon.length(); i++) {
					if(polygon.get(i).getZ() >= zfar)
						break;
				}
				
				k2 =  i-1;
				q2 = IntersectPointZ(polygon.get(k2), polygon.get(i), zfar);
				
				Vertex3D newVertices[] = new Vertex3D[polygon.length()-(k2-k1)+2];
				newVertices[0] = q2;
				for(i = k2+1; i < k1+1+polygon.length();i++)
					newVertices[i-k2] = polygon.get(i);
				newVertices[polygon.length()-(k2-k1)+1] = q1;
				polygon = Polygon.make(newVertices);
			}
		}
		
		// front
		if(polygon.get(0).getZ() > znear) {   // v0 outside
			for(i = 1; i < polygon.length(); i++) {
				if(polygon.get(i).getZ() <= znear)
					break;
			}
			if(i == polygon.length())
				return CULLED;
					
			k1 = i-1;
			q1 = IntersectPointZ(polygon.get(k1), polygon.get(i), znear);
			for(; i < k1+polygon.length(); i++) {
				if(polygon.get(i).getZ() > znear)
					break;
			}
			k2 =  i-1;
			q2 = IntersectPointZ(polygon.get(k2), polygon.get(i), znear);
					
			Vertex3D newVertices[] = new Vertex3D[k2-k1+2];
			newVertices[0] = q1;
			for(i = k1+1; i < k2+1;i++)
				newVertices[i-k1] = polygon.get(i);
			newVertices[k2-k1+1] = q2;
			polygon = Polygon.make(newVertices);
		}
		else {// polygon.get(0).getZ() <= zF  vo inside
			for(i = 1; i < polygon.length(); i++) {
				if(polygon.get(i).getZ() > znear)
					break;
			}
			if(i == polygon.length()) 
				;  // all are inside, do nothing
			else {
				k1 = i-1;
				q1 = IntersectPointZ(polygon.get(k1), polygon.get(i), znear);
						
				for(; i < k1+polygon.length(); i++) {
					if(polygon.get(i).getZ() <= znear)
						break;
				}
						
				k2 =  i-1;
				q2 = IntersectPointZ(polygon.get(k2), polygon.get(i), znear);
						
				Vertex3D newVertices[] = new Vertex3D[polygon.length()-(k2-k1)+2];
				newVertices[0] = q2;
				for(i = k2+1; i < k1+1+polygon.length();i++)
					newVertices[i-k2] = polygon.get(i);
				newVertices[polygon.length()-(k2-k1)+1] = q1;
				polygon = Polygon.make(newVertices);
			}
		}
		
		// left
		if(polygon.get(0).getX()*A[LEFT]+polygon.get(0).getZ()*C[LEFT]+D[LEFT] > 0) { // v0 outside
			
			for(i = 1; i < polygon.length(); i++) {
				if(polygon.get(i).getX()*A[LEFT]+polygon.get(i).getZ()*C[LEFT]+D[LEFT] <= 0)
					break;
			}
			if(i == polygon.length())
				return CULLED;
			
			k1 = i-1;
			q1 = IntersectPoint3D(polygon.get(k1), polygon.get(i), LEFT);
			
			for(; i < k1+polygon.length(); i++) {
				if(polygon.get(i).getX()*A[LEFT]+polygon.get(i).getZ()*C[LEFT]+D[LEFT] > 0)
					break;
			}
			k2 =  i-1;
			q2 = IntersectPoint3D(polygon.get(k2), polygon.get(i), LEFT);
			
			Vertex3D newVertices[] = new Vertex3D[k2-k1+2];
			newVertices[0] = q1;
			for(i = k1+1; i < k2+1;i++)
				newVertices[i-k1] = polygon.get(i);
			newVertices[k2-k1+1] = q2;
			polygon = Polygon.make(newVertices);
		}
		else {// vo inside
			for(i = 1; i < polygon.length(); i++) {
				if(polygon.get(i).getX()*A[LEFT]+polygon.get(i).getZ()*C[LEFT]+D[LEFT] > 0)
					break;
			}
			if(i == polygon.length()) 
				;  // all are inside, do nothing
			else {
				k1 = i-1;
				q1 = IntersectPoint3D(polygon.get(k1), polygon.get(i), LEFT);
				
				for(; i < k1+polygon.length(); i++) {
					if(polygon.get(i).getX()*A[LEFT]+polygon.get(i).getZ()*C[LEFT]+D[LEFT] <= 0)
						break;
				}
				
				k2 =  i-1;
				q2 = IntersectPoint3D(polygon.get(k2), polygon.get(i), LEFT);
				
				Vertex3D newVertices[] = new Vertex3D[polygon.length()-(k2-k1)+2];
				newVertices[0] = q2;
				for(i = k2+1; i < k1+1+polygon.length();i++)
					newVertices[i-k2] = polygon.get(i);
				newVertices[polygon.length()-(k2-k1)+1] = q1;
				polygon = Polygon.make(newVertices);
			}
		}
		
		// right
		if(polygon.get(0).getX()*A[RIGHT]+polygon.get(0).getZ()*C[RIGHT]+D[RIGHT] > 0) {   // v0 outside
			for(i = 1; i < polygon.length(); i++) {
				if(polygon.get(i).getX()*A[RIGHT]+polygon.get(i).getZ()*C[RIGHT]+D[RIGHT] <= 0)
					break;
			}
			if(i == polygon.length())
				return CULLED;
					
			k1 = i-1;
			q1 = IntersectPoint3D(polygon.get(k1), polygon.get(i), RIGHT);
			for(; i < k1+polygon.length(); i++) {
				if(polygon.get(i).getX()*A[RIGHT]+polygon.get(i).getZ()*C[RIGHT]+D[RIGHT] > 0)
					break;
			}
			k2 =  i-1;
			q2 = IntersectPoint3D(polygon.get(k2), polygon.get(i), RIGHT);
					
			Vertex3D newVertices[] = new Vertex3D[k2-k1+2];
			newVertices[0] = q1;
			for(i = k1+1; i < k2+1;i++)
				newVertices[i-k1] = polygon.get(i);
			newVertices[k2-k1+1] = q2;
			polygon = Polygon.make(newVertices);
		}
		else {// polygon.get(0).getX() <= xR  vo inside
			for(i = 1; i < polygon.length(); i++) {
				if(polygon.get(i).getX()*A[RIGHT]+polygon.get(i).getZ()*C[RIGHT]+D[RIGHT] > 0)
					break;
			}
			if(i == polygon.length()) 
				;  // all are inside, do nothing
			else {
				k1 = i-1;
				q1 = IntersectPoint3D(polygon.get(k1), polygon.get(i), RIGHT);
						
				for(; i < k1+polygon.length(); i++) {
					if(polygon.get(i).getX()*A[RIGHT]+polygon.get(i).getZ()*C[RIGHT]+D[RIGHT] <= 0)
						break;
				}
						
				k2 =  i-1;
				q2 = IntersectPoint3D(polygon.get(k2), polygon.get(i), RIGHT);
						
				Vertex3D newVertices[] = new Vertex3D[polygon.length()-(k2-k1)+2];
				newVertices[0] = q2;
				for(i = k2+1; i < k1+1+polygon.length();i++)
					newVertices[i-k2] = polygon.get(i);
				newVertices[polygon.length()-(k2-k1)+1] = q1;
				polygon = Polygon.make(newVertices);
			}
		}
		
		// bottom
		if(polygon.get(0).getY()*B[DOWN]+polygon.get(0).getZ()*C[DOWN]+D[DOWN] > 0) {   // v0 outside
			for(i = 1; i < polygon.length(); i++) {
				if(polygon.get(i).getY()*B[DOWN]+polygon.get(i).getZ()*C[DOWN]+D[DOWN] <= 0)
					break;
			}
			if(i == polygon.length())
				return CULLED;
			
			k1 = i-1;
			q1 = IntersectPoint3D(polygon.get(k1), polygon.get(i), DOWN);
			for(; i < k1+polygon.length(); i++) {
				if(polygon.get(i).getY()*B[DOWN]+polygon.get(i).getZ()*C[DOWN]+D[DOWN] > 0)
					break;
			}
			k2 =  i-1;
			q2 = IntersectPoint3D(polygon.get(k2), polygon.get(i), DOWN);
			
			Vertex3D newVertices[] = new Vertex3D[k2-k1+2];
			newVertices[0] = q1;
			for(i = k1+1; i < k2+1;i++)
				newVertices[i-k1] = polygon.get(i);
			newVertices[k2-k1+1] = q2;
			polygon = Polygon.make(newVertices);
		}
		else {// polygon.get(0).getY() >= yB  vo inside
			for(i = 1; i < polygon.length(); i++) {
				if(polygon.get(i).getY()*B[DOWN]+polygon.get(i).getZ()*C[DOWN]+D[DOWN] > 0)
					break;
			}
			if(i == polygon.length()) 
				;  // all are inside, do nothing
			else {
				k1 = i-1;
				q1 = IntersectPoint3D(polygon.get(k1), polygon.get(i), DOWN);
				
				for(; i < k1+polygon.length(); i++) {
					if(polygon.get(i).getY()*B[DOWN]+polygon.get(i).getZ()*C[DOWN]+D[DOWN] <= 0)
						break;
				}
				
				k2 =  i-1;
				q2 = IntersectPoint3D(polygon.get(k2), polygon.get(i), DOWN);
				
				Vertex3D newVertices[] = new Vertex3D[polygon.length()-(k2-k1)+2];
				newVertices[0] = q2;
				for(i = k2+1; i < k1+1+polygon.length();i++)
					newVertices[i-k2] = polygon.get(i);
				newVertices[polygon.length()-(k2-k1)+1] = q1;
				polygon = Polygon.make(newVertices);
			}
		}
		
		// top
		if(polygon.get(0).getY()*B[TOP]+polygon.get(0).getZ()*C[TOP]+D[TOP] > 0) {   // v0 outside
			for(i = 1; i < polygon.length(); i++) {
				if(polygon.get(i).getY()*B[TOP]+polygon.get(i).getZ()*C[TOP]+D[TOP] <= 0)
					break;
			}
			if(i == polygon.length())
				return CULLED;
					
			k1 = i-1;
			q1 = IntersectPoint3D(polygon.get(k1), polygon.get(i), TOP);
			for(; i < k1+polygon.length(); i++) {
				if(polygon.get(i).getY()*B[TOP]+polygon.get(i).getZ()*C[TOP]+D[TOP] > 0)
					break;
			}
			k2 =  i-1;
			q2 = IntersectPoint3D(polygon.get(k2), polygon.get(i), TOP);
					
			Vertex3D newVertices[] = new Vertex3D[k2-k1+2];
			newVertices[0] = q1;
			for(i = k1+1; i < k2+1;i++)
				newVertices[i-k1] = polygon.get(i);
			newVertices[k2-k1+1] = q2;
			polygon = Polygon.make(newVertices);
		}
		else {// polygon.get(0).getY() <= yT  vo inside
			for(i = 1; i < polygon.length(); i++) {
				if(polygon.get(i).getY()*B[TOP]+polygon.get(i).getZ()*C[TOP]+D[TOP] > 0)
					break;
			}
			if(i == polygon.length()) 
				;  // all are inside, do nothing
			else {
				k1 = i-1;
				q1 = IntersectPoint3D(polygon.get(k1), polygon.get(i), TOP);
						
				for(; i < k1+polygon.length(); i++) {
					if(polygon.get(i).getY()*B[TOP]+polygon.get(i).getZ()*C[TOP]+D[TOP] <= 0)
						break;
				}
						
				k2 =  i-1;
				q2 = IntersectPoint3D(polygon.get(k2), polygon.get(i), TOP);
						
				Vertex3D newVertices[] = new Vertex3D[polygon.length()-(k2-k1)+2];
				newVertices[0] = q2;
				for(i = k2+1; i < k1+1+polygon.length();i++)
					newVertices[i-k2] = polygon.get(i);
				newVertices[polygon.length()-(k2-k1)+1] = q1;
				polygon = Polygon.make(newVertices);
			}
		}	
		
		return polygon;
	}
	
	public Polygon PolygonClipping(Polygon polygon, double xL, double xR, double yB, double yT, double zB, int zF) {
		Polygon CULLED = Polygon.makeEmpty();
		
		int k1, k2, i;
		Vertex3D q1, q2;
		// x left
		if(polygon.get(0).getX() < xL) {   // v0 outside
			for(i = 1; i < polygon.length(); i++) {
				if(polygon.get(i).getX() > xL)
					break;
			}
			if(i == polygon.length())
				return CULLED;
			
			k1 = i-1;
			q1 = IntersectPointX(polygon.get(k1), polygon.get(i), xL);
			for(; i < k1+polygon.length(); i++) {
				if(polygon.get(i).getX() <= xL)
					break;
			}
			k2 =  i-1;
			q2 = IntersectPointX(polygon.get(k2), polygon.get(i), xL);
			
			Vertex3D newVertices[] = new Vertex3D[k2-k1+2];
			newVertices[0] = q1;
			for(i = k1+1; i < k2+1;i++)
				newVertices[i-k1] = polygon.get(i);
			newVertices[k2-k1+1] = q2;
			polygon = Polygon.make(newVertices);
		}
		else {// polygon.get(0).getX() >= xL  vo inside
			for(i = 1; i < polygon.length(); i++) {
				if(polygon.get(i).getX() < xL)
					break;
			}
			if(i == polygon.length()) 
				;  // all are inside, do nothing
			else {
				k1 = i-1;
				q1 = IntersectPointX(polygon.get(k1), polygon.get(i), xL);
				
				for(; i < k1+polygon.length(); i++) {
					if(polygon.get(i).getX() >= xL)
						break;
				}
				
				k2 =  i-1;
				q2 = IntersectPointX(polygon.get(k2), polygon.get(i), xL);
				
				Vertex3D newVertices[] = new Vertex3D[polygon.length()-(k2-k1)+2];
				newVertices[0] = q2;
				for(i = k2+1; i < k1+1+polygon.length();i++)
					newVertices[i-k2] = polygon.get(i);
				newVertices[polygon.length()-(k2-k1)+1] = q1;
				polygon = Polygon.make(newVertices);
			}
		}
		
		// x right
		if(polygon.get(0).getX() > xR) {   // v0 outside
			for(i = 1; i < polygon.length(); i++) {
				if(polygon.get(i).getX() <= xR)
					break;
			}
			if(i == polygon.length())
				return CULLED;
					
			k1 = i-1;
			q1 = IntersectPointX(polygon.get(k1), polygon.get(i), xR);
			for(; i < k1+polygon.length(); i++) {
				if(polygon.get(i).getX() > xR)
					break;
			}
			k2 =  i-1;
			q2 = IntersectPointX(polygon.get(k2), polygon.get(i), xR);
					
			Vertex3D newVertices[] = new Vertex3D[k2-k1+2];
			newVertices[0] = q1;
			for(i = k1+1; i < k2+1;i++)
				newVertices[i-k1] = polygon.get(i);
			newVertices[k2-k1+1] = q2;
			polygon = Polygon.make(newVertices);
		}
		else {// polygon.get(0).getX() <= xR  vo inside
			for(i = 1; i < polygon.length(); i++) {
				if(polygon.get(i).getX() > xR)
					break;
			}
			if(i == polygon.length()) 
				;  // all are inside, do nothing
			else {
				k1 = i-1;
				q1 = IntersectPointX(polygon.get(k1), polygon.get(i), xR);
						
				for(; i < k1+polygon.length(); i++) {
					if(polygon.get(i).getX() <= xR)
						break;
				}
						
				k2 =  i-1;
				q2 = IntersectPointX(polygon.get(k2), polygon.get(i), xR);
						
				Vertex3D newVertices[] = new Vertex3D[polygon.length()-(k2-k1)+2];
				newVertices[0] = q2;
				for(i = k2+1; i < k1+1+polygon.length();i++)
					newVertices[i-k2] = polygon.get(i);
				newVertices[polygon.length()-(k2-k1)+1] = q1;
				polygon = Polygon.make(newVertices);
			}
		}
		
		// y bottom
		if(polygon.get(0).getY() < yB) {   // v0 outside
			for(i = 1; i < polygon.length(); i++) {
				if(polygon.get(i).getY() >= yB)
					break;
			}
			if(i == polygon.length())
				return CULLED;
			
			k1 = i-1;
			q1 = IntersectPointY(polygon.get(k1), polygon.get(i), yB);
			for(; i < k1+polygon.length(); i++) {
				if(polygon.get(i).getY() < yB)
					break;
			}
			k2 =  i-1;
			q2 = IntersectPointY(polygon.get(k2), polygon.get(i), yB);
			
			Vertex3D newVertices[] = new Vertex3D[k2-k1+2];
			newVertices[0] = q1;
			for(i = k1+1; i < k2+1;i++)
				newVertices[i-k1] = polygon.get(i);
			newVertices[k2-k1+1] = q2;
			polygon = Polygon.make(newVertices);
		}
		else {// polygon.get(0).getY() >= yB  vo inside
			for(i = 1; i < polygon.length(); i++) {
				if(polygon.get(i).getY() < yB)
					break;
			}
			if(i == polygon.length()) 
				;  // all are inside, do nothing
			else {
				k1 = i-1;
				q1 = IntersectPointY(polygon.get(k1), polygon.get(i), yB);
				
				for(; i < k1+polygon.length(); i++) {
					if(polygon.get(i).getY() >= yB)
						break;
				}
				
				k2 =  i-1;
				q2 = IntersectPointY(polygon.get(k2), polygon.get(i), yB);
				
				Vertex3D newVertices[] = new Vertex3D[polygon.length()-(k2-k1)+2];
				newVertices[0] = q2;
				for(i = k2+1; i < k1+1+polygon.length();i++)
					newVertices[i-k2] = polygon.get(i);
				newVertices[polygon.length()-(k2-k1)+1] = q1;
				polygon = Polygon.make(newVertices);
			}
		}
		// y top
		if(polygon.get(0).getY() > yT) {   // v0 outside
			for(i = 1; i < polygon.length(); i++) {
				if(polygon.get(i).getY() <= yT)
					break;
			}
			if(i == polygon.length())
				return CULLED;
					
			k1 = i-1;
			q1 = IntersectPointY(polygon.get(k1), polygon.get(i), yT);
			for(; i < k1+polygon.length(); i++) {
				if(polygon.get(i).getY() > yT)
					break;
			}
			k2 =  i-1;
			q2 = IntersectPointY(polygon.get(k2), polygon.get(i), yT);
					
			Vertex3D newVertices[] = new Vertex3D[k2-k1+2];
			newVertices[0] = q1;
			for(i = k1+1; i < k2+1;i++)
				newVertices[i-k1] = polygon.get(i);
			newVertices[k2-k1+1] = q2;
			polygon = Polygon.make(newVertices);
		}
		else {// polygon.get(0).getY() <= yT  vo inside
			for(i = 1; i < polygon.length(); i++) {
				if(polygon.get(i).getY() > yT)
					break;
			}
			if(i == polygon.length()) 
				;  // all are inside, do nothing
			else {
				k1 = i-1;
				q1 = IntersectPointY(polygon.get(k1), polygon.get(i), yT);
						
				for(; i < k1+polygon.length(); i++) {
					if(polygon.get(i).getY() <= yT)
						break;
				}
						
				k2 =  i-1;
				q2 = IntersectPointY(polygon.get(k2), polygon.get(i), yT);
						
				Vertex3D newVertices[] = new Vertex3D[polygon.length()-(k2-k1)+2];
				newVertices[0] = q2;
				for(i = k2+1; i < k1+1+polygon.length();i++)
					newVertices[i-k2] = polygon.get(i);
				newVertices[polygon.length()-(k2-k1)+1] = q1;
				polygon = Polygon.make(newVertices);
			}
		}	
		// z back
		if(polygon.get(0).getZ() < zB) {   // v0 outside
			for(i = 1; i < polygon.length(); i++) {
				if(polygon.get(i).getZ() >= zB)
					break;
			}
			if(i == polygon.length())
				return CULLED;
			
			k1 = i-1;
			q1 = IntersectPointZ(polygon.get(k1), polygon.get(i), zB);
			for(; i < k1+polygon.length(); i++) {
				if(polygon.get(i).getZ() < zB)
					break;
			}
			k2 =  i-1;
			q2 = IntersectPointZ(polygon.get(k2), polygon.get(i), zB);
			
			Vertex3D newVertices[] = new Vertex3D[k2-k1+2];
			newVertices[0] = q1;
			for(i = k1+1; i < k2+1;i++)
				newVertices[i-k1] = polygon.get(i);
			newVertices[k2-k1+1] = q2;
			polygon = Polygon.make(newVertices);
		}
		else {// polygon.get(0).getZ() >= zB  vo inside
			for(i = 1; i < polygon.length(); i++) {
				if(polygon.get(i).getZ() < zB)
					break;
			}
			if(i == polygon.length()) 
				;  // all are inside, do nothing
			else {
				k1 = i-1;
				q1 = IntersectPointZ(polygon.get(k1), polygon.get(i), zB);
				
				for(; i < k1+polygon.length(); i++) {
					if(polygon.get(i).getZ() >= zB)
						break;
				}
				
				k2 =  i-1;
				q2 = IntersectPointZ(polygon.get(k2), polygon.get(i), zB);
				
				Vertex3D newVertices[] = new Vertex3D[polygon.length()-(k2-k1)+2];
				newVertices[0] = q2;
				for(i = k2+1; i < k1+1+polygon.length();i++)
					newVertices[i-k2] = polygon.get(i);
				newVertices[polygon.length()-(k2-k1)+1] = q1;
				polygon = Polygon.make(newVertices);
			}
		}
		
		// z front
		if(polygon.get(0).getZ() > zF) {   // v0 outside
			for(i = 1; i < polygon.length(); i++) {
				if(polygon.get(i).getZ() <= zF)
					break;
			}
			if(i == polygon.length())
				return CULLED;
					
			k1 = i-1;
			q1 = IntersectPointZ(polygon.get(k1), polygon.get(i), zF);
			for(; i < k1+polygon.length(); i++) {
				if(polygon.get(i).getZ() > zF)
					break;
			}
			k2 =  i-1;
			q2 = IntersectPointZ(polygon.get(k2), polygon.get(i), zF);
					
			Vertex3D newVertices[] = new Vertex3D[k2-k1+2];
			newVertices[0] = q1;
			for(i = k1+1; i < k2+1;i++)
				newVertices[i-k1] = polygon.get(i);
			newVertices[k2-k1+1] = q2;
			polygon = Polygon.make(newVertices);
		}
		else {// polygon.get(0).getZ() <= zF  vo inside
			for(i = 1; i < polygon.length(); i++) {
				if(polygon.get(i).getZ() > zF)
					break;
			}
			if(i == polygon.length()) 
				;  // all are inside, do nothing
			else {
				k1 = i-1;
				q1 = IntersectPointZ(polygon.get(k1), polygon.get(i), zF);
						
				for(; i < k1+polygon.length(); i++) {
					if(polygon.get(i).getZ() <= zF)
						break;
				}
						
				k2 =  i-1;
				q2 = IntersectPointZ(polygon.get(k2), polygon.get(i), zF);
						
				Vertex3D newVertices[] = new Vertex3D[polygon.length()-(k2-k1)+2];
				newVertices[0] = q2;
				for(i = k2+1; i < k1+1+polygon.length();i++)
					newVertices[i-k2] = polygon.get(i);
				newVertices[polygon.length()-(k2-k1)+1] = q1;
				polygon = Polygon.make(newVertices);
			}
		}
		return polygon;
	}
	private Vertex3D IntersectPoint3D(Vertex3D p1, Vertex3D p2, int dir) {
		double x1, x2, y1, y2, z1, z2;
		double r1, r2, g1, g2, b1, b2;
		double x, y, z, r, g, b, t;
		double deltaX, deltaY, deltaZ, deltaR, deltaG, deltaB;
		Point3DH n1, n2, deltaN, n;
		
		x1 = p1.getX();
		x2 = p2.getX();
		y1 = p1.getY();
		y2 = p2.getY();
		z1 = p1.getZ();
		z2 = p2.getZ();
		r1 = p1.getColor().getR();
		r2 = p2.getColor().getR();
		g1 = p1.getColor().getG();
		g2 = p2.getColor().getG();
		b1 = p1.getColor().getB();
		b2 = p2.getColor().getB();
		
		deltaR = r2 - r1;
		deltaG = g2 - g1;
		deltaB = b2 - b1;
		deltaX = x2 - x1;
		deltaY = y2 - y1;
		deltaZ = z2 - z1;
		
		n1 = (Point3DH) p1.getNormal();
		n2 = (Point3DH) p2.getNormal();
		deltaN = n2.subtract(n1);
		
		t = GetT(deltaX, deltaY, deltaZ, x1, y1, z1, dir);
		x = x1+ t*deltaX;
		y = y1+ t*deltaY;
		z = z1+ t*deltaZ;
		r = r1+ t*deltaR;
		g = g1+ t*deltaG;
		b = b1+ t*deltaB;
		
		n = n1.add(deltaN.scale(t));
		
		return new Vertex3D(new Point3DH(x, y, z), n, new Color(r, g, b));
	}
	
	private Vertex3D IntersectPointX(Vertex3D p1, Vertex3D p2, double xx) {
		double x1, x2, y1, y2, z1, z2;
		double r1, r2, g1, g2, b1, b2;
		double x, y, z, r, g, b, t;
		double deltaX, deltaY, deltaZ, deltaR, deltaG, deltaB;
		Point3DH n1, n2, deltaN, n;
		
		n1 = (Point3DH) p1.getNormal();
		n2 = (Point3DH) p2.getNormal();
		deltaN = n2.subtract(n1);
		
		x1 = p1.getX();
		x2 = p2.getX();
		y1 = p1.getY();
		y2 = p2.getY();
		z1 = p1.getZ();
		z2 = p2.getZ();
		r1 = p1.getColor().getR();
		r2 = p2.getColor().getR();
		g1 = p1.getColor().getG();
		g2 = p2.getColor().getG();
		b1 = p1.getColor().getB();
		b2 = p2.getColor().getB();
		
		deltaR = r2 - r1;
		deltaG = g2 - g1;
		deltaB = b2 - b1;
		deltaX = x2 - x1;
		deltaY = y2 - y1;
		deltaZ = z2 - z1;
		
		t = (xx-x1)/deltaX;
		x = xx;
		y = y1+ t*deltaY;
		z = z1+ t*deltaZ;
		r = r1+ t*deltaR;
		g = g1+ t*deltaG;
		b = b1+ t*deltaB;
		n = n1.add(deltaN.scale(t));
		return new Vertex3D(new Point3DH(x, y, z), n, new Color(r, g, b));
	}
	
	private Vertex3D IntersectPointY(Vertex3D p1, Vertex3D p2, double yy) {
		double x1, x2, y1, y2, z1, z2;
		double r1, r2, g1, g2, b1, b2;
		double x, y, z, r, g, b, t;
		double deltaX, deltaY, deltaZ, deltaR, deltaG, deltaB;
		Point3DH n1, n2, deltaN, n;
		
		n1 = (Point3DH) p1.getNormal();
		n2 = (Point3DH) p2.getNormal();
		deltaN = n2.subtract(n1);
		
		x1 = p1.getX();
		x2 = p2.getX();
		y1 = p1.getY();
		y2 = p2.getY();
		z1 = p1.getZ();
		z2 = p2.getZ();
		r1 = p1.getColor().getR();
		r2 = p2.getColor().getR();
		g1 = p1.getColor().getG();
		g2 = p2.getColor().getG();
		b1 = p1.getColor().getB();
		b2 = p2.getColor().getB();
		
		deltaR = r2 - r1;
		deltaG = g2 - g1;
		deltaB = b2 - b1;
		deltaX = x2 - x1;
		deltaY = y2 - y1;
		deltaZ = z2 - z1;
		
		t = (yy-y1)/deltaY;
		y = yy;
		x = x1+ t*deltaX;
		z = z1+ t*deltaZ;
		r = r1+ t*deltaR;
		g = g1+ t*deltaG;
		b = b1+ t*deltaB;
		
		n = n1.add(deltaN.scale(t));
		return new Vertex3D(new Point3DH(x, y, z), n, new Color(r, g, b));
	}
	
	private Vertex3D IntersectPointZ(Vertex3D p1, Vertex3D p2, double zz) {
		double x1, x2, y1, y2, z1, z2;
		double r1, r2, g1, g2, b1, b2;
		double x, y, z, r, g, b, t;
		double deltaX, deltaY, deltaZ, deltaR, deltaG, deltaB;
		Point3DH n1, n2, deltaN, n;
		
		n1 = (Point3DH) p1.getNormal();
		n2 = (Point3DH) p2.getNormal();
		deltaN = n2.subtract(n1);
		x1 = p1.getX();
		x2 = p2.getX();
		y1 = p1.getY();
		y2 = p2.getY();
		z1 = p1.getZ();
		z2 = p2.getZ();
		r1 = p1.getColor().getR();
		r2 = p2.getColor().getR();
		g1 = p1.getColor().getG();
		g2 = p2.getColor().getG();
		b1 = p1.getColor().getB();
		b2 = p2.getColor().getB();
		
		deltaR = r2 - r1;
		deltaG = g2 - g1;
		deltaB = b2 - b1;
		deltaX = x2 - x1;
		deltaY = y2 - y1;
		deltaZ = z2 - z1;
		
		t = (zz-z1)/deltaZ;
		z = zz;
		x = x1+ t*deltaX;
		y = y1+ t*deltaY;
		r = r1+ t*deltaR;
		g = g1+ t*deltaG;
		b = b1+ t*deltaB;
		n = n1.add(deltaN.scale(t));
		return new Vertex3D(new Point3DH(x, y, z), n, new Color(r, g, b));
	}
}
