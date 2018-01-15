package notProvided.shading;

import geometry.Vertex3D;
import windowing.graphics.Color;

public class NullShader implements Shader{
	public NullShader() {}
	
	@Override
	public Color shade(Vertex3D vertex) {
		return vertex.getColor();
	}

}
