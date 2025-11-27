//nadia iskandar
public class Classifier implements MNIST_GUI.Classifier {
    @Override
    public int classify(Image image) {
        return Main.smallest(image, Main.array, 4);


    }

}
