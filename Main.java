// Nadia Iskandar
public class Main {
    public static Image[] array;
    public static int distance(Image image1, Image image2) {
        int sum = 0;
        int difference;

        for (int i = 0; i < image1.rows(); i++) {
            for (int j = 0; j < image1.columns(); j++) {
                difference = image1.get(i, j) - image2.get(i, j);
                sum += difference * difference;

            }

        }

        return sum;


    }

    public static Image smallOneMin(Image originalImage, Image[] image) {
        int distance;
        int minDistance = Integer.MAX_VALUE;
        int minDistanceSlot = 0;

        for (int i = 0; i < image.length; i++) {
            distance = distance(originalImage, image[i]);

            if (distance < minDistance) {
                minDistance = distance;
                minDistanceSlot = i;

            }

        }

        return image[minDistanceSlot];


    }


    public static int smallestA(Image originalImage, Image[] image) {
        int smallIndex = Integer.MAX_VALUE;
        int smallerIndex = Integer.MAX_VALUE;
        int smallestIndex = Integer.MAX_VALUE;

        Image[] smallArray = new Image[3];

        int distance;

        // k, go through them, majority
        // inversely

        for (int i = 0; i < image.length; i++) {
            distance = distance(originalImage, image[i]);
            if (distance < smallIndex) {
                if (distance < smallerIndex) {
                    if (distance < smallestIndex) {

                        smallArray[2] = smallArray[1];
                        smallArray[1] = smallArray[0];
                        smallArray[0] = image[i];

                        smallestIndex = distance;

                    }

                    smallArray[2] = smallArray[1];
                    smallArray[1] = image[i];
                    smallerIndex = distance;

                }
                smallArray[2] = image[i];
                smallIndex = distance;


            }
        }

        if (smallArray[0].digit() == smallArray[1].digit() || smallArray[0].digit() == smallArray[2].digit()) {
            return smallArray[0].digit();

        } else if (smallArray[1] == smallArray[2]) {
            return smallArray[1].digit();

        } else if (smallArray[0].digit() != smallArray[1].digit() && smallArray[0].digit() != smallArray[2].digit()) {
            return -1;
        }

        return -1;

    }


    public static int smallest(Image originalImage, Image[] trainImages, int size) {
        int distance;
        Image[] imageArray = new Image[size];
        int[] distances = new int[size];

        for (int i = 0; i < size; i++) {
            distances[i] = Integer.MAX_VALUE;
        }

        for (Image trainImage : trainImages) {
            distance = distance(originalImage, trainImage);

            for (int j = 0; j < size; j++) {
                if (distance < distances[j]) {
                    // shift from j down to size and replace j with image
                    // array also shift in the same for loop index is the same
                    for (int k = size - 1; k > j; k--) {
                        distances[k] = distances[k - 1];
                        imageArray[k] = imageArray[k - 1];

                    }
                    // replace image in slot
                    distances[j] = distance;
                    imageArray[j] = trainImage;
                    break;

                }

            }

        }


        // array that stores all the votes
        double[] numbers = new double[10];
        double sum = 0;

        for (int j = 0; j < size; j++) {
            sum += distances[j];
        }

        for (int k = 0; k < size; k++) {
            // Weighted votes
            numbers[imageArray[k].digit()] += sum / distances[k]; 
        }

        double max = Integer.MIN_VALUE;
        int index = -1;

        for (int l = 0; l < numbers.length; l++) {
            if (numbers[l] > max) {
                max = numbers[l];
                index = l;

            }



        }


        return index;
    }


    public static void main(String[] args) {

        try {

            BufferedReader trainImages = new BufferedReader("train-images.idx3-ubyte",
                    "train-labels.idx1-ubyte");

            BufferedReader testImages = new BufferedReader("t10k-images.idx3-ubyte",
                    "t10k-labels.idx1-ubyte");

            array = trainImages.read();
            Image[] accuracyArray = testImages.read();

            Classifier c = new Classifier();
            MNIST_GUI gui = new MNIST_GUI("Iskandar", c);

            double errors = 0;
/*
            Code to test againts the dataset
            for (int i = 0; i < accuracyArray.length; i++) {

                if (i % 10 == 0) {
                    System.out.println(i + " " + errors + " errors");
                }

                if (accuracyArray[i].digit() != c.classify(accuracyArray[i])) {
                    errors++;

                }

            } */
            // allowing user to try out 

            double accuracyRate = 100 - ((errors / 10000) * 100);

            System.out.println("Accuracy Rate: " + accuracyRate + " Number of errors: " + errors);

        } catch (Exception e) {
            System.out.println(e);

        }
    }


}

/*
 * Notes:
 * Error rate with min: 96.91
 *
 *Error rate : 97.15
 *
 * */
