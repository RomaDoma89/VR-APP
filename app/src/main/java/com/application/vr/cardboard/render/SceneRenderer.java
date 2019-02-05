package com.application.vr.cardboard.render;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

import com.application.vr.cardboard.FPSCounter;
import com.application.vr.cardboard.models.StarsModel;
import com.application.vr.cardboard.models.TestModel;
import com.application.vr.cardboard.motion.DeviceSensorListener;
import com.application.vr.cardboard.motion.MotionCalculator;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import javax.microedition.khronos.egl.EGLConfig;

public class SceneRenderer implements GvrView.StereoRenderer {

    private static final String TAG = "MyGLRenderer";
    private Context context;
    private DeviceSensorListener sensorListener;
    private MotionCalculator mCalculator;
    private FPSCounter fpsCounter;

    private static final float Z_NEAR = 1f;
    private static final float Z_FAR = 1100.0f;

    private TestModel mModel;
    private StarsModel stars;

    // mVPMatrix is an abbreviation for "View Projection Matrix"
    private final float[] mVPMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] rotationMatrix = new float[16];

    public SceneRenderer(Context context, SensorManager mSensorManage, Sensor accelerom, Sensor magnetic) {
        this.context = context;
        sensorListener = new DeviceSensorListener(mSensorManage, accelerom, magnetic);
        mCalculator = new MotionCalculator(sensorListener);
        fpsCounter = new FPSCounter();
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        // Draw background color
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        // Clear depth buffer
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);

        // Set mViewMatrix at the origin and set a new view point.
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 0, 0, 0, -100, 0, 1, 0);
        // Matrix.setLookAtM(mViewMatrix, 0, 5, 5, 40, 0, 0, -100, 0f, 1f, 0f);

        // Create some temp matrix to store a multiplication of the rotationMatrix and new Pitch rotation.
        float[] newPitchRotationM = new float[16];
        Matrix.setIdentityM(newPitchRotationM, 0);
        Matrix.rotateM(newPitchRotationM, 0, mCalculator.getPitch(), 1.0f, 0.0f, 0.0f);
        Matrix.multiplyMM(rotationMatrix, 0, newPitchRotationM , 0, rotationMatrix, 0);

        // Create some temp matrix to store the multiplication of the rotationMatrix and new Roll rotation.
        float[] newRollRotation = new float[16];
        Matrix.setIdentityM(newRollRotation, 0);
        Matrix.rotateM(newRollRotation, 0, mCalculator.getRoll(), 0.0f, 0.0f, 1.0f);
        Matrix.multiplyMM(rotationMatrix, 0, newRollRotation , 0, rotationMatrix, 0);

        // Multiply mViewMatrix with the final rotations.
        Matrix.multiplyMM(mViewMatrix, 0, rotationMatrix, 0, mViewMatrix, 0);

        //Apply mProjectionMatrix and the updated mViewMatrix.
        Matrix.multiplyMM(mVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // Log FPS
        fpsCounter.logFrame();
    }

    @Override
    public void onDrawEye(Eye eye) {
        // Draw square
        mModel.draw(mVPMatrix);
        stars.draw(mVPMatrix);
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        // Set the background frame color
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        // Set identity to the rotationMatrix only once.
        Matrix.setIdentityM(rotationMatrix, 0);
        mModel = new TestModel(context);
        stars = new StarsModel(context);
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
