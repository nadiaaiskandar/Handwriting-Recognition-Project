// nadia iskandar

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

public class MNIST_GUI {

    // A GUI that allows users to draw images of digits in a graphics panel
    // using the mouse.  The user can then request that the image be classified
    // as a digit (0 - 9) using a MNIST-based classifier.  The result will be
    // displayed as a label below the grapics panel.

    private final int rows;     // Number of rows in an MNIST image
    private final int columns;  // Number of cols in an MNIST image
    private final int scale;    // Scaling factor for drawing and image on screen
    private final Classifier classifier; // The KNN based classification algorithm

    private JFrame frame;
    private Label digitLabel;
    private DrawingPanel drawingPanel;

    public static interface Classifier {
        // An interface to the user's digit classification algorithm.
        // Returns the value (0-9) of the classification but may return
        // a negative value if it was unable to classify the image as a digit.
        public int classify(Image image);
    }

    private static abstract class Button extends JButton implements ActionListener {
        public Button(String label) {
            super(label);
            this.addActionListener(this);
        }
    }

    private static abstract class Label extends JLabel {
        public Label(String label, int size) {
            super(label);
            this.setFont(new Font(this.getFont().getName(), Font.BOLD, size));
        }

        public abstract void set(int digit);

        public abstract void clear();
    }


    public MNIST_GUI(String title, Classifier classifier) {
        // Suggested default values (see below)
        this(title, 28, 28, 20, 35, classifier);
    }

    public MNIST_GUI(String title, int scale, int penSize, Classifier classifier) {
        // For us, and MNIST image is always 28 x 28 pixels
        this(title, 28, 28, scale, penSize, classifier);
    }

