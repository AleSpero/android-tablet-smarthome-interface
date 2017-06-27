package com.example.stefano.myapplication.stickers.Activity;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.util.Base64;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Nearable;
import com.estimote.sdk.SystemRequirementsChecker;
import com.example.stefano.myapplication.R;
import com.example.stefano.myapplication.Utils;
import com.example.stefano.myapplication.Weka;
import com.example.stefano.myapplication.stickers.ClassifiedManipulation;
import com.example.stefano.myapplication.stickers.Activity.DatasetCollection.MovementList;
import com.example.stefano.myapplication.stickers.Manipulation;
import com.example.stefano.myapplication.stickers.MyMovement;
import com.example.stefano.myapplication.stickers.MyNearable;
import com.example.stefano.myapplication.stickers.Statistics;
import com.example.stefano.myapplication.stickers.beanInfoSticker.BeanInfoSticker;
import com.example.stefano.myapplication.stickers.beanInfoSticker.Category;
import com.example.stefano.myapplication.stickers.beanInfoSticker.SingletonInfoSticker;
import com.example.stefano.myapplication.stickers.beanInfoSticker.SingletonMapModel;
import com.example.stefano.myapplication.stickers.beanInfoSticker.SingletonMapSticker;
import com.example.stefano.myapplication.stickers.beanInfoSticker.Sticker;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

import weka.classifiers.Classifier;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SerializationHelper;

/**
 * Created by Stefano on 11/12/2015.
 * 
 * This activity labels manipulations with training sets in Environment.getExternalStorageDirectory()+"/Android/"
 * 
 */

public class PredictMovement extends AppCompatActivity {

    private BeanInfoSticker infoSticker;

    /************************************************************************************
     * The mapSticker Key represents the ID sticker
     * The mapSticker Value represents an ObjectSticker who is composed by, sticker name and a Sticker object, associated to sticker id
     ***********************************************************************************/
    private Map<String, Sticker> mapSticker = new HashMap<>();

    /********************************************************************************
     * The mapModel Key represents the object TYPE
     * The mapmodel Value represents a ClassifierModel object who is composed by, Model and classifier, associated to object type
     *******************************************************************************/
    private Map<String, Classifier> mapModel = new HashMap<>();
    private Map<String, Boolean> mapNearables = new HashMap<>();

    private Map<String,NearableQueue> stickerQueues = new HashMap<>();

    private TextView view;
    private TextView viewInviati;

    private int numSent = 0;

    private BeaconManager beaconManager;

    private List<Manipulation> mov;

    /*private RandomForest randomForestBottle;
    private RandomForest randomForestMedicine;

    private AdaBoostM1 adaBoostKnife;

    private Instances dataBottle;
    private Instances dataKnife;
    private Instances dataMedicine;

    private Classifier clsBottle;
    private Classifier clsMedicine;
    private Classifier clsKnife;

    private AttributeSelection filter;

    MovementList mvList = new MovementList();*/
    int contatore = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*******************************************************************************************
         * We get all information about stickers and objects used in the smart-home and the relative
         * .model file to allow us to classify the manipulations
         *******************************************************************************************/
        infoSticker = SingletonInfoSticker.getInstance().getInfoSticker();

        NearableQueue queue;
        String stickerId;


        for(Sticker sticker: infoSticker.getSticker()){

            queue = new NearableQueue();
            stickerId = sticker.getId();

            new Thread(new NearableThread(stickerId,queue)).start();

            stickerQueues.put(stickerId, queue);

        }

        setContentView(R.layout.predict_movement);
        view = (TextView) findViewById(R.id.textView);
        viewInviati = (TextView) findViewById(R.id.textInviati);

        beaconManager = new BeaconManager(this);

        mov = new ArrayList<>();
        view.setText("Wait...");

        /*************************************************************
         * Insert in mapSticker a sticker object and relative sticker id
         **************************************************************/
        this.mapSticker = SingletonMapSticker.getInstance().getMapSticker();

        /*******************************************************************************************
         * Decode all model sent from the server and use it to get Classifier for the object type and
         * insert it in mapModel associate it model name as Key in the map.
         *******************************************************************************************/
        this.mapModel = SingletonMapModel.getInstance().getMapModel();


