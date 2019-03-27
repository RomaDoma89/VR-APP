package com.application.vr.cardboard.render;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

import com.application.vr.cardboard.Camera;
import com.application.vr.cardboard.FPSCounter;
import com.application.vr.cardboard.app_mode.InputMode;
import com.application.vr.cardboard.levels.TestLevel;
import com.application.vr.cardboard.models.ui_models.UiCreator;
import com.application.vr.cardboard.models.interfaces.DynamicModel;
import com.application.vr.cardboard.models.interfaces.StaticModel;
import com.application.vr.cardboard.motion.DeviceSensorListener;
import com.application.vr.cardboard.motion.MotionCalculator;
import com.application.vr.cardboard.motion.MotionInterpolator;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;

public class SceneRenderer implements GvrView.StereoRenderer {

    private static final String TAG = "SceneRenderer";
    private static final float Z_NEAR = 1f;
    private static final float Z_FAR = 1100.0f;
    private int INPUT_MODE;

    private Context context;
    private Camera camera;
    private FPSCounter fpsCounter;

    private List<DynamicModel> dynamicModels;
    private List<StaticModel> staticModels;
    private UiCreator uiCreator;

    // dynamicVPMatrix is an abbreviation for "View Projection Matrix"
    private final float[] dynamicVPMatrix = new float[16];
    private final float[] staticVPMatrix = new float[16];
    private final float[] uiVPMatrix = new float[16];

    public SceneRenderer(Context context, int inputMode) {
        this.context = context;
        this.INPUT_MODE = inputMode;

        SensorManager mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        DeviceSensorListener sensorListener = new DeviceSensorListener(mSensorManager, accelerometer);
        MotionCalculator mCalculator = new MotionCalculator(sensorListener);

        camera = new Camera(mCalculator);
        dynamicModels = new ArrayList<>();
        staticModels = new ArrayList<>();

        fpsCounter = new FPSCounter();
    }

    private float[] eulerAngles = new float[3];
    private float[] viewMatrix = new float[16];
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        // Draw background color
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        // Clear depth buffer
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        // Initialize a new forward vector;
        float[] forwardVec = new float[3];

        //Transform camera for InputMode.ACCELEROMETER:
        if (INPUT_MODE == InputMode.ACCELEROM) {
            camera.transform();
            // Update the current forward vector
            camera.getForwardVec(forwardVec);
            camera.getEulerAngles(eulerAngles);
            viewMatrix = camera.getCompleteViewMatrix();
        }

        // Get a new forward vector for InputMode.GYROSCOPE:
        else if (INPUT_MODE == InputMode.GYROSCOPE) {
            // Update the current forward vector
            headTransform.getForwardVector(forwardVec, 0);
            headTransform.getEulerAngles(eulerAngles, 0);
            headTransform.getHeadView(viewMatrix, 0);
        }

        // Update speed value
        float speed = camera.getSpeed();
        // Move the dynamic models to implement the camera movement
        for (DynamicModel m : dynamicModels) m.moveByCamera(forwardVec, speed);
        // Log FPS
        fpsCounter.logFrame();
    }

    @Override
    public void onDrawEye(Eye eye) {
        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
        if (INPUT_MODE == InputMode.ACCELEROM) {
            Matrix.multiplyMM(dynamicVPMatrix, 0, perspective, 0, camera.getCompleteViewMatrix(), 0);
            Matrix.multiplyMM(staticVPMatrix, 0, perspective, 0, camera.getCompleteViewMatrix(), 0);
        } else if (INPUT_MODE == InputMode.GYROSCOPE){
            // Get an eye view matrix and interpolate it to reduce camera shaking
            float[] normalizedEyeView = MotionInterpolator.interpolateView(eye.getEyeView());
            Matrix.multiplyMM(dynamicVPMatrix, 0, perspective, 0, normalizedEyeView, 0);
            Matrix.multiplyMM(staticVPMatrix, 0, perspective, 0, normalizedEyeView, 0);
        }

        Matrix.multiplyMM(uiVPMatrix, 0, perspective, 0, camera.getUiViewMatrix(), 0);
        //Draw all models including UI elements.
        drawModel();
    }

    private void drawModel() {
        // Draw the dynamic models with imitating camera movement
        for (DynamicModel m : dynamicModels) m.draw(dynamicVPMatrix);
        // Draw the static models without movement
        for (StaticModel m : staticModels) m.draw(staticVPMatrix);
        // Draw the UI elements including the map with the dynamic models
        uiCreator.draw(uiVPMatrix, viewMatrix, dynamicModels, 10, 8, 6, 14, 20, 10);
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        // Set the background frame color
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        TestLevel.generateAll(context, dynamicModels, staticModels);
//        uiCreator = new UiCreator(context, xScale, yScale);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        float xScale = width/1000f;
        float yScale = height/1000f;
        //Create an instance of the UiCreator to put screen parameters for scaling
        uiCreator = new UiCreator(context, xScale, yScale);
    }

    @Override
    public void onRendererShutdown() {}

    @Override
    public void onFinishFrame(Viewport viewport) {
    }

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
//    |    0     -sin(θ)   cos(θ)     0    |  [1]
//    |    0        0        0        1    |

//        Rotate Y
//    |  cos(θ)     0    -sin(θ)      0    |
//    |    0        1        0        0    |
//    |  sin(θ)     0     cos(θ)      0    |
//    |    0        0        0        1    |

//        Rotate Z
//    |  cos(θ)  -sin(θ)     0        0    |
//    |  sin(θ)   cos(θ)     0        0    |  [4] - for Z by Y
//    |    0        0        1        0    |
//    |    0        0        0        1    |

//    |  cos(θ)  -sin(θ)  -sin(θ)     0    |
//    |  sin(θ)   cos(θ)   sin(θ)     0    |
//    |  sin(θ)  -sin(θ)   cos(θ)     0    |
//    |    0        0        0        1    |