package items.image.m3g;

import utils.*;
import items.FSuperItem;
import items.image.FCanvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import javax.microedition.m3g.Appearance;
import javax.microedition.m3g.Background;
import javax.microedition.m3g.Camera;
import javax.microedition.m3g.Graphics3D;
import javax.microedition.m3g.Image2D;
import javax.microedition.m3g.IndexBuffer;
import javax.microedition.m3g.Light;
import javax.microedition.m3g.Material;
import javax.microedition.m3g.Texture2D;
import javax.microedition.m3g.Transform;
import javax.microedition.m3g.TriangleStripArray;
import javax.microedition.m3g.VertexArray;
import javax.microedition.m3g.VertexBuffer;

/**
 *
 */
public class Cube extends FSuperItem {

    private static Graphics3D iG3D = null;
    private static Camera iCamera = null;
    private static Light iLight = null;
    private static float iAngle;//no compatible with CLDC1.0 preverify
    private static Transform iTransform = new Transform();
    private static Background iBackground = new Background();
    private static VertexBuffer iVb = null;    // positions, normals, colors, texcoords
    private static IndexBuffer iIb = null;    // indices to iVB, forming triangle strips
    private static Appearance iAppearance = null; // material, texture, compositing, ...
    private static Material iMaterial = new Material();
    private static Image iImage = null;
    //private static String          iImagePath;
    /** Some constants */
    private static final int CUBE_BACKGROUND_COLOR = 0xd3d7cf;
    //TODO delete use of float to use CLDC1.0
    private static final float CUBE_SHININESS = 100.0f;
    // Each line in this array declaration represents a triangle strip
    // for one side of a cube. The only primitive we can draw with is the
    // triangle strip so if we want to make a cube with hard edges we
    // need to construct one triangle strip per face of the cube.
    // 1 * * * * * 0
    //   * *     *
    //   *   *   *
    //   *     * *
    // 3 * * * * * 2
    // The ASCII diagram above represents the vertices in the first line
    // (the first tri-strip)
    private static final byte LENGTH_24 = 24;
    //VERT.lenght / 3
    //or NORM.length / 3
    //or TEX.length / 2
    private static final short[] VERT = {
        10, 10, 10, -10, 10, 10, 10, -10, 10, -10, -10, 10, // front
        -10, 10, -10, 10, 10, -10, -10, -10, -10, 10, -10, -10, // back
        -10, 10, 10, -10, 10, -10, -10, -10, 10, -10, -10, -10, // left
        10, 10, -10, 10, 10, 10, 10, -10, -10, 10, -10, 10, // right
        10, 10, -10, -10, 10, -10, 10, 10, 10, -10, 10, 10, // top
        10, -10, 10, -10, -10, 10, 10, -10, -10, -10, -10, -10}; // bottom
    // The per-vertex normals for the cube; these match with the vertices
    // above. Each normal is perpendicular to the surface of the object at
    // the corresponding vertex.
    private static final byte[] NORM = {
        0, 0, 127, 0, 0, 127, 0, 0, 127, 0, 0, 127,
        0, 0, -127, 0, 0, -127, 0, 0, -127, 0, 0, -127,
        -127, 0, 0, -127, 0, 0, -127, 0, 0, -127, 0, 0,
        127, 0, 0, 127, 0, 0, 127, 0, 0, 127, 0, 0,
        0, 127, 0, 0, 127, 0, 0, 127, 0, 0, 127, 0,
        0, -127, 0, 0, -127, 0, 0, -127, 0, 0, -127, 0};
    // per vertex texture coordinates
    private static final short[] TEX = {
        1, 0, 0, 0, 1, 1, 0, 1,
        1, 0, 0, 0, 1, 1, 0, 1,
        1, 0, 0, 0, 1, 1, 0, 1,
        1, 0, 0, 0, 1, 1, 0, 1,
        1, 0, 0, 0, 1, 1, 0, 1,
        1, 0, 0, 0, 1, 1, 0, 1};
    // the length of each triangle strip
    private static final int[] STRIP_LEN = {4, 4, 4, 4, 4, 4};

    public Cube(Image image) {
        super("");
        iImage = image;
        try {
            init();
        } catch (Throwable ex) {
            //Utils.debugModePrintStack(ex, "Cube", "constructor");
        }
    }

