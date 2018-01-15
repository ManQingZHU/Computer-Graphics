package notProvided.client.interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import geometry.Point3DH;
import geometry.Vertex3D;
import line.LineRenderer;
import notProvided.client.Clipper;
import notProvided.client.interpreter.SimpInterpreter.RenderStyle;
import notProvided.client.interpreter.SimpInterpreter.ShadeStyle;
import notProvided.shading.FlatShader;
import notProvided.shading.GouraudShader;
import notProvided.shading.Lighting;
import notProvided.shading.PhongShader;
import polygon.Polygon;
import polygon.PolygonRenderer;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

class ObjReader {
	private static final char COMMENT_CHAR = '#';
	private static final int NOT_SPECIFIED = -1;

	private class ObjVertex {
		// TODO: fill this class in.  Store indices for a vertex, a texture, and a normal.  Have getters for them.
		private int vertexIndex;
		private int textureIndex;
		private int normalIndex; 
		
		protected ObjVertex() { }
		
		protected ObjVertex(int vertexIndex, int textureIndex, int normalIndex) {
			this.vertexIndex = vertexIndex;
			this.textureIndex = textureIndex;
			this.normalIndex = normalIndex;
		}

		protected int NormalIndex() {
			return normalIndex;
		}
		
		protected int TextureIndex() {
			return textureIndex;
		}
		
		protected int VertexIndex() {
			return vertexIndex;
		}
	}
	
	private class ObjFace extends ArrayList<ObjVertex> {
		private static final long serialVersionUID = -4130668677651098160L;
		
		protected Vertex3D getVertex(int index) {
			int vertexIndex = this.get(index).VertexIndex();
			int normIndex = this.get(index).NormalIndex();
			
			Vertex3D vertex = objVertices.get(vertexIndex);
			if(normIndex != NOT_SPECIFIED)
				vertex = vertex.replaceNormal(objNormals.get(normIndex));
			
			return vertex;
		}
	}	
	private LineBasedReader reader;
	
	private List<Vertex3D> objVertices;
	private List<Vertex3D> transformedVertices;
	private List<Point3DH> objNormals;
	private List<ObjFace> objFaces;

	private Color defaultColor;
	private Color ambientLight;
	private ArrayList<Lighting> lightPoints;
	private double kSpecular;
	private double specularExp;
	private FlatShader flatShader;
	private GouraudShader gouraudShader;
	private PhongShader phongShader;
	private boolean DoShading;
	
	public ObjReader(String filename, Color defaultColor, Color ambientLight, ArrayList<Lighting> lightPoints, double kSpecular, double specularExp, boolean DoShading) {
		this.defaultColor = defaultColor;
		this.ambientLight = ambientLight;
		this.lightPoints = lightPoints;
		this.kSpecular = kSpecular;
		this.specularExp = specularExp;
		this.DoShading = DoShading;
		
		reader = new LineBasedReader(filename);
		objVertices = new ArrayList<Vertex3D>();
		transformedVertices = new ArrayList<Vertex3D>();
		objNormals = new ArrayList<Point3DH>();
		objFaces = new ArrayList<ObjFace>();
	}
	
	public void render(Clipper clipper, ShadeStyle shadeStyle, RenderStyle renderStyle, Drawable drawable, PolygonRenderer filledRenderer, LineRenderer lineRenderer) {
		for(int i = 0; i < objVertices.size(); ++i) {
			Vertex3D vertex = objVertices.get(i);
			vertex =	 SimpInterpreter.TransWithCTM(vertex);
			vertex = SimpInterpreter.TransToCamera(vertex);
			objVertices.set(i, vertex);
		}
		for(int i = 0; i < objNormals.size(); ++i) {
			Point3DH norm = objNormals.get(i);	
			norm = SimpInterpreter.TransWithCTM(norm);
			norm = SimpInterpreter.TransToCamera(norm);
			norm = norm.normalize();
			objNormals.set(i, norm);
		}
		
		for(int i = 0; i < objFaces.size(); ++i) {
			ObjFace face = objFaces.get(i);

			Vertex3D firstV = face.getVertex(0);
					
			if(renderStyle == RenderStyle.FILLED) {
				for(int j = 2; j < face.size(); j += 1) {
					Vertex3D[] vertices = new Vertex3D[3];
					vertices[0] = firstV;
					vertices[1] = face.getVertex(j-1);
					vertices[2] = face.getVertex(j);
					
					Point3DH vector1, vector2, faceNorm;
					vector1 = vertices[1].getPoint3D().subtract(vertices[0].getPoint3D());
					vector2 = vertices[2].getPoint3D().subtract(vertices[0].getPoint3D());
					faceNorm = vector1.crossMultiply3D(vector2).normalize();
					
					Polygon p = Polygon.make(vertices);
					p.setNorm(faceNorm);
					
					if(DoShading && shadeStyle == ShadeStyle.FLAT) {
						flatShader = new FlatShader(ambientLight, lightPoints, kSpecular, specularExp);
						p = flatShader.shade(p);
					}
					else if(DoShading && shadeStyle == ShadeStyle.GOURAUD) {
						gouraudShader = new GouraudShader(ambientLight, lightPoints, kSpecular, specularExp);
						p = gouraudShader.shade(p);
					}
					
					p = clipper.PolygonClipping3D(p);
					if(p.length() == 0)
						continue;
				
					// Projection to view windows (remain the cs coords and norms)
					Vertex3D[] newVertices = new Vertex3D[p.length()];
					int ii;
					for(ii = 0; ii < p.length(); ii++) {
						newVertices[ii] =  SimpInterpreter.ProjectToViewWindow(p.get(ii));
						if(ii >= 2)
						{
							if(Polygon.isClockwise(newVertices[ii-2], newVertices[ii-1], newVertices[ii]))
								break;
						}
					}
					if(ii < p.length())
						break;
					
					Vertex3D fV = newVertices[0];
					Vertex3D[] pVertices = new Vertex3D[3];
					
					if(DoShading && shadeStyle == ShadeStyle.PHONG) {
						phongShader = new PhongShader(ambientLight, lightPoints, kSpecular, specularExp);
						for(int j1 = 2; j1 < newVertices.length; j1 += 1) {
							pVertices[0] = fV;
							pVertices[1] = newVertices[j1-1];
							pVertices[2] = newVertices[j1];
							p = Polygon.make(pVertices);
							filledRenderer.drawPolygon(p, drawable, phongShader);
						}
					}
					else {
						p = Polygon.make(newVertices);
						filledRenderer.drawPolygon(p, drawable);
					}
				}
			}
			else {
				for(int j = 1; j < face.size(); ++j) {
					Vertex3D[] vertices = new Vertex3D[2];
					vertices[0] = face.getVertex(j-1);
					vertices[1] = face.getVertex(j);
					
					vertices = clipper.LineClipping3D(vertices[0], vertices[1]);
					if(vertices == null)
						continue;

					vertices[0] = SimpInterpreter.ProjectToViewWindow(vertices[0]);
					vertices[1] = SimpInterpreter.ProjectToViewWindow(vertices[1]);
					lineRenderer.drawLine(vertices[0], vertices[1], drawable);
				}
				
				Vertex3D[] vertices = new Vertex3D[2];
				vertices[0] = objVertices.get(face.get(face.size()-1).VertexIndex());
				vertices[1] = firstV;
				vertices = clipper.LineClipping3D(vertices[0], vertices[1]);
				if(vertices == null)
					continue;

				vertices[0] = SimpInterpreter.ProjectToViewWindow(vertices[0]);
				vertices[1] = SimpInterpreter.ProjectToViewWindow(vertices[1]);
				lineRenderer.drawLine(vertices[0], vertices[1], drawable);
			}
		}
	}

