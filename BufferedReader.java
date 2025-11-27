// Nadia Iskandar

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class BufferedReader implements Iterable<Image> {

    private int rows;
    private int cols;
    private int count;
    private int current;

    private BufferedInputStream images;
    private BufferedInputStream labels;
    private boolean tracing = false;


    public BufferedReader(String imageFileName, String labelFileName) throws IOException {
        this.images = new BufferedInputStream(new FileInputStream(imageFileName));
        this.labels = new BufferedInputStream(new FileInputStream(labelFileName));
        this.readImagesHeader();
        this.readLabelsHeader();
    }

    public int rows() {
        return this.rows;
    }

    public int columns() {
        return this.cols;
    }

    public int count() {
        return this.count;
    }

    public int current() {
        return this.current;
    }

    public int remain() {
        return this.count - this.current;
    }

    public void trace() {
        this.tracing = true;
    }

    public void trace(boolean tracing) {
        this.tracing = tracing;
    }


    private int readInt(BufferedInputStream input) throws IOException {
        int result = 0;

        for (int i = 0; i < 4; i++) {
            int value = input.read();
            result = 256 * result + value;
        }

        return result;
    }


    private void readImagesHeader() throws IOException {
        int magic = this.readInt(this.images);
        this.count = this.readInt(this.images);
        this.rows = this.readInt(this.images);
        this.cols = this.readInt(this.images);

        if (magic != 2051) {
            throw new BufferedReader.FileFormatException("Bad magic (images): " + magic);
        } else if (this.count <= 0) {
            throw new BufferedReader.FileFormatException("Invalid image count: " + this.count);
        } else if (this.rows <= 0) {
            throw new BufferedReader.FileFormatException("Invalid row size: " + this.rows);
        } else if (this.cols <= 0) {
            throw new BufferedReader.FileFormatException("Invalid col size: " + this.cols);
        }
    }


    private void readLabelsHeader() throws IOException {
        int magic = this.readInt(this.labels);
        int count = this.readInt(this.labels);
        if (magic != 2049) {
            throw new BufferedReader.FileFormatException("Bad magic (labels): " + magic);
        } else if (count < this.count) {
            throw new BufferedReader.FileFormatException("Invalid label count: " + count);
        }
    }


    private byte[][] readPixels() throws IOException {
        byte[][] pixels = new byte[this.rows][this.cols];

        for (int rows = 0; rows < this.rows; rows++) {
            for (int cols = 0; cols < this.cols; cols++) {
                int pixel = this.images.read();
                if (pixel < 0 || pixel > 255) {
                    throw new BufferedReader.FileFormatException("Invalid pixel: " + pixel);
                }
                pixels[rows][cols] = (byte) pixel;
            }
        }

        return pixels;
    }


    private int readLabel() throws IOException {
        int digit = this.labels.read();
        if (digit < 0 || digit > 9) {
            throw new BufferedReader.FileFormatException("Invalid label: " + digit);
        } else {
            return digit;
        }
    }


    public Image readImage() throws IOException {
        this.current++;
        int digit = this.readLabel();
        byte[][] pixels = this.readPixels();
        return new Image(pixels, digit);
    }


    public Image[] read(int count) throws IOException {
        count = Math.min(count, this.remain());
        Image[] images = new Image[count];

        for (int i = 0; i < count; i++) {
            if (this.tracing && i % 100 == 0) {
                int index = this.current + 1;
                System.out.println("Reading image: " + index);
            }
            images[i] = this.readImage();
        }

        return images;
    }

    public Image[] read() throws IOException {
        return read(this.remain());
    }


    @Override
    public Iterator<Image> iterator() {
        return new ImageIterator(this);
    }

    private static class ImageIterator implements Iterator<Image> {

        private BufferedReader reader;
        private int current;

        public ImageIterator(BufferedReader reader) {
            this.reader = reader;
            this.current = 0;
        }

        @Override
        public boolean hasNext() {
            return this.current < this.reader.count();
        }

        @Override
        public Image next() {
            this.current++;
            try {
                return this.reader.readImage();
            } catch (IOException e) {
                this.current = this.reader.count();
                return null;
            }
        }
    }


    public static class FileFormatException extends IOException {
        public FileFormatException(String message) {
            super(message);
        }
    }
}
