package CGFramework.myself;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_NONE;
import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;
import static org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT24;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_COMPARE_FUNC;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_COMPARE_MODE;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;

/**
 * Created by S on 02.07.2015.
 */
public class FrameBufferFactory {

    public static int setupPostProcessFrameBuffer(int texbuf, int width, int height) {
        int framebuf = glGenFramebuffers();
        glBindFramebuffer( GL_FRAMEBUFFER, framebuf );
        glFramebufferTexture2D( GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                GL_TEXTURE_2D, texbuf, 0 );
        int depthrenderbuffer = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, depthrenderbuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
                GL_RENDERBUFFER, depthrenderbuffer);
        //glEnable(GL_FRAMEBUFFER_SRGB);  // gamma correction

        int err = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if( err != GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("Frame buffer is not complete. Error: " + err);
            System.exit(-1);
        }

        glBindFramebuffer( GL_FRAMEBUFFER, 0 );
        return framebuf;
    }

    public static int setupShadowFrameBuffer(int shadowTextureID) {
        int shadowFrameBufferID = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, shadowFrameBufferID);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, shadowTextureID, 0);
        glReadBuffer(GL_NONE);
        glDrawBuffer(GL_NONE);

        int err = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if( err != GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("Frame buffer is not complete. Error: " + err);
            System.exit(-1);
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        return shadowFrameBufferID;
    }
}
