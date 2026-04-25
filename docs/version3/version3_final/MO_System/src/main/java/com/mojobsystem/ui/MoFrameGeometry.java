package com.mojobsystem.ui;

import javax.swing.JFrame;
import java.awt.Rectangle;

/**
 * Keeps window size and screen position stable when replacing one MO screen with another.
 */
public final class MoFrameGeometry {
    public static final int FRAME_W = 1080;
    public static final int FRAME_H = 760;

    private static Rectangle lastBounds;

    private MoFrameGeometry() {
    }

    /**
     * Call from every top-level MO {@link JFrame} constructor (after {@code setDefaultCloseOperation}).
     * First window: centred default size; later: same bounds as the last remembered frame.
     */
    public static void apply(JFrame frame) {
        if (lastBounds != null && lastBounds.width >= 200 && lastBounds.height >= 200) {
            frame.setBounds(lastBounds);
        } else {
            frame.setSize(FRAME_W, FRAME_H);
            frame.setLocationRelativeTo(null);
        }
    }

    /**
     * Call at the end of constructors that use {@link #apply(JFrame)} after all children are added.
     * Re-applies {@link #lastBounds} so layout / LAF does not leave the window smaller than the
     * previous screen after navigation.
     */
    public static void finishTopLevelFrame(JFrame frame) {
        if (frame == null) {
            return;
        }
        if (lastBounds != null && lastBounds.width >= 200 && lastBounds.height >= 200) {
            frame.setBounds(lastBounds);
        }
    }


}
