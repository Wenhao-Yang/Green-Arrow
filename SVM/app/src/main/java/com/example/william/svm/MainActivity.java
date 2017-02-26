package com.example.william.svm;

import android.Manifest;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

import static com.example.william.svm.util.Util.dataToFeatures;
import com.example.william.svm.svmlib.svm_predict;
import com.example.william.svm.svmlib.svm_scale;
import com.example.william.svm.svmlib.svm_train;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerAction, spinnerPosition;
    private int lableAction = 1, lablePosition = 1, lable;
    private int sinter = 1000 * 1000 / 32;   // 传感器采样频率，1秒钟采集32个数据，4秒钟采样128个数据，然后对128个数据做特征值提取

    private SensorManager sensorManager;
    private CollectionSensorListener collectionSensorListener;
    private UnderstandSensorListener understandSensorListener;

    private String directory;       // 文件目录
    private String trainFilePath;       // 样本文件路径
    private String scaleFilePath;       // 归一化后的文件路径
    private String rangeFilePath;       // 归一化规则文件
    private String modelFilePath;       // model文件路径
    private String modelTrainInfo;      // 训练model的时候控制台的信息
    private String predictFilePath;     // 使用规划化后的文件测试model的结果文件
    private String predictAccuracyFilePath;   // model精度文件

    private RandomAccessFile trainFile;
    private String[] actions;
    private String[] postions;
    private TextView tvAction;
    private TextView tvPostion;

    private TextView tvCollectioinNum, tvCollectionAcc, tvUnderstandAcc;
    private TextView tvAccuracy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvAction = (TextView) findViewById(R.id.tv_actions);
        tvPostion = (TextView) findViewById(R.id.tv_position);

        tvCollectioinNum = (TextView) findViewById(R.id.tv_collection_num);
        tvCollectionAcc = (TextView) findViewById(R.id.tv_train_acc);
        tvUnderstandAcc = (TextView) findViewById(R.id.tv_identify_acc);
        //tvAccuracy = (TextView) findViewById(R.id.tv_now_acc);

        actions = getResources().getStringArray(R.array.actions);
        postions = getResources().getStringArray(R.array.positions);

        // 动态请求写文件的权限
        try {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            System.out.println("权限申请成功");
        } catch (Exception e) {
            throw e;
        }


        // 创建目录
        directory = Environment.getExternalStorageDirectory() + File.separator + "SVM";
        File file = new File(directory);
        if (!file.exists()) {
            try{
                file.mkdirs();
            }
            catch(Exception e) {
                throw e;
            }

            System.out.println("文件夹创建成功");
        }

        // 给文件路径赋值
        trainFilePath = directory + File.separator + "train.txt";
        scaleFilePath = directory + File.separator + "scale.txt";
        rangeFilePath = directory + File.separator + "range.txt";
        modelFilePath = directory + File.separator + "model.txt";
        modelTrainInfo = directory + File.separator + "modelTrainInfo.txt";
        predictFilePath = directory + File.separator + "predict.txt";
        predictAccuracyFilePath = directory + File.separator + "predictAccuracy.txt";

        // 创建文件
        try {
            trainFile = new RandomAccessFile(trainFilePath, "rwd");
            System.out.print(trainFilePath);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        spinnerAction = (Spinner) findViewById(R.id.sp_actions);
        spinnerPosition = (Spinner) findViewById(R.id.sp_position);

        spinnerAction.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                lableAction = position + 1;
                System.out.println(lableAction);
                lable = lableAction * 100 + lablePosition;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerPosition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                lablePosition = position + 1;
                System.out.println(lablePosition);
                lable = lableAction * 100 + lablePosition;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        collectionSensorListener = new CollectionSensorListener();
        understandSensorListener = new UnderstandSensorListener();
    }

    /**
     * 开始采集数据
     *
     * @param view
     */
    public void startCollect(View view) {
        collectionNum = 0;
        // 每次在文件后面追加
        try {
            //trainFile.seek(trainFile.length());
        } catch (Exception e) {
            e.printStackTrace();
        }
        sensorManager.registerListener(collectionSensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sinter);
    }

    /**
     * 停止采集数据
     *
     * @param view
     */
    public void stopCollect(View view) {
        sensorManager.unregisterListener(collectionSensorListener);
    }

    /**
     * 训练模型
     *
     * @param view
     */
    public void trainModel(View view) {
        new MyTrainTask().execute();
    }

    /**
     * 开始识别
     *
     * @param view
     */
    public void startIdentify(View view) {
        // 1. 加载model
        try {
            svmModel = svm.svm_load_model(new BufferedReader(new InputStreamReader(new FileInputStream(modelFilePath))));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 2. 读取range文件
        readRange();
        // 3. 转换加速度数据
        // 4. 识别

        sensorManager.registerListener(understandSensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sinter);
    }


    private svm_model svmModel;
    private List<String> range = new ArrayList<>();
    double scaleLower, scaleUpper;      // 规划的最大值和最小值
    double[][] featureRange;        // 规划区间
    int featureCount;


    /**
     * 读取range文件
     */
    private void readRange() {
        range.clear();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(rangeFilePath)));
            String string = null;
            while ((string = bufferedReader.readLine()) != null) {
                range.add(string);
            }
            featureCount = range.size() - 2;
            featureRange = new double[featureCount][2];

            // 读取lower 和 upper
            String lowerAndUpper = range.get(1);
            String[] split = lowerAndUpper.split(" ");  // 以空格切分字符串
            scaleLower = Double.parseDouble(split[0]);
            scaleUpper = Double.parseDouble(split[1]);

            // 读取每一行的特征值
            for (int i = 0; i < featureCount; i++) {
                String[] featureLowerAndUpper = range.get(i + 2).split(" ");
                featureRange[i][0] = Double.parseDouble(featureLowerAndUpper[1]);
                featureRange[i][1] = Double.parseDouble(featureLowerAndUpper[2]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止识别
     *
     * @param view
     */
    public void stopIdentify(View view) {
        sensorManager.unregisterListener(understandSensorListener);
    }

    int collectionNum;

    /**
     * 收集手机数据传感器回调类
     */
    class CollectionSensorListener implements SensorEventListener {

        private float x;
        private float y;
        private float z;

        int num = 128;
        int currentNum = 0;
        double[] data = new double[num];

        @Override
        public void onSensorChanged(SensorEvent event) {
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];
            double acc = Math.sqrt(x * x + y * y + z * z);
            tvCollectionAcc.setText(String.valueOf(acc));
            System.out.println("开始采集数据:" + acc);
            // 当采集数量达到128个以后转换成特征值写入到文件
            if (currentNum >= num) {
                String[] features = dataToFeatures(data, sinter);
                // String[] features = dataToFeaturesArr(data);
                writeToFile(features);
                currentNum = 0;
                collectionNum++;
                tvCollectioinNum.setText(String.valueOf(collectionNum));
            }
            data[currentNum++] = acc;
        }

        /**
         * 把特征值写入到文件
         *
         * @param features
         */
        private void writeToFile(String[] features) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(lable);
            for (String feature : features) {
                stringBuilder.append(" " + feature);
            }
            stringBuilder.append("\n");
            try {
                trainFile.write(stringBuilder.toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    /**
     * 识别行为传感器回调类
     */
    class UnderstandSensorListener implements SensorEventListener {

        private float x;
        private float y;
        private float z;
        int num = 128;
        int currentNum = 0;
        double[] data = new double[num];

        @Override
        public void onSensorChanged(SensorEvent event) {
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];
            double acc = Math.sqrt(x * x + y * y + z * z);
            tvUnderstandAcc.setText(String.valueOf(acc));
            System.out.println("开始识别:" + acc);
            if (currentNum >= num) {
                double code = underStand(dataToFeatures(data, sinter));
                //double code = underStand(dataToFeaturesArr(data));
                int action = (int) (code / 100);
                int postion = (int) (code - action * 100);

                tvAction.setText(actions[action - 1]);
                tvPostion.setText(postions[postion - 1]);

                System.out.println(code);
                currentNum = 0;
            }
            data[currentNum++] = acc;
        }

        private String[] featureString;

        private double underStand(String[] features) {
            svm_node[] svm_nodes = new svm_node[featureCount];
            svm_node svm_node;
            for (int i = 0; i < features.length; i++) {
                featureString = features[i].split(":");
                svm_node = new svm_node();
                svm_node.index = Integer.parseInt(featureString[0]);
                svm_node.value = Double.parseDouble(featureString[1]);
                svm_nodes[i] = svm_node;
            }
            return svm.svm_predict(svmModel, svm_nodes);
        }


        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }


    /**
     * 训练model的类
     */
    class MyTrainTask extends AsyncTask<Void, Void, Void> {

        /**
         * 开始执行
         */
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            tarinModel(trainFilePath,
                    rangeFilePath,
                    scaleFilePath,
                    modelFilePath,
                    predictFilePath,
                    modelTrainInfo,
                    predictAccuracyFilePath);
            return null;
        }

        /**
         * 执行结束
         *
         * @param aVoid
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(predictAccuracyFilePath)));
                String readLine = reader.readLine();
                System.out.println(readLine);
                tvAccuracy.setText(readLine);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 训练模型
     *
     * @param trainFile
     * @param rangeFile
     * @param scaleFile
     * @param modelFile
     * @param predictResult
     * @param modelTrainInfo
     * @param prdictAccuracy
     */
    public void tarinModel(String trainFile, String rangeFile, String scaleFile, String modelFile, String predictResult, String modelTrainInfo, String prdictAccuracy) {
        creatScaleFile(new String[]{"-l", "0", "-u", "1", "-s", rangeFile, trainFile}, scaleFile);
        creatModelFile(new String[]{"-s", "0", "-c", "128.0", "-t", "2", "-g", "8.0", "-e", "0.1", scaleFile, modelFile}, modelTrainInfo);
        creatPredictFile(new String[]{scaleFile, modelFile, predictResult}, prdictAccuracy);
        //svm_train.main(new String[]{"-s", "0", "-c", "128.0", "-t", "2", "-g", "8.0", "-e", "0.1", scaleFile, modelFile});
        //svm_predict.main(new String[]{scaleFile, modelFile, predictResult});
    }


    /**
     * 训练数据train 进行归一化处理并生生scale文件
     *
     * @param args      String[] args = new String[]{"-l","0","-u","1",path+"/train"};
     * @param scalePath 结果输出文件路径
     */
    private static void creatScaleFile(String[] args, String scalePath) {
        FileOutputStream fileOutputStream = null;
        PrintStream printStream = null;
        try {
            File file = new File(scalePath);
            file.createNewFile();
            fileOutputStream = new FileOutputStream(file);
            printStream = new PrintStream(fileOutputStream);
            // old stream
            PrintStream oldStream = System.out;
            System.setOut(printStream);//重新定义system.out
            svm_scale.main(args);//开始归一化
            System.setOut(oldStream);//回复syste.out
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (printStream != null) {
                    printStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void creatModelFile(String[] args, String outInfo) {
        FileOutputStream fileOutputStream = null;
        PrintStream printStream = null;
        try {
            File file = new File(outInfo);
            file.createNewFile();
            fileOutputStream = new FileOutputStream(file);
            printStream = new PrintStream(fileOutputStream);
            // old stream
            PrintStream oldStream = System.out;
            System.setOut(printStream);//重新定义system.out
            svm_train.main(args);//开始训练模型
            System.setOut(oldStream);//回复syste.out
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (printStream != null) {
                    printStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void creatPredictFile(String[] args, String outInfo) {
        FileOutputStream fileOutputStream = null;
        PrintStream printStream = null;
        try {
            File file = new File(outInfo);
            file.createNewFile();
            fileOutputStream = new FileOutputStream(file);
            printStream = new PrintStream(fileOutputStream);
            // old stream
            PrintStream oldStream = System.out;
            System.setOut(printStream);//重新定义system.out
            svm_predict.main(args);//开始测试精度
            System.setOut(oldStream);//回复syste.out
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (printStream != null) {
                    printStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}