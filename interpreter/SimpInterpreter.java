package notProvided.client.interpreter;

import java.util.ArrayList;
import java.util.Stack;

import com.sun.org.apache.bcel.internal.generic.GotoInstruction;

import geometry.Point2D;
import geometry.Point3DH;
import geometry.Vertex3D;
import line.LineRenderer;
import notProvided.client.Clipper;
import notProvided.client.DepthEffectDrawable;
import notProvided.client.RendererTrio;
import notProvided.client.zBufferingDrawable;
import notProvided.geometry.Transformation;
import notProvided.shading.FlatShader;
import notProvided.shading.GouraudShader;
import notProvided.shading.Lighting;
import notProvided.shading.NullShader;
import notProvided.shading.PhongShader;
import notProvided.shading.Shader;
import polygon.Polygon;
import polygon.PolygonRenderer;
import windowing.drawable.Drawable;
import windowing.drawable.TranslatingDrawable;
import windowing.graphics.Color;
import windowing.graphics.Dimensions;

public class SimpInterpreter {
	private static final int NUM_TOKENS_FOR_POINT = 3;
	private static final int NUM_TOKENS_FOR_COMMAND = 1;
	private static final int NUM_TOKENS_FOR_COLORED_VERTEX = 6;
	private static final int NUM_TOKENS_FOR_UNCOLORED_VERTEX = 3;
	private static final char COMMENT_CHAR = '#';
	private RenderStyle renderStyle;
	private ShadeStyle shadeStyle;
	
	private static Transformation CTM;
	private Stack<double[][]> pointStack;
	private Stack<double[][]> InvpointStack;
	private ArrayList<Lighting> lightPoints;
	
	private LineBasedReader reader;
	private Stack<LineBasedReader> readerStack;
	
	private Color defaultColor = Color.WHITE;
	private static Color ambientLight = Color.BLACK; 
	private Color depColor = Color.BLACK;
	
	private Drawable drawable;
	private Drawable depthDrawable;
	
	private LineRenderer lineRenderer;
	private PolygonRenderer filledRenderer;
	private PolygonRenderer wireframeRenderer; 
	private static Transformation worldToCamera;
	private Clipper clipper;
	
	private static double vwx_low;
	private static double vwx_high;
	private static double vwy_low;
	private static double vwy_high;
	private double csz_near = 0;
	private double csz_far = -200;
	
	private double depz_near = -Double.MAX_VALUE;
	private double depz_far = -Double.MAX_VALUE;
	
	private double kSpecular = 0.3;
	private double specularExp = 8;
	
	private FlatShader flatShader;
	private GouraudShader gouraudShader;
	private PhongShader phongShader;
	private boolean HasLight = false;
	private boolean DoShading = false;

	public enum RenderStyle {
		FILLED,
		WIREFRAME;
	}
	public enum ShadeStyle {
		PHONG,
		GOURAUD,
		FLAT;
	}
	
	public SimpInterpreter(String filename, 
			Drawable drawable,
			RendererTrio renderers) {
		this.drawable = new zBufferingDrawable(drawable);
		this.drawable = new TranslatingDrawable(this.drawable, new Point2D(325.0,325.0), new Dimensions(650,  650));
		this.depthDrawable = new DepthEffectDrawable(this.drawable, depColor, depz_near, depz_far);
		this.lineRenderer = renderers.getLineRenderer();
		this.filledRenderer = renderers.getFilledRenderer();
		this.wireframeRenderer = renderers.getWireframeRenderer();
		
		reader = new LineBasedReader(filename);
		readerStack = new Stack<>();
		renderStyle = RenderStyle.FILLED;
		shadeStyle = ShadeStyle.PHONG;
		CTM = Transformation.make();
		pointStack = new Stack<>();
		InvpointStack = new Stack<>();
		lightPoints = new ArrayList<>();
		clipper = new Clipper();
	}
	