    public MNIST_GUI(String title, int rows, int columns, int scale, int penSize, Classifier classifier) {

        // Constructor Parameters:
        //
        //    Title       Title to be displayed in the window header
        //    Rows        Number of rows in an MNIST image (28 for us)
        //    Columns     Number of columns in an MNIST image (28 for us)
        //    Scale       Factor by which the an MNIST image should be scaled up in the drawing area
        //    PenSize     The width of the pen stroke (in pixels)
        //    Classifier  Provides the method to be called to classify an image

        this.classifier = classifier;
        this.rows = rows;
        this.columns = columns;
        this.scale = scale;

        this.frame = new JFrame(title);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.digitLabel = new Label(" ", 32) {
            public void set(int digit) {
                this.setText(digit < 0 ? "?" : Integer.toString(digit));
            }

            public void clear() {
                this.setText(" ");
            }
        };

        JButton clearButton = new Button("Clear") {
            @Override
            public void actionPerformed(ActionEvent event) {
                drawingPanel.clear();
                digitLabel.clear();
            }
        };

        JButton classifyButton = new Button("Classify") {
            @Override
            public void actionPerformed(ActionEvent event) {
                Image image = drawingPanel.getImage();
                digitLabel.set(classifier.classify(image));
            }
        };

        // The window consists of three panels:
        //
        //   Top      The drawing area in which the user draws a digit to be classified
        //   Middle   The label which displays the result of classifying this digit image
        //   Bottom   Buttons to clear the image and to classify the image.

        this.drawingPanel = new DrawingPanel(scale * rows, scale * columns, penSize);

        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
        labelPanel.add(digitLabel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(clearButton);
        buttonPanel.add(classifyButton);

        // Bundle these three panels into one content panel.
        // And the establish the frame/window.

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.add(drawingPanel);
        contentPanel.add(labelPanel);
        contentPanel.add(buttonPanel);

        Container content = frame.getContentPane();
        content.add(contentPanel);
        frame.pack();
        frame.setVisible(true);

        this.drawingPanel.clear();
    }


    private class DrawingPanel extends JPanel implements MouseListener, MouseMotionListener {

        // A panel on which a user can draw using the mouse.

        private int beginX = -1;     // Keep track of the last X-coord when mouse was pressed
        private int beginY = -1;     // Keep track of the last Y-coord when mouse was pressed
        private final Stroke stroke; // The shape and characteristics of the pen.
        private final BufferedImage image; // The image backing store

        public DrawingPanel(int height, int width, int penSize) {
            super();
            this.addMouseListener(this);
            this.addMouseMotionListener(this);

            this.setPreferredSize(new Dimension(width, height));
            this.setBackground(Color.WHITE);

            this.stroke = new BasicStroke(penSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            this.image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        }

        @Override
        public void paint(Graphics g) {
            int width = this.getWidth();
            int height = this.getHeight();
            g.drawImage(this.image, 0, 0, width, height, null);
        }


        public void clear() {
            // Clears the drawing panel (WHITE)
            int height = this.image.getHeight();
            int width = this.image.getWidth();

            Graphics g = this.image.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
            g.dispose();

            this.repaint();
        }

        public BufferedImage getBufferedImage() {
            return this.image;
        }

        @Override
        public void mousePressed(MouseEvent event) {
            // Convert screen coordinates to buffer coordinates
            this.beginX = this.image.getWidth() * event.getX() / this.getWidth();
            this.beginY = this.image.getHeight() * event.getY() / this.getHeight();
        }

        @Override
        public void mouseReleased(MouseEvent event) {
            // We finished the current line ... clear the starting point
            // and wait for a new mouse press to begin the next line segment
            this.beginX = -1;
            this.beginY = -1;
        }

        @Override
        public void mouseEntered(MouseEvent event) {
        }

        @Override
        public void mouseExited(MouseEvent event) {
        }

        @Override
        public void mouseClicked(MouseEvent event) {
        }

        @Override
        public void mouseDragged(MouseEvent event) {
            // Convert screen coordinates to buffer coordinates
            int endX = this.image.getWidth() * event.getX() / this.getWidth();
            int endY = this.image.getHeight() * event.getY() / this.getHeight();

            // Draw a line from the previous mouse position to the current mouse position.
            // There may not be a previous position ... in which case we just record the start of the line.

            if (this.beginX >= 0 && this.beginY >= 0) {
                Graphics2D g = (Graphics2D) this.image.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                g.setBackground(Color.WHITE);
                g.setColor(Color.BLACK);
                g.setStroke(stroke);
                g.drawLine(beginX, beginY, endX, endY);
                g.dispose();

                this.repaint();
            }

            this.beginX = endX;
            this.beginY = endY;
        }

        @Override
        public void mouseMoved(MouseEvent event) {
        }


        public Image getImage() {
            // Turn the drawing into and MNIST-style image.
            // We really should crop to a bounding box for the image
            // but this only matters if the user draws a small image
            MNIST_GUI gui = MNIST_GUI.this;
            Image result = new Image(gui.rows, gui.columns, Image.UNKNOWN);
            BufferedImage scaled = scale(this.image, gui.rows, gui.columns);

            for (int row = 0; row < gui.rows; row++) {
                for (int col = 0; col < gui.columns; col++) {
                    int gray = getGray(scaled, row, col);
                    result.set(row, col, gray);
                }
            }

            // Maximize the contrast (utilize the full range of gray intensities).
            // and the recenter the image so that its center of mass is at the center.
            return center(brighten(result));
        }
    }


    private static int RGBtoGray(int rgb) {
        // MNIST gray scale is inverted wrt to the GUI gray scale
        // MNIST: White = 0, Black = 255; GUI: White = 255, Black = 0
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        int gray = (red + green + blue) / 3;
        return 255 - gray;
    }

    private static int getGray(BufferedImage image, int row, int column) {
        int rgb = image.getRGB(column, row);
        return RGBtoGray(image.getRGB(column, row));
    }


    private static BufferedImage scale(BufferedImage image, int height, int width) {
        // Resize (downsize) the image buffer to new dimensions (usually to 28 x 28).
        final int scaling = java.awt.Image.SCALE_SMOOTH;
        BufferedImage scaled = new BufferedImage(width, height, image.getType());
        Graphics2D g = scaled.createGraphics();
        g.drawImage(image.getScaledInstance(width, height, scaling), 0, 0, null);
        g.dispose();
        return scaled;
    }


    private static Image brighten(Image image) {
        // Restore full dynamic constrast range to the image
        // It is reduced by the scaling/resizing operation
        int rows = image.rows();
        int columns = image.columns();

        int max = 0;
        int min = 255;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int pixel = image.get(row, col);
                if (pixel > max) max = pixel;
                if (pixel < min) min = pixel;
            }
        }

        if (max == min) return image;

        Image result = new Image(rows, columns, image.digit());
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int pixel = image.get(row, col);
                pixel = min + 255 * (pixel - min) / (max - min);
                result.set(row, col, pixel);
            }
        }

        return result;
    }


    private static int get(Image image, int row, int col) {
        if (row < 0 || row >= image.rows() || col < 0 || col >= image.columns()) {
            return 0; // WHITE (MNIST)
        } else {
            return image.get(row, col);
        }
    }

    private static Image center(Image image) {
        // Recenter the image so that its center of mass is in the middle of the image
        int rows = image.rows();
        int columns = image.columns();

        int sum = 0;
        int rowSum = 0;
        int colSum = 0;

        // Compute the center of mass.

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int pixel = image.get(row, col);
                rowSum += row * pixel;
                colSum += col * pixel;
                sum += pixel;
            }
        }

        if (sum == 0) return image;

        int centerRow = rows / 2;
        int centerCol = columns / 2;

        int centerOfMassRow = rowSum / sum;
        int centerOfMassCol = colSum / sum;

        int rowShift = centerRow - centerOfMassRow;
        int colShift = centerCol - centerOfMassCol;

        // Shift the image so that the center of mass is now
        // at the center pixel of the image.

        Image result = new Image(rows, columns, image.digit());
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int pixel = get(image, row - rowShift, col - colShift);
                result.set(row, col, pixel);
            }
        }

        return result;
    }
}
