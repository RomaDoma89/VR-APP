package com.application.vr.cardboard.file_utils;

import android.content.Context;
import android.opengl.GLES30;

public class ShaderUtils {
    /**
     * Create empty OpenGL Program.
     * */
    public static int createProgram(int vertexShaderId, int fragmentShaderId) {
        final int programId = GLES30.glCreateProgram();
        if (programId == 0) return 0;

        GLES30.glAttachShader(programId, vertexShaderId);
        GLES30.glAttachShader(programId, fragmentShaderId);

        GLES30.glLinkProgram(programId);
        final int[] linkStatus = new int[1];
        GLES30.glGetProgramiv(programId, GLES30.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            GLES30.glDeleteProgram(programId);
            return 0;
        }
        return programId;
    }

    /**
     * Create OpenGL shader
     * */
    public static int createShader(Context context, int type, int shaderRawId) {
        String shaderText = FileUtils.readTextFromRaw(context, shaderRawId);
        return ShaderUtils.createShader(type, shaderText);
    }

    /**
     * Create OpenGL shader
     * */
    public static int createShader(int type, String shaderText) {
        final int shaderId = GLES30.glCreateShader(type);
        if (shaderId == 0) return 0;
        GLES30.glShaderSource(shaderId, shaderText);
        GLES30.glCompileShader(shaderId);
        final int[] compileStatus = new int[1];
        GLES30.glGetShaderiv(shaderId, GLES30.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            GLES30.glDeleteShader(shaderId);
            return 0;
        }
        return shaderId;
    }
}