	public void interpret() {
		while(reader.hasNext() ) {
			String line = reader.next().trim();
			interpretLine(line);
			while(!reader.hasNext()) {
				if(readerStack.isEmpty()) {
					return;
				}
				else {
					reader = readerStack.pop();
				}
			}
		}
	}
	public void interpretLine(String line) {
		if(!line.isEmpty() && line.charAt(0) != COMMENT_CHAR) {
			String[] tokens = line.split("[ \t,()]+");
			if(tokens.length != 0) {
				interpretCommand(tokens);
			}
		}
	}
	private void interpretCommand(String[] tokens) {
		switch(tokens[0]) {
		case "{" :      push();   break;
		case "}" :      pop();    break;
		case "wire" :   wire();   break;
		case "filled" : filled(); break;
		
		case "file" :		interpretFile(tokens);		break;
		case "scale" :		interpretScale(tokens);		break;
		case "translate" :	interpretTranslate(tokens);	break;
		case "rotate" :		interpretRotate(tokens);		break;
		case "line" :		interpretLine(tokens);		break;
		case "polygon" :		interpretPolygon(tokens);	break;
		case "camera" :		interpretCamera(tokens);		break;
		case "surface" :		interpretSurface(tokens);	break;
		case "ambient" :		interpretAmbient(tokens);	break;
		case "depth" :		interpretDepth(tokens);		break;
		case "obj" :			interpretObj(tokens);		break;
		case "light" :		interpretLight(tokens);		break;
		case "phong" : 		phong();						break;
		case "gouraud" :		gouraud();					break;
		case	 "flat"	: 		flat();						break;
		
		default :
			System.err.println("bad input line: " + tokens);
			break;
		}
	}

	private void interpretLight(String[] tokens) {
		double r = cleanNumber(tokens[1]);
		double g = cleanNumber(tokens[2]);
		double b = cleanNumber(tokens[3]);
		double attA = cleanNumber(tokens[4]);
		double attB = cleanNumber(tokens[5]);
		
		Color intensity = new Color(r, g, b);
		Lighting newLight = new Lighting(CTM, intensity, attA, attB);
		lightPoints.add(newLight);
		HasLight = true;
	}

	private void flat() {
		shadeStyle = ShadeStyle.FLAT;
	}

	private void gouraud() {
		shadeStyle = ShadeStyle.GOURAUD;
	}

	private void phong() {
		shadeStyle = ShadeStyle.PHONG;
	}

	private void interpretDepth(String[] tokens) {
		depz_near = cleanNumber(tokens[1]);
		depz_far = cleanNumber(tokens[2]);
		
		double r = cleanNumber(tokens[3]);
		double g = cleanNumber(tokens[4]);
		double b = cleanNumber(tokens[5]);
		depColor = new Color(r, g, b);
		
		((DepthEffectDrawable) depthDrawable).SetParam(depColor, depz_near, depz_far);
	}

	private void interpretSurface(String[] tokens) {
		double r = cleanNumber(tokens[1]);
		double g = cleanNumber(tokens[2]);
		double b = cleanNumber(tokens[3]);
		kSpecular = cleanNumber(tokens[4]);
		specularExp = cleanNumber(tokens[5]);
		
		defaultColor = new Color(r, g, b);
	}

	private void interpretAmbient(String[] tokens) {
		double r = cleanNumber(tokens[1]);
		double g = cleanNumber(tokens[2]);
		double b = cleanNumber(tokens[3]);
		
		ambientLight = new Color(r, g, b);
	}

	private void interpretCamera(String[] tokens) {
		vwx_low = cleanNumber(tokens[1]);
		vwy_low = cleanNumber(tokens[2]);
		vwx_high =cleanNumber(tokens[3]);
		vwy_high = cleanNumber(tokens[4]);
		csz_near = cleanNumber(tokens[5]);
		csz_far = cleanNumber(tokens[6]);
		worldToCamera = Transformation.make();
		worldToCamera.Rewrite( CTM.matrixInverse() );
		worldToCamera.RewriteInv(CTM.matrixCopy());
		
		clipper.setClipperParam(csz_near, csz_far, vwx_low, vwx_high, vwy_low, vwy_high);
	}

