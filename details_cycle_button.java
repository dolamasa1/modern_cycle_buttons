package assets.buttons;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.beans.JavaBean;

@JavaBean(description = "Custom circular draggable button")
public class CycleButton_details extends JButton implements DragGestureListener, DragSourceListener {
    private static final Color BACKGROUND_COLOR = new Color(30, 30, 30);
    private static final Color HOVER_COLOR = new Color(50, 50, 50);
    private static final Color BORDER_COLOR = new Color(42, 42, 42);
    private static final Color HOVER_BORDER_COLOR = new Color(76, 76, 76);
    private static final Color ICON_COLOR = new Color(76, 175, 80);
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 60);

    private boolean isHovered = false;
    private boolean isPressed = false;
    private boolean isReleasing = false;
    private float releaseProgress = 0f;
    private float animationProgress = 0f;
    private float targetProgress = 0f;
    private int buttonSize = 40;
    private Timer animationTimer;
    private static CycleButton_details draggedButton;

    public CycleButton_details() {
        this(40);
    }

    public CycleButton_details(int size) {
        this.buttonSize = size;
        initializeComponent();
        setupDragAndDrop();
        setupAnimations();
        setupMouseListeners();
    }

    private void initializeComponent() {
        setPreferredSize(new Dimension(buttonSize, buttonSize));
        setMinimumSize(new Dimension(buttonSize, buttonSize));
        setMaximumSize(new Dimension(buttonSize, buttonSize));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setToolTipText("Details");
    }

    private void setupDragAndDrop() {
        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
            this, DnDConstants.ACTION_MOVE, this);
    }

    private void setupAnimations() {
        animationTimer = new Timer(16, e -> updateAnimations());
    }

    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { handleMouseEnter(); }
            public void mouseExited(MouseEvent e) { handleMouseExit(); }
            public void mousePressed(MouseEvent e) { handleMousePress(); }
            public void mouseReleased(MouseEvent e) { handleMouseRelease(); }
        });
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        configureRenderingHints(g2);
        drawButtonComponents(g2);
        g2.dispose();
    }

    private void configureRenderingHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    private void drawButtonComponents(Graphics2D g2) {
        drawShadow(g2);
        drawMainButton(g2);
        drawBorder(g2);
        drawIcon(g2);
    }

    private void drawShadow(Graphics2D g2) {
        g2.setColor(SHADOW_COLOR);
        g2.fill(new Ellipse2D.Double(0, calculateShadowY(), buttonSize, buttonSize));
    }

    private void drawMainButton(Graphics2D g2) {
        g2.translate(0, calculateTranslateY());
        g2.setColor(calculateBackgroundColor());
        g2.fillOval(0, 0, buttonSize, buttonSize);
    }

    private void drawBorder(Graphics2D g2) {
        g2.setColor(calculateBorderColor());
        g2.setStroke(new BasicStroke(1));
        g2.drawOval(0, 0, buttonSize - 1, buttonSize - 1);
    }

    private void drawIcon(Graphics2D g2) {
        g2.translate(buttonSize/4, buttonSize/4);
        g2.scale(buttonSize/48.0, buttonSize/48.0);
        g2.setColor(ICON_COLOR);
        g2.fill(createIconPath());
    }