	public void read() {
		while(reader.hasNext() ) {
			String line = reader.next().trim();
			interpretObjLine(line);
		}
	}
	private void interpretObjLine(String line) {
		if(!line.isEmpty() && line.charAt(0) != COMMENT_CHAR) {
			String[] tokens = line.split("[ \t,()]+");
			if(tokens.length != 0) {
				interpretObjCommand(tokens);
			}
		}
	}

	private void interpretObjCommand(String[] tokens) {
		switch(tokens[0]) {
		case "v" :
		case "V" :
			interpretObjVertex(tokens);
			break;
		case "vn":
		case "VN":
			interpretObjNormal(tokens);
			break;
		case "f":
		case "F":
			interpretObjFace(tokens);
			break;
		default:	// do nothing
			break;
		}
	}
	private void interpretObjFace(String[] tokens) { // finished
		ObjFace face = new ObjFace();
		
		for(int i = 1; i<tokens.length; i++) {
			String token = tokens[i];
			String[] subtokens = token.split("/");
			
			int vertexIndex  = objIndex(subtokens, 0, objVertices.size());
			int textureIndex = objIndex(subtokens, 1, 0);
			int normalIndex  = objIndex(subtokens, 2, objNormals.size());

			ObjVertex vertex = new ObjVertex(vertexIndex, textureIndex, normalIndex);
			face.add(vertex);
		}
		objFaces.add(face);
	}

	private int objIndex(String[] subtokens, int tokenIndex, int baseForNegativeIndices) {
		// TODO: write this.  subtokens[tokenIndex], if it exists, holds a string for an index.
		// use Integer.parseInt() to get the integer value of the index.
		// Be sure to handle both positive and negative indices.
		int index = NOT_SPECIFIED;
		if(subtokens.length > tokenIndex && subtokens[tokenIndex].length() > 0){
			index = Integer.parseInt(subtokens[tokenIndex]);
			// convert to 0-based index, -1 represents not exist
			if(index < 0)
				index += baseForNegativeIndices;
			else if(index > 0)
				index -= 1;
		}
		
		return index;  // error check
	}

	private void interpretObjNormal(String[] tokens) {  // finished
		int numArgs = tokens.length - 1;
		if(numArgs != 3) {
			throw new BadObjFileException("vertex normal with wrong number of arguments : " + numArgs + ": " + tokens);				
		}
		Point3DH normal = SimpInterpreter.interpretPoint(tokens, 1);
		
		objNormals.add(normal);
	}
	private void interpretObjVertex(String[] tokens) {  // finished
		int numArgs = tokens.length - 1;
		Point3DH point = objVertexPoint(tokens, numArgs);
		Color color = objVertexColor(tokens, numArgs);
		
		Vertex3D vertex = new Vertex3D(point, color);
		objVertices.add(vertex);
	}

	private Color objVertexColor(String[] tokens, int numArgs) {
		if(numArgs == 6) {
			return SimpInterpreter.interpretColor(tokens, 4);
		}
		if(numArgs == 7) {
			return SimpInterpreter.interpretColor(tokens, 5);
		}
		return defaultColor;
	}

	private Point3DH objVertexPoint(String[] tokens, int numArgs) {
		if(numArgs == 3 || numArgs == 6) {
			return SimpInterpreter.interpretPoint(tokens, 1);
		}
		else if(numArgs == 4 || numArgs == 7) {
			return SimpInterpreter.interpretPointWithW(tokens, 1);
		}
		throw new BadObjFileException("vertex with wrong number of arguments : " + numArgs + ": " + tokens);
	}
}