	private void push() { 
		pointStack.push(CTM.matrixCopy()); 
		InvpointStack.push(CTM.matrixInverse());
	}
	
	private void pop() {
		if(HasLight)
			DoShading = true;
		
		if(!pointStack.isEmpty()) {
			CTM.Rewrite(pointStack.pop());
		}
		if(!InvpointStack.isEmpty()) {
			CTM.RewriteInv(InvpointStack.pop());
		}
	}
	private void wire() {
		renderStyle = RenderStyle.WIREFRAME; 
	}
	
	private void filled() {
		renderStyle = RenderStyle.FILLED;  
	}
	
	// this one is complete.
	private void interpretFile(String[] tokens) {
		String quotedFilename = tokens[1];
		int length = quotedFilename.length();
		assert quotedFilename.charAt(0) == '"' && quotedFilename.charAt(length-1) == '"'; 
		String filename = quotedFilename.substring(1, length-1);
		file("simp/"+filename + ".simp");
	}
	private void file(String filename) {
		readerStack.push(reader);
		reader = new LineBasedReader(filename);
	}	

	private void interpretScale(String[] tokens) {
		double sx = cleanNumber(tokens[1]);
		double sy = cleanNumber(tokens[2]);
		double sz = cleanNumber(tokens[3]);
		CTM.scale(sx, sy, sz); 
	}
	private void interpretTranslate(String[] tokens) {
		double tx = cleanNumber(tokens[1]);
		double ty = cleanNumber(tokens[2]);
		double tz = cleanNumber(tokens[3]);
		CTM.translate(tx, ty, tz); 
	}
	private void interpretRotate(String[] tokens) {
		String axisString = tokens[1];
		double angleInDegrees = cleanNumber(tokens[2]);
		if(axisString.charAt(0) == 'X') {  
			CTM.rotateX(angleInDegrees*Math.PI/180);
		}
		else if(axisString.charAt(0) == 'Y') {
			CTM.rotateY(angleInDegrees*Math.PI/180);
		}
		else {
			CTM.rotateZ(angleInDegrees*Math.PI/180);
		} 
	}
	private static double cleanNumber(String string) {
		return Double.parseDouble(string);
	}
	
	private enum VertexColors {
		COLORED(NUM_TOKENS_FOR_COLORED_VERTEX),
		UNCOLORED(NUM_TOKENS_FOR_UNCOLORED_VERTEX);
		
		private int numTokensPerVertex;
		
