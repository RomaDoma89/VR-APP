package com.application.vr.cardboard.render;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

import com.application.vr.cardboard.Camera;
import com.application.vr.cardboard.FPSCounter;
import com.application.vr.cardboard.models.factories.FactoryAsteroid;
import com.application.vr.cardboard.models.factories.FactorySpaceshipCargo;
import com.application.vr.cardboard.models.factories.FactorySpaceshipHunter;
import com.application.vr.cardboard.models.Stars;
import com.application.vr.cardboard.models.TestModel;
import com.application.vr.cardboard.models.interfaces.Model;
import com.application.vr.cardboard.motion.DeviceSensorListener;
import com.application.vr.cardboard.motion.MotionCalculator;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;

public class SceneRenderer implements GvrView.StereoRenderer {

    private static final String TAG = "MyGLRenderer";
    private List<Model> models;
    private Context context;
    private Camera camera;
    private FPSCounter fpsCounter;

    private static final float Z_NEAR = 1f;
    private static final float Z_FAR = 1100.0f;

    private FactoryAsteroid asteroidFactory;
    private FactorySpaceshipCargo cargoFactory;
    private FactorySpaceshipHunter hunterFactory;
    private TestModel testModel;
    private Stars stars;

    // mVPMatrix is an abbreviation for "View Projection Matrix"
    private final float[] mVPMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mRotatedOnlyMatrix = new float[16];

    public SceneRenderer(Context context, SensorManager mSensorManage, Sensor accelerom, Sensor magnetic) {
        this.context = context;
        models = new ArrayList<>();

        DeviceSensorListener sensorListener = new DeviceSensorListener(mSensorManage, accelerom, magnetic);
        MotionCalculator mCalculator = new MotionCalculator(sensorListener);

        camera = new Camera(mCalculator);
        fpsCounter = new FPSCounter();

        asteroidFactory = new FactoryAsteroid();
        cargoFactory = new FactorySpaceshipCargo();
        hunterFactory = new FactorySpaceshipHunter();
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        // Draw background color
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        // Clear depth buffer
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);

        //Draw camera view and update mVPMatrix to draw models.
        mRotatedOnlyMatrix = camera.draw(mVPMatrix, mProjectionMatrix);

        // Log FPS
        fpsCounter.logFrame();
    }

    @Override
    public void onDrawEye(Eye eye) {
        for (Model m : models) m.draw(mVPMatrix);
        stars.draw(mRotatedOnlyMatrix.clone(), mProjectionMatrix.clone(), camera.getSpeed());
        testModel.draw(mVPMatrix);
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        // Set the background frame color
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        models.add(asteroidFactory.create(context, 70, -55, -60, 0f, 0.5f, 0.5f, 15f));
        models.add(asteroidFactory.create(context, -10, -100, -6, 0.5f, 0f, 0.5f, 5f));
        models.add(asteroidFactory.create(context, -40, 35, 30, 0.5f, 0.5f, 0f, 2f));
        models.add(asteroidFactory.create(context, 40, 68, 90, 0.5f, 0.5f, 0f, 8f));

        models.add(cargoFactory.create(context, 16f, -5f, -150f, 0f, 0f, 0f, 0f));
        models.add(cargoFactory.create(context, 0f, -15f, -155f, 0f, 0f, 0f, 0f));
        models.add(cargoFactory.create(context, -19f, -10f, -140f, 0f, 0f, 0f, 0f));

        models.add(hunterFactory.create(context, 10f, 25f, -170f,  0f, 0f, 0f, 0f));
        models.add(hunterFactory.create(context, 20f, 55f, -180f,  0f, 0f, 0f, 0f));
        models.add(hunterFactory.create(context, 30f, 35f, -180f,  0f, 0f, 0f, 0f));
        models.add(hunterFactory.create(context, 40f, 75f, -190f,  0f, 0f, 0f, 0f));

        testModel = new TestModel(context);
        stars = new Stars(context);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES30.glViewport(0, 0, width, height);

        float ratio = (float) width / height;
        float fov = 0.75f; // 0.2 to 1.0
        float size = (float) (Z_NEAR * Math.tan(fov / 2));
        // This projection matrix is applied to object coordinates in the onDrawFrame() method.
        Matrix.frustumM(mProjectionMatrix, 0, -size, size, -size/ratio, size/ratio, Z_NEAR, Z_FAR);
    }

    @Override
    public void onRendererShutdown() {}
    @Override
    public void onFinishFrame(Viewport viewport) {}

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES30.glGetError()) != GLES30.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
}

//        COMMON INDEX
//    |    0        4        8       12    |     Translate:       Scale:          Rotate X                 Rotate Y                 Rotate Z
//    |    1        5        9       13    |     m[12] = x;       m[0] = sx;      m[5] = cos(radians);     m[0] = cos(radians);     m[0] = cos(radians);
//    |    2        6       10       14    |     m[13] = y;       m[5] = sy;      m[6] = -sin(radians);    m[2] = sin(radians);     m[1] = sin(radians);
//    |    3        7       11       15    |     m[14] = z;       m[10] = sz;     m[9] = -m[6];            m[8] = -m[2];            m[4] = -m[1];
//                                                                                m[10] = m[5];            m[10] = m[0];            m[5] = m[0];

//       Rotate X  where θ angle id Degrees
//    |    1        0        0        0    |
//    |    0      cos(θ)   sin(θ)     0    |
//    |    0     -sin(θ)   cos(θ)     0    |
//    |    0        0        0        1    |

//        Rotate Y
//    |  cos(θ)     0    -sin(θ)      0    |
//    |    0        1        0        0    |
//    |  sin(θ)     0     cos(θ)      0    |
//    |    0        0        0        1    |

//        Rotate Z
//    |  cos(θ)  -sin(θ)     0        0    |
//    |  sin(θ)   cos(θ)     0        0    |
//    |    0        0        1        0    |
//    |    0        0        0        1    |
