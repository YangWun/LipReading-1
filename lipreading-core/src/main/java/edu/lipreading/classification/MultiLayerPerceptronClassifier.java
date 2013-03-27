package edu.lipreading.classification;

import edu.lipreading.Constants;
import edu.lipreading.Sample;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffLoader;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class MultiLayerPerceptronClassifier implements Classifier{
    private static final int INSTANCE_SIZE = (Constants.FRAMES_COUNT * Constants.POINT_COUNT * 2) + Constants.SAMPLE_ROW_SHIFT;
    private List<String> vocabulary = Constants.VOCABULARY;
    private MultilayerPerceptron classifier;
    private List<Sample> samples;

    public MultiLayerPerceptronClassifier(InputStream modelFileInputStream) throws Exception {
        Object read = SerializationHelper.read(modelFileInputStream);
        this.classifier  = (MultilayerPerceptron) read;
    }

    @Override
    public String test(Sample test) {
        double ans = -1;
        try {
            Instance sampleToInstance = sampleToInstance(test);
            ans = classifier.classifyInstance(sampleToInstance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return vocabulary.get((int)ans);
    }

    private Instance sampleToInstance(Sample sample) {
        Instance instance = new DenseInstance(INSTANCE_SIZE);
        instance.setMissing(0);
        instance.setValue(1, sample.getOriginalMatrixSize());
        instance.setValue(2, sample.getWidth());
        instance.setValue(3, sample.getHeight());
        for (int i = 0; i < INSTANCE_SIZE - Constants.SAMPLE_ROW_SHIFT; i++) {
            instance.setValue(i + Constants.SAMPLE_ROW_SHIFT, sample.getMatrix().get(i / 8).get(i % 8));
        }
        return instance;
    }


    public void trainFromFile(String arffFilePath) throws Exception {
        ArffLoader loader = new ArffLoader();
        loader.setSource(new URL(arffFilePath));
        Instances dataSet = loader.getDataSet();
        dataSet.setClassIndex(0);
        this.classifier = new MultilayerPerceptron();
        classifier.buildClassifier(dataSet);
        weka.core.SerializationHelper.write("mp-classifier.model", classifier);
    }

    public List<String> getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(List<String> vocabulary) {
        this.vocabulary = vocabulary;
    }

    @Override
    public void update(Sample train) {
        this.samples.add(train);
        this.train(samples);
    }

    @Override
    public void train(List<Sample> trainingSet) {
        this.samples = trainingSet;
        Instances instances = new Instances("dataset", null, trainingSet.size());
        for (Sample sample : trainingSet) {
            instances.add(sampleToInstance(sample));
        }
        try {
            MultilayerPerceptron multilayerPerceptron = new MultilayerPerceptron();
            multilayerPerceptron.buildClassifier(instances);
            this.classifier = multilayerPerceptron;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
