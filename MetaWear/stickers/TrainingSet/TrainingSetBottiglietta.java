package com.example.stefano.myapplication.stickers.TrainingSet;

import android.os.Environment;

import java.io.FileNotFoundException;
import java.io.IOException;

import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.supervised.attribute.AttributeSelection;

/**
 * Created by Stefano on 11/12/2015.
 */

public class TrainingSetBottiglietta {
    private static TrainingSetBottiglietta instance;
    private RandomForest algorithmBottle;
    private AttributeSelection filtro;
    private Instances data;

    private TrainingSetBottiglietta(){
        try{
            ConverterUtils.DataSource sourceTrain;
            if(ConverterUtils.DataSource.isArff(Environment.getExternalStorageDirectory()+"/Android/bottle.arff"))
                sourceTrain = new ConverterUtils.DataSource(Environment.getExternalStorageDirectory()+"/Android/bottle.arff");
            else
                sourceTrain = null;
            data = sourceTrain.getDataSet(); // in data c'è tutto il dataset con tutte le features
            if (data.classIndex() == -1)
                data.setClassIndex(data.numAttributes() - 1);

            /*AttributeSelection filter = new AttributeSelection();

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
                dataFiltered.add(processed); // in dataFiltered c'è tutto il dataset con la features selections*/

            RandomForest randomForest = new RandomForest(); // new instance of tree
            randomForest.buildClassifier(data); // build classifier

            algorithmBottle = randomForest;
          //  filtro = filter;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static TrainingSetBottiglietta getInstance(){
        if (instance == null)
            instance = new TrainingSetBottiglietta();
        return instance;
    }

    public RandomForest getAlgorithm(){return algorithmBottle;}

    public AttributeSelection getFilter(){return filtro;}

    public Instances getData(){ return data; }
}