    /**
     * Component initialization.
     */
    private void init() throws Throwable {
        iAngle = 0.0f;
        // add the Exit command
        //addCommand(new Command("Exit", Command.EXIT, 1));

        // get the singleton Graphics3D instance
        iG3D = Graphics3D.getInstance();

        // create a camera
        iCamera = new Camera();
        iCamera.setPerspective(60.0f, // field of view
                (float) FCanvas.canvasWidth / (float) FCanvas.canvasHeight, // aspectRatio
                1.0f, // near clipping plane
                1000.0f); // far clipping plane

        // create a light
        iLight = new Light();
        iLight.setColor(Utils.COLOR_WHITE);  // white light
        iLight.setIntensity(1.25f);          // overbright

        // init some arrays for our object (cube)

        // create a VertexArray to hold the vertices for the object
        VertexArray vertArray = new VertexArray(LENGTH_24, 3, 2);
        vertArray.set(0, LENGTH_24, VERT);

        // create a vertex array for the normals of the object
        VertexArray normArray = new VertexArray(LENGTH_24, 3, 1);
        normArray.set(0, LENGTH_24, NORM);



        // create a vertex array for the texture coordinates of the object
        VertexArray texArray = new VertexArray(LENGTH_24, 2, 2);
        texArray.set(0, LENGTH_24, TEX);

        // create the VertexBuffer for our object
        VertexBuffer vb = iVb = new VertexBuffer();
        vb.setPositions(vertArray, 1.0f, null);      // unit scale, zero bias
        vb.setNormals(normArray);
        vb.setTexCoords(0, texArray, 1.0f, null);    // unit scale, zero bias

        // create the index buffer for our object (this tells how to
        // create triangle strips from the contents of the vertex buffer).
        iIb = new TriangleStripArray(0, STRIP_LEN);

        // load the image for the texture
        //iImage = Image.createImage( iImagePath );

        // create the Image2D (we need this so we can make a Texture2D)
        Image2D image2D = new Image2D(Image2D.RGB, iImage);

        // create the Texture2D

        // texture color is to be modulated with the lit material color
        Texture2D texture = new Texture2D(image2D);
        texture.setFiltering(Texture2D.FILTER_NEAREST,
                Texture2D.FILTER_NEAREST);
        texture.setWrapping(Texture2D.WRAP_CLAMP,
                Texture2D.WRAP_CLAMP);
        texture.setBlending(Texture2D.FUNC_MODULATE);

        // create the appearance
        iAppearance = new Appearance();
        iAppearance.setTexture(0, texture);
        iAppearance.setMaterial(iMaterial);
        iMaterial.setColor(Material.DIFFUSE, Utils.COLOR_TANGO_ALUMINIUM1);   // white
        iMaterial.setColor(Material.SPECULAR, Utils.COLOR_TANGO_ALUMINIUM1);  // white
        iMaterial.setShininess(CUBE_SHININESS);

        iBackground.setColor(CUBE_BACKGROUND_COLOR); // set the background color
    }

    /**
     * Paint the scene.
     * @param g
     */
    public void paint(Graphics g) {
        // Bind the Graphics of this Canvas to our Graphics3D. The
        // viewport is automatically set to cover the entire clipping
        // rectangle of the Graphics object. The parameters indicate
        // that z-buffering, dithering, and true color rendering are
        // enabled, but antialiasing is disabled.
        iG3D.bindTarget(g, true,
                Graphics3D.DITHER |
                Graphics3D.TRUE_COLOR);

        // clear the color and depth buffers
        iG3D.clear(iBackground);

        // set up the camera in the desired position
        Transform transform = new Transform();
        transform.postTranslate(0.0f, 0.0f, 45.0f);
        iG3D.setCamera(iCamera, transform);

        // set up a "headlight": a directional light shining
        // from the direction of the camera
        iG3D.resetLights();
        iG3D.addLight(iLight, transform);

        // update our transform (this will give us a rotating cube)
        iAngle += 2.0f;
        iTransform.setIdentity();
        iTransform.postRotate(iAngle, // rotate 1 degree per frame
                0.0f, 1.0f, 0.0f);  // rotate around this axis

        // Render our cube. We provide the vertex and index buffers
        // to specify the geometry; the appearance so we know what
        // material and texture to use; and the transform to tell
        // where to render the object
        iG3D.render(iVb, iIb, iAppearance, iTransform);

        // flush
        iG3D.releaseTarget();
    }
}
