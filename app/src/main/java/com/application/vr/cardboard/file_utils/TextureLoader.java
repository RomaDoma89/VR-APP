/*
 * Copyright 2018 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.application.vr.cardboard.file_utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLUtils;

import java.io.IOException;

/** A texture, meant for use with TexturedMesh. */
public class TextureLoader {
    private final int[] textureId = new int[1];

    /**
     * Initializes the texture.
     *
     * @param context Context for loading the texture file.
     * @param texturePath Path to the image to use for the texture.
     */
    public TextureLoader(Context context, String texturePath) throws IOException {
        GLES30.glGenTextures(1, textureId, 0);
        bind();
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_LINEAR);
        Bitmap textureBitmap = BitmapFactory.decodeStream(context.getAssets().open(texturePath));
        // Use tightly packed data
        GLES30.glPixelStorei(GLES30.GL_UNPACK_ALIGNMENT, 1);
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, textureBitmap, 0);
        textureBitmap.recycle();
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
    }

    /** Binds the texture to GL_TEXTURE0. */
    public void bind() {
//        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId[0]);
    }
    public void unbind() {
        GLES30.glBindTexture (GLES30.GL_TEXTURE_2D, 0);
    }
}
