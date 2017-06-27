package com.alesp.feedbackapp.MetaWear;

import com.alesp.feedbackapp.MetaWear.stickers.MyMovement;
import com.alesp.feedbackapp.MetaWear.stickers.beanInfoSticker.BeanInfoSticker;
import com.alesp.feedbackapp.MetaWear.stickers.beanInfoSticker.Category;
import com.alesp.feedbackapp.MetaWear.stickers.beanInfoSticker.SingletonInfoSticker;

import java.util.ArrayList;

import weka.classifiers.Classifier;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.RandomForest;
import weka.core.*;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;


/**
 * Created by Stefano on 10/12/2015.
 */

public class Weka {
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

    public static String classify(Classifier classifier, Instances test){
        String prediction = null;
        try{
            if (test.classIndex() == -1)
                test.setClassIndex(test.numAttributes() - 1);
            double pred = classifier.classifyInstance(test.instance(0));
            prediction = test.classAttribute().value((int) pred);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prediction;
    }

    public static String testWeka(Classifier classifier, Instances test, Instances training){
        String prediction = null;
        try{
            if (test.classIndex() == -1)
                test.setClassIndex(test.numAttributes() - 1);
            RandomForest rf;
            AdaBoostM1 ab;
            if(classifier instanceof RandomForest) {
                rf = (RandomForest) classifier;
                double pred = rf.classifyInstance(test.instance(0));
                prediction = training.classAttribute().value((int) pred);
            } else if(classifier instanceof AdaBoostM1) {
                ab = (AdaBoostM1) classifier;
                prediction = training.classAttribute().value((int) ab.classifyInstance(training.instance(0)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prediction;
    }

    public static Instances getTest(MyMovement movement, String object){
        ArrayList<Attribute> atts;
        ArrayList<String> attIdentifier;
        ArrayList<String> attTag;
        Instances       data;
        double[]        vals;

        // 1. set up attributes
        atts = new ArrayList<Attribute>();

        // - nominal
        attIdentifier = new ArrayList<String>();
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
        atts.add(new Attribute("xzero"));
        atts.add(new Attribute("yzero"));
        atts.add(new Attribute("zzero"));
        atts.add(new Attribute("xenergy"));
        atts.add(new Attribute("yenergy"));
        atts.add(new Attribute("zenergy"));
        atts.add(new Attribute("xrootmeansquare"));
        atts.add(new Attribute("yrootmeansquare"));
        atts.add(new Attribute("zrootmeansquare"));
        atts.add(new Attribute("xkurtosis"));
        atts.add(new Attribute("ykurtosis"));
        atts.add(new Attribute("zkurtosis"));
        atts.add(new Attribute("xquartileinferiore"));
        atts.add(new Attribute("yquartileinferiore"));
        atts.add(new Attribute("zquartileinferiore"));
        atts.add(new Attribute("xquartilesuperiore"));
        atts.add(new Attribute("yquartilesuperiore"));
        atts.add(new Attribute("zquartilesuperiore"));
        attTag = new ArrayList<>();

        /***********************************************************************************
         * this part of code parse manipulation received from server and add to test instance.
         * ***********************************************************************************/
        BeanInfoSticker infoSticker = SingletonInfoSticker.getInstance().getInfoSticker();
        String[] labels;
        for(Category category : infoSticker.getCategory()) {
            if(category.getName().equals(object)){
                labels = category.getManipulation().split("_");
                for(int i=0; i<labels.length; i++){
                    attTag.add(labels[i]);
                }
                break;
            }
        }
        /*attTag.add("SpostamentoRilevante");
        attTag.add("Irrilevante");
        attTag.add("Bere/Versare");*/
        atts.add(new Attribute("azione", attTag));

        // 2. create Instances object
        data = new Instances("test", atts, 0);

        // 3. fill with data
        // first instance
        vals = new double[data.numAttributes()];
        // - nominal
        double[] values = movement.getValues();
        for(int i=0; i<47; i++){
            vals[i] = values[i];
        }
        vals[47] = attTag.indexOf("SpostamentoRilevante");
        data.add(new DenseInstance(1.0, vals));

        // 4. output data
        return data;
    }


}