        view.setText("Ok. Let's go!");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SystemRequirementsChecker.checkWithDefaultDialogs(this)) {
            startScanning();
        }
    }

    private void startScanning() {
        beaconManager.setNearableListener(new BeaconManager.NearableListener() {

            /***************************************************************************************
             * Using the Estimote api we get every 150 milliseconds a list of Nearable objects,
             * presents in the system, who contains all nearable information
             **************************************************************************************/
            @Override
            public void onNearablesDiscovered(List<Nearable> list) {
                for (Nearable n : list) {

                    MyNearable nearableWrapper = new MyNearable(n);

                    String nearableIdentifier = nearableWrapper.getIdentifier();

                    if(stickerQueues.containsKey(nearableIdentifier))
                        stickerQueues.get(nearableIdentifier).add(nearableWrapper);


                    /*if (mapSticker.containsKey(n.identifier)) {

                        MyNearable obj = new MyNearable(n);
                        int count = 0;

                        for (Manipulation m : mov) {
                            if (m.getIdentifier().equals(obj.getId())) {
                                count++;
                            }
                        }
                        view.setText(obj.getMotion() + "\n0");

                        if (count == 0) {
                            mov.add(new Manipulation(obj));
                        }

                        if (!mapNearables.containsKey(obj.getId())) { // first n value received
                            mapNearables.put(obj.getId(), obj.getMotion());

                        } else if (mapNearables.get(obj.getId()) != obj.getMotion()) { // Manipulation starts or finishes
                            mapNearables.put(obj.getId(), obj.getMotion());
                            if (obj.getMotion()) { // motion goes from FALSE to TRUE -> manipulation starts
                                for (Manipulation m : mov) {
                                    if (m.getIdentifier().equals(obj.getId())) {
                                        m.addNearable(obj);
                                    }
                                }
                            } else { // motion goes from TRUE to FALSE -> manipulation finishes
                                for (Manipulation m : mov) {
                                    if (m.getIdentifier().equals(obj.getId())) {
                                        // Duration
                                        Long duration = m.getListNearable().get(m.getListNearable().size() - 1).getTime() - m.getListNearable().get(0).getTime(); // ultimo motion false - primo motion true

                                        sendMovement(m, duration, obj.getId()); // sendMoviment invia al server rest il movimento
                                        m.clearListNearable();
                                    }
                                }
                            }
                        } else if (obj.getMotion()) { // il movimento continua e il valore precedente di motion era sempre true
                            for (Manipulation m : mov) {
                                if (m.getIdentifier().equals(obj.getId())) {
                                    view.setText(obj.getMotion() + "\n" + (m.getListNearable().get(m.getListNearable().size() - 1).getTime() - m.getListNearable().get(0).getTime()));
                                    m.addNearable(obj);
                                }
                            }
                        }
                    } // end if (n.identifier.equals......)*/
                } // end for (Nearable n : list)
            } // end onNearableDiscovered
        });

        beaconManager.setForegroundScanPeriod(150, 0);
        beaconManager.setBackgroundScanPeriod(150, 0);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback()

        {
            @Override
            public void onServiceReady() {
                beaconManager.startNearableDiscovery();
            }
        });
    }

    @Override
    protected void onStop() {
        //beaconManager.disconnect();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        beaconManager.disconnect();
        super.onDestroy();
    }

    public void insertMapSticker(List<Sticker> listSticker) {
        for (Sticker sticker : listSticker) {
            mapSticker.put(sticker.getId(), sticker);
        }
    }

    public void insertMapModel(List<Category> categoryList) {
        for (Category category : categoryList) {
            byte[] modelDecoded = Base64.decode(category.getModel(), Base64.DEFAULT);
            mapModel.put(category.getName(), getClassifierFromModel(modelDecoded));
        }
    }

    private void sendMovement(Manipulation m, long durata, String id) {
        Statistics stat = new Statistics(m.getListNearable(), durata);
        MyMovement movement = new MyMovement(stat, id, durata, m.getListNearable().size(), null);

        /*StickersList stList = new StickersList();
        for(MyNearable myNearable : m.getListNearable()){
            myNearable.setId(idSt);

            stList.add(myNearable, contatore);
        }
        idSt++;
        movement.addStickers(stList.getList());*/


        //MovementList ml = SpezzaMovimento(movement);
        movement = Classify(movement);
        String ret = Send(movement);
        view.setText(ret);
        viewInviati.setText("Number of sent manipulations: " + numSent);

        /*mvList.add(movement, contatore);
        button.setVisibility(View.VISIBLE);*/

        contatore++;
    }

    private String Send(MyMovement movement) {
        ClassifiedManipulation classifiedManipulation = new ClassifiedManipulation(movement);
        String r = Utils.sendObject(classifiedManipulation, "sticker/classifiedmanipulation");
        numSent++;
        return r;
    }

    private MyMovement Classify(MyMovement ml) {
        MyMovement res = ml;
        String prediction = "";
      /*  if(ml.getIdentifier().equals(identifierBottle))
            prediction = Weka.testWeka(randomForestBottle, Weka.getTest(res), dataBottle);//Weka.testWeka(algoritmo, filtro, Weka.getTest(res));
        else if(ml.getIdentifier().equals(identifierMedicine))
            prediction = Weka.testWeka(randomForestMedicine, Weka.getTest(res), dataMedicine);//Weka.testWeka(algoritmo, filtro, Weka.getTest(res));
        else if(ml.getIdentifier().equals(identifierKnife))
            prediction = Weka.testWeka(adaBoostKnife, Weka.getTest(res) ,dataKnife);//Weka.testWeka(algoritmo, filtro, Weka.getTest(res));
        */

        // classify from .model
        /*******************************************************************
         * STEFANO: Weka.classify(classifier, Weka.getTest(), trainingSet)
         * ******************************************************************/

        prediction = Weka.classify(mapModel.get(mapSticker.get(ml.getIdentifier()).getType()), Weka.getTest(res, mapSticker.get(ml.getIdentifier()).getType()));

        /*if (ml.getIdentifier().equals(identifierBottle))
            prediction = Weka.classify(clsBottle, Weka.getTest(res), dataBottle);
        else if (ml.getIdentifier().equals(identifierMedicine))
            prediction = Weka.classify(clsMedicine, Weka.getTest(res), dataMedicine);
        else if (ml.getIdentifier().equals(identifierKnife))
            prediction = Weka.classify(clsKnife, Weka.getTest(res), dataKnife);*/

        res.setAction(prediction);
        return res;
    }

    private Classifier getClassifierFromModel(byte[] model) {
        // deserialize model
        Classifier cls = null;
        try {
            /*Create an InputStream object to pass to SerializationHelper.read method.
             *This permit to avoid save all model in a file and read it to create the classifier */
            //InputStream stream = new ByteArrayInputStream(/*infoSticker.getCategory().get(0).getModel().getBytes(StandardCharsets.UTF_8)*/mapModel.get(objType));
            /*Vecchio argomento di read:
             *Environment.getExternalStorageDirectory()+"/Android/"+ objType +".model"*/
            /*FileReader file = new FileReader(Environment.getExternalStorageDirectory() + "/Android/" + model + ".model");
            BufferedReader br = new BufferedReader(file);*/
            //System.out.println("File model: " + br.readLine());
            cls = (Classifier) SerializationHelper.read(new ByteArrayInputStream(model)/*Environment.getExternalStorageDirectory() + "/Android/" + model + ".model"*/);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cls;
    }

}




    /*
    private void createModelFromArff(String objType, Classifier cls) {
        Instances inst;
        try{
            inst = new Instances(new BufferedReader(new FileReader(Environment.getExternalStorageDirectory()+"/Android/"+ objType +".arff")));
            inst.setClassIndex(inst.numAttributes() - 1);
            cls.buildClassifier(inst);
            SerializationHelper.write(Environment.getExternalStorageDirectory()+"/Android/"+ objType +".model", cls);
        } catch(IOException e){
            e.printStackTrace();
        } catch(Exception e){
            e.printStackTrace();
        }
    }




    // The following code is able to segment manipulations

    private MovementList SpezzaMovimento(MyMovement movement) {
        double alfa = 0.7; // più è alto più consideta l'ultima rilevazione
        double soglia = 400.00;
        final int numeroCampioni = 3;

        int count = 1;
        double emaAlt = 0.0;

        List<MyNearable> list = new ArrayList<>();
        MovementList ml = new MovementList();
        MyNearable[] myNearables = new MyNearable[numeroCampioni];
        List<MyNearable> listNearable = movement.getList();

        for(MyNearable n : listNearable) { // controllo per ogni rilevazione se spezzettare il movimento o no
            MyNearable[] myNearablesTmp = new MyNearable[numeroCampioni];
            for(int k=1; k<numeroCampioni; k++){
                myNearablesTmp[k] = myNearables[k-1];
            }
            myNearablesTmp[0] = n;
            myNearables = myNearablesTmp;

            double calcAlt = AlternativeCalc(myNearables);

            emaAlt = alfa * calcAlt + (1 - alfa) * emaAlt;

            if(list.isEmpty()){ // se è la prima rilevazione del movimento
                list.add(n);
            }else {
                if (emaAlt <= soglia || n.equals(listNearable.get(listNearable.size() - 1))) {
                    list.add(n);
                    if (n.equals(listNearable.get(listNearable.size() - 1))) { // ultima rilevazione del movimento
                        long dur = list.get(list.size() - 1).getTime() - list.get(0).getTime();
                        Statistics stat = new Statistics(list, dur);

                        MyMovement myMovement = new MyMovement(stat, n.getId(), dur, list.size(), list.get(0).getDate());
                        myMovement.addStickers(list);

                        ml = ml.add(myMovement, count);
                        count++;
                        list = new ArrayList<>();

                    }
                } else { // bisogna spezzare il movimento
                    long dur = list.get(list.size() - 1).getTime() - list.get(0).getTime();
                    Statistics stat = new Statistics(list, dur);

                    MyMovement myMovement = new MyMovement(stat, n.getId(), dur, list.size(), list.get(0).getDate());
                    myMovement.addStickers(list);

                    ml = ml.add(myMovement, count);
                    count++;

                    list = new ArrayList<>();
                    list.add(n);

                    myNearables = new MyNearable[numeroCampioni];
                    emaAlt = 0.0;

                }
            }
        }
        return ControlloTempoMinimo(ml);
    }

    private MovementList ControlloTempoMinimo(MovementList ml) {
        long tMinimo = 500;
        MovementList res = new MovementList();
        int count = 1;

        boolean durataUnioneMovimenti  = false;

        for (int i=1; i<ml.size()+1; i++){
            MyMovement movCorrente;
            MyMovement movSuccessivo;

            if(durataUnioneMovimenti && ml.getMap().get(i + 1) != null) {
                movSuccessivo = ml.getMap().get(i + 1);
                movCorrente = res.removeMovement(count - 1);
                count --;
                durataUnioneMovimenti = false;

            } else {
                movCorrente = ml.getMap().get(i);
                movSuccessivo = ml.getMap().get(i + 1);
            }


            if(movSuccessivo != null) {
                boolean idUguali = movCorrente.getIdentifier().equals(movSuccessivo.getIdentifier());

                long diff = movSuccessivo.getList().get(0).getTime() - movCorrente.getList().get(movCorrente.getList().size()-1).getTime();
                boolean stessoMovimentoOriginario = diff < 300;
                boolean movimentoDurataZero = movCorrente.getDurata() == (long) 0 || movSuccessivo.getDurata() == (long) 0;
                boolean movimentoCorto = movCorrente.getDurata() < tMinimo || movSuccessivo.getDurata() < tMinimo;
                boolean ultimoMovimento = i != ml.size();

                if (idUguali && stessoMovimentoOriginario && (movimentoDurataZero || movimentoCorto) && ultimoMovimento) {
                    res = res.add(joinMovements(movCorrente, movSuccessivo), count);
                    count++;
                    i++;
                    durataUnioneMovimenti = movCorrente.getDurata() + movSuccessivo.getDurata() < tMinimo;
                } else {
                    res = res.add(movCorrente, count);
                    count++;
                }
            } else {
                res = res.add(movCorrente, count);
                count++;
            }
        }

        return res;
    }

    private MyMovement joinMovements(MyMovement a, MyMovement b) {
        List<MyNearable> ln = a.getList();
        ln.addAll(b.getList());
        Statistics stat = new Statistics(ln);
        MyMovement ret = new MyMovement(stat, ln.get(0).getId(), ln.get(ln.size()-1).getTime()-ln.get(0).getTime(), ln.size(), a.getDate());
        ret.addStickers(ln);

        return ret;
    }

    private double AlternativeCalc(MyNearable[] myNearables) {
        double sum = 0.0;

        int size = myNearables.length;

        for(int i=1; i<size; i++){
            MyNearable a = myNearables[i-1]==null ? null : myNearables[i-1];
            MyNearable b = myNearables[i]==null ? null : myNearables[i];

            double x = a==null ? 0.0 : a.getX();
            double y = a==null ? 0.0 : a.getY();
            double z = a==null ? 0.0 : a.getZ();

            double x2 = b==null ? 0.0 : b.getX();
            double y2 = b==null ? 0.0 : b.getY();
            double z2 = b==null ? 0.0 : b.getZ();

            sum += Calc(x, y, z, x2, y2, z2);
        }

        return sum/size;
    }

    private static double Calc(double x, double y, double z, double x2, double y2, double z2) {
        double a = x-x2;
        double b = y-y2;
        double c = z-z2;

        double powA = Math.pow((a), 2);
        double powB = Math.pow((b), 2);
        double powC = Math.pow((c), 2);

        double result = Math.sqrt(powA + powB + powC);

        return result;
    }
*/