private Path2D createIconPath() {
    Path2D path = new Path2D.Double();
    // SVG path converted to Java2D commands
    path.moveTo(12, 2);
    path.curveTo(6.48, 2, 2, 6.48, 2, 12);
    path.curveTo(2, 17.52, 6.48, 22, 12, 22);
    path.curveTo(17.52, 22, 22, 17.52, 22, 12);
    path.curveTo(22, 6.48, 17.52, 2, 12, 2);
    path.closePath();
    path.moveTo(11, 17);
    path.lineTo(13, 17);
    path.lineTo(13, 19);
    path.lineTo(11, 19);
    path.closePath();
    path.moveTo(11, 9);
    path.lineTo(13, 9);
    path.lineTo(13, 15);
    path.lineTo(11, 15);
    path.closePath();
    return path;
}

    public int getButtonSize() { return buttonSize; }
    public void setButtonSize(int size) { 
        buttonSize = size; 
        initializeComponent();
        repaint();
    }

    public boolean contains(int x, int y) {
        return new Ellipse2D.Double(0, 0, buttonSize, buttonSize).contains(x, y);
    }

    public void dragGestureRecognized(DragGestureEvent dge) {
        draggedButton = this;
        dge.startDrag(null, new StringSelection("CycleButton"), this);
    }

    public void dragDropEnd(DragSourceDropEvent dsde) { draggedButton = null; }
    public void dragEnter(DragSourceDragEvent dsde) {}
    public void dragOver(DragSourceDragEvent dsde) {}
    public void dropActionChanged(DragSourceDragEvent dsde) {}
    public void dragExit(DragSourceEvent dse) {}

    private void updateAnimations() {
        if (isReleasing) updateReleaseAnimation();
        updateMainAnimation();
        repaint();
    }

    private void updateReleaseAnimation() {
        releaseProgress += 0.1f;
        if (releaseProgress >= 1f) {
            releaseProgress = 1f;
            isReleasing = false;
        }
    }

    private void updateMainAnimation() {
        float delta = targetProgress - animationProgress;
        if (Math.abs(delta) > 0.01f) {
            animationProgress += delta * 0.2f;
        } else {
            animationProgress = targetProgress;
            if (!isReleasing) animationTimer.stop();
        }
    }

    private void handleMouseEnter() {
        isHovered = true;
        targetProgress = 1f;
        startAnimation();
    }

    private void handleMouseExit() {
        isHovered = false;
        targetProgress = 0f;
        startAnimation();
    }

    private void handleMousePress() {
        isPressed = true;
        repaint();
    }

    private void handleMouseRelease() {
        if (isPressed) {
            isPressed = false;
            isReleasing = true;
            releaseProgress = 0f;
            startAnimation();
        }
        repaint();
    }

    private void startAnimation() {
        if (!animationTimer.isRunning()) animationTimer.start();
    }

    private Color calculateBackgroundColor() {
        return interpolateColor(BACKGROUND_COLOR, HOVER_COLOR, animationProgress);
    }

    private Color calculateBorderColor() {
        return interpolateColor(BORDER_COLOR, HOVER_BORDER_COLOR, animationProgress);
    }

    private int calculateTranslateY() {
        if (isPressed) return 1;
        if (isReleasing) return Math.round(1 * (1 - releaseProgress) + (-2 * animationProgress) * releaseProgress);
        return Math.round(-2 * animationProgress);
    }

    private int calculateShadowY() {
        if (isPressed) return 2;
        if (isReleasing) return Math.round(2 * (1 - releaseProgress) + (3 + 2 * animationProgress) * releaseProgress);
        return 3 + Math.round(2 * animationProgress);
    }

    private Color interpolateColor(Color c1, Color c2, float ratio) {
        return new Color(
            Math.round(c1.getRed() * (1 - ratio) + c2.getRed() * ratio),
            Math.round(c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio),
            Math.round(c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio)
        );
    }

public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        try { UIManager.setLookAndFeel(new FlatDarkLaf()); } 
        catch (Exception e) { e.printStackTrace(); }
        
        JFrame frame = new JFrame("CycleButton Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        
        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(18, 18, 18));
        panel.setDropTarget(createDropTarget());
        
        // Original button
        CycleButton_details detailsButton = new CycleButton_details(40);
        detailsButton.setBounds(50, 50, 40, 40);
        panel.add(detailsButton);
        
        // Pedo button
        CycleButton_pedo pedoButton = new CycleButton_pedo(40);
        pedoButton.setBounds(100, 50, 40, 40);
        panel.add(pedoButton);
        
        // Document button
        CycleButton_document documentButton = new CycleButton_document(40);
        documentButton.setBounds(150, 50, 40, 40);
        panel.add(documentButton);
        
        frame.add(panel);
        frame.setVisible(true);
    });
}

    private static DropTarget createDropTarget() {
        return new DropTarget(null, DnDConstants.ACTION_MOVE, new DropTargetListener() {
            public void dragEnter(DropTargetDragEvent dtde) { handleDragEnter(dtde); }
            public void dragOver(DropTargetDragEvent dtde) { dtde.acceptDrag(DnDConstants.ACTION_MOVE); }
            public void drop(DropTargetDropEvent dtde) { handleDrop(dtde); }
            public void dropActionChanged(DropTargetDragEvent dtde) {}
            public void dragExit(DropTargetEvent dte) {}
        });
    }

    private static void handleDragEnter(DropTargetDragEvent dtde) {
        if (dtde.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_MOVE);
        }
    }

    private static void handleDrop(DropTargetDropEvent dtde) {
        try {
            dtde.acceptDrop(DnDConstants.ACTION_MOVE);
            Point dropPoint = dtde.getLocation();
            draggedButton.setLocation(
                dropPoint.x - draggedButton.buttonSize/2,
                dropPoint.y - draggedButton.buttonSize/2
            );
            draggedButton.getParent().repaint();
            dtde.dropComplete(true);
        } catch (Exception e) {
            dtde.rejectDrop();
        }
    }
}
