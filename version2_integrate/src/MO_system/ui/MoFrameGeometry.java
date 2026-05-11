package com.mojobsystem.ui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.Rectangle;

/**
 * Keeps window size and screen position stable when replacing one MO screen with another.
 */
public final class MoFrameGeometry {
    /** Default size — same as Authentication / profile / TA Job / Admin portals. */
    public static final int FRAME_W = 1080;
    public static final int FRAME_H = 760;
    public static final int FRAME_MIN_W = 980;
    public static final int FRAME_MIN_H = 680;

    private static Rectangle lastBounds;

    private MoFrameGeometry() {
    }

    /**
     * Call from every top-level MO {@link JFrame} constructor (after {@code setDefaultCloseOperation}).
     * First window: centred default size; later: same bounds as the last remembered frame.
     */
    public static void apply(JFrame frame) {
        frame.setMinimumSize(new Dimension(FRAME_MIN_W, FRAME_MIN_H));
        if (lastBounds != null && lastBounds.width >= 200 && lastBounds.height >= 200) {
            frame.setBounds(lastBounds);
        } else {
            frame.setSize(FRAME_W, FRAME_H);
            frame.setLocationRelativeTo(null);
        }
    }

    /**
     * Child window that stacks over a parent (e.g. create job) should match the parent rectangle.
     */
    public static void applyMatching(JFrame parent, JFrame child) {
        child.setMinimumSize(new Dimension(FRAME_MIN_W, FRAME_MIN_H));
        if (parent != null && parent.isDisplayable()) {
            child.setBounds(parent.getBounds());
        } else {
            apply(child);
        }
    }

    public static void rememberFrom(JFrame frame) {
        if (frame != null) {
            lastBounds = frame.getBounds();
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
        frame.setMinimumSize(new Dimension(FRAME_MIN_W, FRAME_MIN_H));
        if (lastBounds != null && lastBounds.width >= 200 && lastBounds.height >= 200) {
            frame.setBounds(lastBounds);
        }
    }


    /**
     * Replace the current window with another while preserving bounds.
     * <ol>
     *   <li>Remembers {@code from}'s bounds for the next {@link #apply(JFrame)}.</li>
     *   <li>Builds and shows the next frame first (same EDT), so there is no gap with no window.</li>
     *   <li>Disposes {@code from} on a <em>later</em> EDT tick — avoids disposing the window that is
     *       still handling the navigation action (which can cause flicker or unstable behaviour on
     *       some platforms).</li>
     * </ol>
     * If {@code openNext} throws, {@code from} is left open.
     */
    public static void navigateReplace(JFrame from, Runnable openNext) {
        rememberFrom(from);
        openNext.run();
        SwingUtilities.invokeLater(() -> {
            if (from != null && from.isDisplayable()) {
                from.dispose();
            }
        });
    }
}
