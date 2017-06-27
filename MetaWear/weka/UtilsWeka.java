package com.alesp.feedbackapp;
import com.example.stefano.myapplication.stickers.MyMovement;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import weka.classifiers.trees.RandomForest;
import weka.core.*;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;

/**
 * Created by Stefano on 10/12/2015.
 */

public class UtilsWeka {

    public static String testWeka(RandomForest randomForest, AttributeSelection filter, Instances test){
        try{
            if (test.classIndex() == -1)
                test.setClassIndex(test.numAttributes() - 1);
            Instances testFiltered = Filter.useFilter(test, filter);

            return testFiltered.classAttribute().value((int) randomForest.classifyInstance(testFiltered.instance(0)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String doWeka(String trainingSet, Instances test){
        try {
            DataSource sourceTrain = new DataSource(trainingSet);
            Instances data = sourceTrain.getDataSet(); // in data c'è tutto il dataset con tutte le features
            if (data.classIndex() == -1)
                data.setClassIndex(data.numAttributes() - 1);

            if (test.classIndex() == -1)
                test.setClassIndex(test.numAttributes() - 1);

            AttributeSelection filter = new AttributeSelection();

            String[] optionsBestFirst = new String[2];
            optionsBestFirst[0] = "-S";
            optionsBestFirst[1] = "weka.attributeSelection.BestFirst -S 8";

            String[] optionsSubSet = new String[2];
            optionsSubSet[0] = "-E";
            optionsSubSet[1] = "weka.attributeSelection.CfsSubsetEval -L";

            filter.setOptions(optionsBestFirst);
            filter.setOptions(optionsSubSet);

            // Applico il filtro al training set
            filter.setInputFormat(data);
            for (int i = 0; i < data.numInstances(); i++)
                filter.input(data.instance(i));
            filter.batchFinished();
            Instances dataFiltered = filter.getOutputFormat();
            Instance processed;
            while ((processed = filter.output()) != null)
                dataFiltered.add(processed); // in dataFiltered c'è tutto il dataset con la features selections

            // Applico il filtro al test set
            Instances testFiltered = Filter.useFilter(test, filter);

            RandomForest randomForest = new RandomForest(); // new instance of tree
            randomForest.buildClassifier(dataFiltered); // build classifier

            return testFiltered.classAttribute().value((int) randomForest.classifyInstance(testFiltered.instance(0)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Instances getTest(MyMovement movement){
        ArrayList<Attribute> atts;
        ArrayList<String> attIdentifier;
        ArrayList<String> attEtichetta;
        Instances       data;
        double[]        vals;

        // 1. set up attributes
        atts = new ArrayList<Attribute>();

        // - nominal
        attIdentifier = new ArrayList<String>();
        attIdentifier.add("e980ee1b26e9a66c");
        atts.add(new Attribute("identifier", attIdentifier));
        atts.add(new Attribute("durata"));
        atts.add(new Attribute("ncampioni"));
        atts.add(new Attribute("xconsecutivi"));
        atts.add(new Attribute("yconsecutivi"));
        atts.add(new Attribute("zconsecutivi"));
        atts.add(new Attribute("xmax"));
        atts.add(new Attribute("ymax"));
        atts.add(new Attribute("zmax"));
        atts.add(new Attribute("xmin"));
        atts.add(new Attribute("ymin"));
        atts.add(new Attribute("zmin"));
        atts.add(new Attribute("xmean"));
        atts.add(new Attribute("ymean"));
        atts.add(new Attribute("zmean"));
        atts.add(new Attribute("xstddev"));
        atts.add(new Attribute("ystddev"));
        atts.add(new Attribute("zstddev"));
        atts.add(new Attribute("xdifference"));
        atts.add(new Attribute("ydifference"));
        atts.add(new Attribute("zdifference"));
        atts.add(new Attribute("xvar"));
        atts.add(new Attribute("yvar"));
        atts.add(new Attribute("zvar"));
        atts.add(new Attribute("xycov"));
        atts.add(new Attribute("xzcov"));
        atts.add(new Attribute("yzcov"));
        atts.add(new Attribute("xypearson"));
        atts.add(new Attribute("xzpearson"));
        atts.add(new Attribute("yzpearson"));
        attEtichetta = new ArrayList<String>();
        attEtichetta.add("SpostareOrizzontalmente");
        attEtichetta.add("AlzareVerticalmente");
        atts.add(new Attribute("etichetta", attEtichetta));

        // 2. create Instances object
        data = new Instances("test", atts, 0);

        // 3. fill with data
        // first instance
        vals = new double[data.numAttributes()];
        // - nominal
        vals[0] = attIdentifier.indexOf("e980ee1b26e9a66c");
        double[] values = movement.getValues();
        for(int i=0; i<29; i++){
            vals[i+1] = values[i];
        }
        vals[30] = attEtichetta.indexOf("AlzareVerticalmente");
        data.add(new DenseInstance(1.0, vals));

        // 4. output data
        return data;
    }
}