		private VertexColors(int numTokensPerVertex) {
			this.numTokensPerVertex = numTokensPerVertex;
		}
		public int numTokensPerVertex() {
			return numTokensPerVertex;
		}
	}
	private void interpretLine(String[] tokens) {			
		Vertex3D[] vertices = interpretVertices(tokens, 2, 1);
		
		// put into the world space (not change w)
		vertices[0] = TransWithCTM(vertices[0]);
		vertices[1] = TransWithCTM(vertices[1]);
		
		// put into the camera space (not change w)
		vertices[0] = TransToCamera(vertices[0]);
		vertices[1] = TransToCamera(vertices[1]);
		// lighting calc 
		vertices[0] = SimpLightCalc(vertices[0]);
		vertices[1] = SimpLightCalc(vertices[1]);
		
		// Clip 
		vertices = clipper.LineClipping3D(vertices[0], vertices[1]);
		if(vertices == null)
			return;
		
		// Projection to Screen windows  (z = 1/csz)
		vertices[0] = ProjectToViewWindow(vertices[0]);
		vertices[1] = ProjectToViewWindow(vertices[1]);
		
		// Projection, shading and putting it on the screen 
		lineRenderer.drawLine(vertices[0], vertices[1], depthDrawable);
	}	
	private void interpretPolygon(String[] tokens) {			
		Vertex3D[] vertices = interpretVertices(tokens, 3, 1);
		
		// put into the world space
		vertices[0] = TransWithCTM(vertices[0]);
		vertices[1] = TransWithCTM(vertices[1]);
		vertices[2] = TransWithCTM(vertices[2]);
		
		// put into the camera space
		vertices[0] = TransToCamera(vertices[0]);
		vertices[1] = TransToCamera(vertices[1]);
		vertices[2] = TransToCamera(vertices[2]);
		
		Point3DH vector1, vector2, faceNorm;
		vector1 = vertices[1].getPoint3D().subtract(vertices[0].getPoint3D());
		vector2 = vertices[2].getPoint3D().subtract(vertices[0].getPoint3D());
		faceNorm = vector1.crossMultiply3D(vector2).normalize();
		
		Polygon p = Polygon.make(vertices);
		p.setNorm(faceNorm);
		
		// light calculation
		if(DoShading && shadeStyle == ShadeStyle.FLAT) {
			flatShader = new FlatShader(ambientLight, lightPoints, kSpecular, specularExp);
			p = flatShader.shade(p);
		}
		else if(DoShading && shadeStyle == ShadeStyle.GOURAUD) {
			gouraudShader = new GouraudShader(ambientLight, lightPoints, kSpecular, specularExp);
			p = gouraudShader.shade(p);
		}
		
		// Clip, calculate the vertex Norm
		p = clipper.PolygonClipping3D(p);
		if(p.length() == 0)
			return;
		
		// Projection to view windows (remain the cs coords and norms)
		Vertex3D[] newVertices = new Vertex3D[p.length()];
		for(int i = 0; i < p.length(); i++) {
			newVertices[i] = ProjectToViewWindow(p.get(i));
			if(i >= 2)
			{
				if(Polygon.isClockwise(newVertices[i-2], newVertices[i-1], newVertices[i]))
					return;
			}
		}
		
		Vertex3D firstV = newVertices[0];
		Vertex3D[] pVertices = new Vertex3D[3];
		
		// Projection, light calculation, depth shading
		if(renderStyle == RenderStyle.FILLED) {
			if(DoShading && shadeStyle == ShadeStyle.PHONG) {
				phongShader = new PhongShader(ambientLight, lightPoints, kSpecular, specularExp);
				
				for(int j = 2; j < newVertices.length; j += 1) {
					
					pVertices[0] = firstV;
					pVertices[1] = newVertices[j-1];
					pVertices[2] = newVertices[j];
					p = Polygon.make(pVertices);
				filledRenderer.drawPolygon(p, depthDrawable, phongShader);
				}
			}
			else{
				for(int j = 2; j < newVertices.length; j += 1) {
					pVertices[0] = firstV;
					pVertices[1] = newVertices[j-1];
					pVertices[2] = newVertices[j];
					p = Polygon.make(pVertices);
					
				filledRenderer.drawPolygon(p, depthDrawable);
				}
			}
		}
		else {
			p = Polygon.make(newVertices);
			wireframeRenderer.drawPolygon(p, depthDrawable);
		}
	}
	
	
	
