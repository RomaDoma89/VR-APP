package com.application.vr.cardboard;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;


import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import javax.microedition.khronos.egl.EGLConfig;

public class SceneRenderer implements GvrView.StereoRenderer, SensorEventListener {

    private static final String TAG = "MyGLRenderer";
    private SensorManager mSensorManage;
    private SensorInterpolator interpolator;

    private static final float Z_NEAR = 1f;
    private static final float Z_FAR = 100.0f;

    private Model mModel;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];

    private float x;
    private float y;
    private float z;

    public SceneRenderer(SensorManager mSensorManage, Sensor accelerom, Sensor magnetic) {
        this.interpolator = new SensorInterpolator();
        this.mSensorManage = mSensorManage;
        this.mSensorManage.registerListener(this, accelerom, SensorManager.SENSOR_DELAY_GAME);
        this.mSensorManage.registerListener(this, magnetic, SensorManager.SENSOR_DELAY_GAME);
    }
    float xAngle = 0f;
    float yAngle = 0f;
    float zAngle = 0f;

    double startX = 0f;
    double startY = 0f;
    double startZ = 1f;

    float[] oldRotationMatrix = new float[16];
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        // Draw background color
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        // Camera(mViewMatrix) at the origin
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 0, 0, 0, -100, 0f, 1f, 0f);
        // Camera(mViewMatrix) looks from the side
//        Matrix.setLookAtM(mViewMatrix, 0, 5, 5, 40, 0, 0, -100, 0f, 1f, 0f);

        float pitch = (float) Math.atan(x/Math.sqrt(Math.pow(y,2) + Math.pow(z,2)));
        float roll = (float) Math.atan(y/Math.sqrt(Math.pow(x,2) + Math.pow(z,2)));
        float azim = (float) Math.atan(z/Math.sqrt(Math.pow(y,2) + Math.pow(x,2)));

        //convert radians into degrees
        pitch = (float)(Math.toDegrees(pitch));
        roll = (float) (Math.toDegrees(roll));
        azim = (float) (Math.toDegrees(azim));

        if (roll < -10 || roll > 10) {
            zAngle += (roll)/100;
        } else {
            if (pitch < 80 && azim > 0) xAngle -= (90-pitch)/100;
            if (pitch < 80 && azim < 0) xAngle += (90-pitch)/100;
        }

        if (xAngle > 360) xAngle = 0 + (xAngle - 360);
        if (xAngle < 0) xAngle = 360 - xAngle;
        if (zAngle > 360) zAngle = 0 + (zAngle - 360);
        if (zAngle < 0) zAngle = 360 - zAngle;

        float[] newRotationMatrix = new float[16];
        Matrix.setIdentityM(newRotationMatrix, 0);
        float[] tmpRoteMatrix = new float[16];
        Matrix.multiplyMM(tmpRoteMatrix, 0, newRotationMatrix, 0, oldRotationMatrix, 0);

        Matrix.rotateM(tmpRoteMatrix, 0, zAngle, 0.0f, 0.0f, 1.0f);
        Matrix.rotateM(tmpRoteMatrix, 0, xAngle, 1.0f, 0.0f, 0.0f);

        float[] tmpViewMatrix = new float[16];
        Matrix.multiplyMM(tmpViewMatrix, 0, mViewMatrix, 0, tmpRoteMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, tmpViewMatrix, 0);

        oldRotationMatrix = newRotationMatrix.clone();

        if (count == 0) {
            Log.e("mViewMatrix",
                    "|\n"+mViewMatrix[0]+" | "+mViewMatrix[1]+" | "+mViewMatrix[2]+" | "+mViewMatrix[3]+
                            "|\n"+mViewMatrix[4]+" | "+mViewMatrix[5]+" | "+mViewMatrix[6]+" | "+mViewMatrix[7]+
                            "|\n"+mViewMatrix[8]+" | "+mViewMatrix[9]+" | "+mViewMatrix[10]+" | "+mViewMatrix[11]+
                            "|\n"+mViewMatrix[12]+" | "+mViewMatrix[13]+" | "+mViewMatrix[14]+" | "+mViewMatrix[15]);

//            Log.e("newRotationMatrix",
//                    "|\n"+newRotationMatrix[0]+" | "+newRotationMatrix[1]+" | "+newRotationMatrix[2]+" | "+newRotationMatrix[3]+
//                            "|\n"+newRotationMatrix[4]+" | "+newRotationMatrix[5]+" | "+newRotationMatrix[6]+" | "+newRotationMatrix[7]+
//                            "|\n"+newRotationMatrix[8]+" | "+newRotationMatrix[9]+" | "+newRotationMatrix[10]+" | "+newRotationMatrix[11]+
//                            "|\n"+newRotationMatrix[12]+" | "+newRotationMatrix[13]+" | "+newRotationMatrix[14]+" | "+newRotationMatrix[15]);
//
//            Log.e("mViewMatrix",
//                    "|\n"+tmpRotationMatrix[0]+" | "+tmpRotationMatrix[1]+" | "+tmpRotationMatrix[2]+" | "+tmpRotationMatrix[3]+
//                            "|\n"+tmpRotationMatrix[4]+" | "+tmpRotationMatrix[5]+" | "+tmpRotationMatrix[6]+" | "+tmpRotationMatrix[7]+
//                            "|\n"+tmpRotationMatrix[8]+" | "+tmpRotationMatrix[9]+" | "+tmpRotationMatrix[10]+" | "+tmpRotationMatrix[11]+
//                            "|\n"+tmpRotationMatrix[12]+" | "+tmpRotationMatrix[13]+" | "+tmpRotationMatrix[14]+" | "+tmpRotationMatrix[15]);

//            Log.e("XYZ", "|\n"+newX+" "+newY+" "+newZ);
//            Log.e("XX", "|\n"+XX+" ");
//            Log.e("YY", "|\n"+YY+" ");
            Log.e("---", "------------------------------------------------------------- ");
            count = 100;
        }
        count--;
    }


    @Override
    public void onDrawEye(Eye eye) {
        // Draw square
        mModel.draw(mMVPMatrix);
    }


    int count = 100;
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] updatedValues = interpolator.interpolate(event.values);
            x = updatedValues[0];
            y = updatedValues[1];
            z = updatedValues[2];
        }
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        // Set the background frame color
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        mModel = new Model();
    }


    @Override
    public void onSurfaceChanged(int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES30.glViewport(0, 0, width, height);

        float ratio = (float) width / height;
        float fov = 0.75f; // 0.2 to 1.0
        float size = (float) (Z_NEAR * Math.tan(fov / 2));
        //this projection matrix is applied to object coordinates
        //in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -size, size, -size/ratio, size/ratio, Z_NEAR, Z_FAR);
    }



    @Override
    public void onRendererShutdown() {}
    @Override
    public void onFinishFrame(Viewport viewport) {}
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}





    /**
     * Utility method for compiling a OpenGL shader.
     *
     * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
     * method to debug shader coding errors.</p>
     *
     * @param type - Vertex or fragment shader type.
     * @param shaderCode - String containing the shader code.
     * @return - Returns an id for the shader.
     */
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES30.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES30.glShaderSource(shader, shaderCode);
        GLES30.glCompileShader(shader);

        return shader;
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
