package ru.ninaland.heartrate;

import android.util.Log;

/**
 * Created by Caelestis on 09.04.2018.
 */

public class Helpers {
    public static int getStepRate(int age, int weight, int sex, int height, int rate) {
        int[] ages = new int[5];
        int stepRate = 0;

        if (weight >= 40 && weight < 50) {
            for (int i = 0; i < Methodologies.STEP_DEPENDENCY[0].length; i++) {
                ages[i] = Methodologies.STEP_DEPENDENCY[0][i];
            }
        } else if (weight >= 50 && weight < 60) {
            for (int i = 0; i < Methodologies.STEP_DEPENDENCY[1].length; i++) {
                ages[i] = Methodologies.STEP_DEPENDENCY[1][i];
            }
        } else if (weight >= 60 && weight < 70) {
            for (int i = 0; i < Methodologies.STEP_DEPENDENCY[2].length; i++) {
                ages[i] = Methodologies.STEP_DEPENDENCY[2][i];
            }
        } else if (weight >= 70 && weight < 80) {
            for (int i = 0; i < Methodologies.STEP_DEPENDENCY[3].length; i++) {
                ages[i] = Methodologies.STEP_DEPENDENCY[3][i];
            }
        } else if (weight >= 80 && weight < 90) {
            for (int i = 0; i < Methodologies.STEP_DEPENDENCY[4].length; i++) {
                ages[i] = Methodologies.STEP_DEPENDENCY[4][i];
            }
        } else if (weight >= 90 && weight < 100) {
            for (int i = 0; i < Methodologies.STEP_DEPENDENCY[5].length; i++) {
                ages[i] = Methodologies.STEP_DEPENDENCY[5][i];
            }
        } else if (weight >= 100) {
            for (int i = 0; i < Methodologies.STEP_DEPENDENCY[6].length; i++) {
                ages[i] = Methodologies.STEP_DEPENDENCY[6][i];
            }
        }

        if (age >= 30 && age < 40) {
            stepRate = ages[0];
        } else if (age >= 40 && age < 50) {
            stepRate = ages[1];
        } else if (age >= 50 && age < 60) {
            stepRate = ages[2];
        } else if (age >= 60 && age < 70) {
            stepRate = ages[3];
        } else if (age >= 70) {
            stepRate = ages[4];
        }

        Log.i(Helpers.class.getSimpleName(), "число подъемов - " + String.valueOf(stepRate));

        if (stepRate == 0)
            throw new NumberFormatException();

        return stepRate;
    }

    public static String getMOCData(int height, int weight, int stepRate, int heartRate, int age) {
        double moc = (height * weight * stepRate *1.0) / (heartRate - 60.0);
        Log.i(Helpers.class.getSimpleName(), "первая итерация - " + String.valueOf(moc));

        moc = 1.29 * Math.sqrt(moc) * Math.pow(2.7, (-0.00884 * age));
        Log.i(Helpers.class.getSimpleName(), "итог - " + String.valueOf(moc));

        return String.valueOf(Math.round(moc*100.0)/100.0);
    }

    public static String getAPHRData(int heightFieldInteger, int weightFieldInteger, int stepRate,
                                int finalRateFieldInteger, int ageFieldInteger) {

        return null;
    }
}