	public Vertex3D[] interpretVertices(String[] tokens, int numVertices, int startingIndex) {
		VertexColors vertexColors = verticesAreColored(tokens, numVertices);	
		Vertex3D vertices[] = new Vertex3D[numVertices];
		
		for(int index = 0; index < numVertices; index++) {
			vertices[index] = interpretVertex(tokens, startingIndex + index * vertexColors.numTokensPerVertex(), vertexColors);
		}
		return vertices;
	}
	public VertexColors verticesAreColored(String[] tokens, int numVertices) {
		return hasColoredVertices(tokens, numVertices) ? VertexColors.COLORED :
														 VertexColors.UNCOLORED;
	}
	public boolean hasColoredVertices(String[] tokens, int numVertices) {
		return tokens.length == numTokensForCommandWithNVertices(numVertices);
	}
	public int numTokensForCommandWithNVertices(int numVertices) {
		return NUM_TOKENS_FOR_COMMAND + numVertices*(NUM_TOKENS_FOR_COLORED_VERTEX);
	}

	
	private Vertex3D interpretVertex(String[] tokens, int startingIndex, VertexColors colored) {
		Point3DH point = interpretPoint(tokens, startingIndex);
		
		Color color = defaultColor;
		if(colored == VertexColors.COLORED) {
			color = interpretColor(tokens, startingIndex + NUM_TOKENS_FOR_POINT);
		}

		return new Vertex3D(point, color); // finished
	}
	public static Point3DH interpretPoint(String[] tokens, int startingIndex) {
		double x = cleanNumber(tokens[startingIndex]);
		double y = cleanNumber(tokens[startingIndex + 1]);
		double z = cleanNumber(tokens[startingIndex + 2]);  

		return new Point3DH(x, y, z);   // finished
	}
	public static Color interpretColor(String[] tokens, int startingIndex) {
		double r = cleanNumber(tokens[startingIndex]);
		double g = cleanNumber(tokens[startingIndex + 1]);
		double b = cleanNumber(tokens[startingIndex + 2]);

		return new Color(r, g, b); // finished
	}

	public static Vertex3D SimpLightCalc(Vertex3D vertex) {
		return vertex.replaceColor( vertex.getColor().multiply(ambientLight) );
	}
	
	public static Vertex3D TransToCamera(Vertex3D vertex) {
		return worldToCamera.multiply(vertex);
	}
	
	public static Point3DH TransToCamera(Point3DH vector) {
		return worldToCamera.multiplyInv(vector);
	}
	
	public static Vertex3D TransWithCTM(Vertex3D vertex) {
		return CTM.multiply(vertex);
	}
	
	public static Point3DH TransWithCTM(Point3DH vector) {
		return CTM.multiplyInv(vector);
	}
	
	public static Vertex3D ProjectToViewWindow(Vertex3D vertex) {
		vertex = vertex.replaceCSPoint(vertex.getPoint3D());
		
		double viewWidth = vwx_high - vwx_low;
		double viewHeight = vwy_high - vwy_low;
		double ratio = (viewWidth < viewHeight)?(650.0/viewHeight):(650.0/viewWidth);
		double x = ratio*(-vertex.getX()/vertex.getZ()-(vwx_low+vwx_high)/2);
		double y = ratio*(-vertex.getY()/vertex.getZ()-(vwy_low+vwy_high)/2);
		double z = 1.0/vertex.getZ();
		double w = 1.0;
		
		return vertex.replacePoint(new Point3DH(x, y, z, w));
	}
	
	public static Point3DH interpretPointWithW(String[] tokens, int startingIndex) {
		double x = cleanNumber(tokens[startingIndex]);
		double y = cleanNumber(tokens[startingIndex + 1]);
		double z = cleanNumber(tokens[startingIndex + 2]);
		double w = cleanNumber(tokens[startingIndex + 3]);
		Point3DH point = new Point3DH(x, y, z, w);
		return point;
	}

	private void interpretObj(String[] tokens) {
		String quotedFilename = tokens[1];
		int length = quotedFilename.length();
		assert quotedFilename.charAt(0) == '"' && quotedFilename.charAt(length-1) == '"'; 
		String filename = quotedFilename.substring(1, length-1);
		objFile("simp/"+filename + ".obj");
	}
	
	private void objFile(String filename) {
		ObjReader objReader = new ObjReader(filename, defaultColor,ambientLight, lightPoints, kSpecular, specularExp, DoShading);
		objReader.read();
		objReader.render(clipper, shadeStyle, renderStyle, depthDrawable, filledRenderer,lineRenderer);
	}
}
