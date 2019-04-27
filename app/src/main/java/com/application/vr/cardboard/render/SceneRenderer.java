package com.application.vr.cardboard.render;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.opengl.GLES10;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

import com.application.vr.cardboard.Camera;
import com.application.vr.cardboard.FPSCounter;
import com.application.vr.cardboard.app_mode.HeadMode;
import com.application.vr.cardboard.control.HeadController;
import com.application.vr.cardboard.control.events.DownEvent;
import com.application.vr.cardboard.control.events.LeftEvent;
import com.application.vr.cardboard.control.events.RightEvent;
import com.application.vr.cardboard.control.events.TopEvent;
import com.application.vr.cardboard.levels.TestLevel;
import com.application.vr.cardboard.models.interfaces.Model;
import com.application.vr.cardboard.models.ui_models.TipsHead;
import com.application.vr.cardboard.models.ui_models.UiCreator;
import com.application.vr.cardboard.models.interfaces.DynamicModel;
import com.application.vr.cardboard.models.interfaces.StaticModel;
import com.application.vr.cardboard.motion.AccelerometerListener;
import com.application.vr.cardboard.motion.GyroscopeListener;
import com.application.vr.cardboard.motion.MotionInterpolator;
import com.application.vr.cardboard.motion.MotionManager;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;

import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;

public class SceneRenderer implements GvrView.StereoRenderer {

    private static final String TAG = "SceneRenderer";
    private static final float Z_NEAR = 1f;
    private static final float Z_FAR = 10000.0f;
    private int HEAD_MODE;

    private Context context;
    private Camera camera;
    private HeadController headController;
    private FPSCounter fpsCounter;

    private List<Model> allModels;
    private List<DynamicModel> dynamicModels;
    private List<StaticModel> staticModels;
    private UiCreator uiCreator;
    private final float[] globalLightColor = new float[] { 0.5f, 0.5f, 0.5f };
    private final float[] globalLightPosition = new float[] {60, 1095, -5000};

    private float xLightPosition = 20f, yLightPosition = 10f, zLightPosition = -50f;

    // moveVPMatrix is an abbreviation for "View Projection Matrix"
    private float[] viewMatrix = new float[16];
    private float[] completeVPMatrix = new float[16];
    private float[] uiVPMatrix = new float[16];
    private float[] uiMapViewMatrix = new float[16];
    // Initialize a new forward vector;
    private float[] forwardVec = new float[3];

    public SceneRenderer(Context context, int inputMode) {
        this.context = context;
        this.HEAD_MODE = inputMode;

        SensorManager mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        AccelerometerListener accListener = new AccelerometerListener(mSensorManager, accelerometer);
        GyroscopeListener gyrListener = new GyroscopeListener(mSensorManager, gyroscope);
        MotionManager mCalculator = new MotionManager(accListener, gyrListener);

        camera = new Camera(mCalculator);
        dynamicModels = new ArrayList<>();
        staticModels = new ArrayList<>();
        headController = new HeadController(accListener);

        fpsCounter = new FPSCounter();
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        // Draw background color
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT | GLES30.GL_COLOR_BUFFER_BIT);
        // Clear depth buffer
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
//        GLES30.glEnable(GLES30.GL_)

        if (HEAD_MODE == HeadMode.FREE_HEAD) {
            // Update the current forward vector
            headTransform.getForwardVector(forwardVec, 0);
            headTransform.getHeadView(uiMapViewMatrix, 0);
        } else {
            // Compute the view point matrix
            camera.transform();
            // Update the current forward vector
            camera.getForwardVec(forwardVec);
            uiMapViewMatrix = camera.getCompleteView();
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
        if (HEAD_MODE == HeadMode.FREE_HEAD) {
            // Get an eye view matrix and interpolate it to reduce camera shaking
            float[] normalizedEyeView = MotionInterpolator.interpolateView(eye.getEyeView());
            Matrix.multiplyMM(completeVPMatrix, 0, perspective, 0, normalizedEyeView, 0);
            Matrix.multiplyMM(uiVPMatrix, 0, perspective, 0, camera.getStraightView(), 0);
            viewMatrix = normalizedEyeView;
        } else {
            Matrix.multiplyMM(uiVPMatrix, 0, perspective, 0, camera.getYawView(), 0);
            Matrix.multiplyMM(completeVPMatrix, 0, perspective, 0, camera.getCompleteView(), 0);
            viewMatrix = camera.getCompleteView();
        }

//        Matrix.multiplyMV(lightPosInEyeSpace, 0, viewMatrix, 0, , 0);
        //Draw all models.
        for (Model m : allModels) m.draw(completeVPMatrix, viewMatrix, globalLightPosition, globalLightColor);
        // Draw the UI elements including the map with the dynamic models
        uiCreator.draw(uiVPMatrix, uiMapViewMatrix, dynamicModels, camera.getSpeedScaleVal(),
                8, 6, 3, 10, 8);
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        // Set the background frame color
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        allModels = TestLevel.generateAll(context, dynamicModels, staticModels);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        float xScale = width/1000f;
        float yScale = height/1000f;

        // Устанавливаем OpenGL окно просмотра того же размера что и поверхность экрана.
        GLES30.glViewport(0, 0, width, height);

        // Создаем новую матрицу проекции. Высота остается та же,
        // а ширина будет изменяться в соответствии с соотношением сторон.
        final float ratio = (float) width / height;

        Log.e("XY", xScale+" "+yScale);
        Log.e("ratio", ratio+" ");
        //Create an instance of the UiCreator to put screen parameters for scaling
        uiCreator = new UiCreator(context, xScale, yScale, ratio);
    }

    public void setHeadMode(int HEAD_MODE) {
        this.HEAD_MODE = HEAD_MODE;
    }

    public int putDownEvent(DownEvent e) {
        if (e.isReached()) {
            camera.speedDown();
            uiCreator.removeUiHead(TipsHead.Direction.DOWN);
        }
        return camera.getSpeedScaleVal();
    }

    public int putTopEvent(TopEvent e) {
        if (e.isReached()) {
            camera.speedUp();
            uiCreator.removeUiHead(TipsHead.Direction.UP);
        }
        return camera.getSpeedScaleVal();
    }

    public int putRightEvent(RightEvent e) {
//        if (e.isReached()) {
//            uiCreator.changeColorScheme();
//            uiCreator.removeUiHead(TipsHead.Direction.RIGHT);
//        }
        return 0;
    }

    public int putLeftEvent(LeftEvent e) {
//        if (e.isReached()) {
//            uiCreator.showMap();
//            uiCreator.removeUiHead(TipsHead.Direction.LEFT);
//        }
        return 0;
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