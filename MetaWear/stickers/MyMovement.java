package com.alesp.feedbackapp.MetaWear.stickers;
import android.os.Parcel;
import android.os.Parcelable;

import com.alesp.feedbackapp.MetaWear.MetaWearManipulation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Stefano on 24/11/2015.
 */
public class MyMovement implements Parcelable {
    private List<MyNearable> list;

    private String etichettaTemporanea;
    private String identifier; // id sticker
    private Long durata;
    private Double maxX;
    private Double maxY;
    private Double maxZ;
    private Double minX;
    private Double minY;
    private Double minZ;
    private Double meanX;
    private Double meanY;
    private Double meanZ;
    private Double varX;
    private Double varY;
    private Double varZ;
    private Double stdDevX;
    private Double stdDevY;
    private Double stdDevZ;
    private Double differenceX;
    private Double differenceY;
    private Double differenceZ;
    private Double covXY;
    private Double covXZ;
    private Double covYZ;
    private Double pearsonXY;
    private Double pearsonXZ;
    private Double pearsonYZ;
    private Double consecutiviX;
    private Double consecutiviY;
    private Double consecutiviZ;
    private Double xzero;
    private Double yzero;
    private Double zzero;
    private Double xenergy;
    private Double yenergy;
    private Double zenergy;
    private Double rootMeanSquareX;
    private Double rootMeanSquareY;
    private Double rootMeanSquareZ;
    private Double kurtosisX;
    private Double kurtosisY;
    private Double kurtosisZ;
    private Double quartileInferioreX;
    private Double quartileInferioreY;
    private Double quartileInferioreZ;
    private Double quartileSuperioreX;
    private Double quartileSuperioreY;
    private Double quartileSuperioreZ;
    private String azione;
    private int ncampioni;
    private Date timeStart;
    private Date timeEnd;

    public MyMovement(Statistics stat, String id, long durMotion, int size, Date data) {
        list = new ArrayList<>();

        etichettaTemporanea = "Unknown";
        identifier = id;
        durata = durMotion;
        maxX = stat.getMax("x");
        maxY = stat.getMax("y");
        maxZ = stat.getMax("z");
        minX = stat.getMin("x");
        minY = stat.getMin("y");
        minZ = stat.getMin("z");
        meanX = stat.getMean("x");
        meanY = stat.getMean("y");
        meanZ = stat.getMean("z");
        varX = stat.getVariance("x");
        varY = stat.getVariance("y");
        varZ = stat.getVariance("z");
        stdDevX = stat.getStdDev("x");
        stdDevY = stat.getStdDev("y");
        stdDevZ = stat.getStdDev("z");
        differenceX = stat.getDifference("x");
        differenceY = stat.getDifference("y");
        differenceZ = stat.getDifference("z");
        covXY = stat.getCovariance("x", "y");
        covXZ = stat.getCovariance("x", "z");
        covYZ = stat.getCovariance("y", "z");
        pearsonXY = stat.getPearson("x", "y");
        pearsonXZ = stat.getPearson("x", "z");
        pearsonYZ = stat.getPearson("y", "z");
        consecutiviX = stat.getConsecutivi("x");
        consecutiviY = stat.getConsecutivi("y");
        consecutiviZ = stat.getConsecutivi("z");
        xzero = stat.getZeroCrossing("x");
        yzero = stat.getZeroCrossing("y");
        zzero = stat.getZeroCrossing("z");
        xenergy = stat.getEnergy("x");
        yenergy = stat.getEnergy("y");
        zenergy = stat.getEnergy("z");
        rootMeanSquareX = stat.getRootMeanSquare("x");
        rootMeanSquareY = stat.getRootMeanSquare("y");
        rootMeanSquareZ = stat.getRootMeanSquare("z");
        kurtosisX = stat.getKurtosis("x");
        kurtosisY = stat.getKurtosis("y");
        kurtosisZ = stat.getKurtosis("z");
        quartileInferioreX = stat.getQuartile("x", 25.00);
        quartileInferioreY = stat.getQuartile("y", 25.00);
        quartileInferioreZ = stat.getQuartile("z", 25.00);
        quartileSuperioreX = stat.getQuartile("x", 75.00);
        quartileSuperioreY = stat.getQuartile("y", 75.00);
        quartileSuperioreZ = stat.getQuartile("z", 75.00);
        azione = null;
        ncampioni = size;
        if(data == null)
            timeStart = new Date();
        else
            timeStart = data;
        timeEnd = null;

    }

    public void addStickers(List<MyNearable> sl){
        list= sl;
        timeStart = sl.get(0).getDate();
        timeEnd = sl.get(sl.size()-1).getDate();
        ncampioni = sl.size();
        identifier = sl.get(0).getId();
        azione =  sl.get(0).getAction();
    }

    public String getEtichettaTemporanea(){ return etichettaTemporanea; }

    public void setEtichettaTemporanea(String str){ etichettaTemporanea = str; }

    public Double getMaxX() {
        return maxX;
    }

    public Double getMaxY() {
        return maxY;
    }

    public Double getMaxZ() {
        return maxZ;
    }

    public Double getMinX() {
        return minX;
    }

    public Double getMinY() {
        return minY;
    }

    public Double getMinZ() {
        return minZ;
    }

    public Double getMeanX() {
        return meanX;
    }

    public Double getMeanY() {
        return meanY;
    }

    public Double getMeanZ() {
        return meanZ;
    }

    public Double getVarX() {
        return varX;
    }

    public Double getVarY() {
        return varY;
    }

    public Double getVarZ() {
        return varZ;
    }

    public Double getStdDevX() {
        return stdDevX;
    }

    public Double getStdDevY() {
        return stdDevY;
    }

    public Double getStdDevZ() {
        return stdDevZ;
    }

    public Double getDifferenceX() {
        return differenceX;
    }

    public Double getDifferenceY() {
        return differenceY;
    }

    public Double getDifferenceZ() {
        return differenceZ;
    }

    public Double getCovXY() {
        return covXY;
    }

    public Double getCovXZ() {
        return covXZ;
    }

    public Double getCovYZ() {
        return covYZ;
    }

    public Double getPearsonXY() {
        return pearsonXY;
    }

    public Double getPearsonXZ() {
        return pearsonXZ;
    }

    public Double getPearsonYZ() {
        return pearsonYZ;
    }

    public Double getConsecutiviX() {
        return consecutiviX;
    }

    public Double getConsecutiviY() {
        return consecutiviY;
    }

    public Double getConsecutiviZ() {
        return consecutiviZ;
    }

    public Double getYzero() {
        return yzero;
    }

    public Double getXzero() {
        return xzero;
    }

    public Double getZzero() {
        return zzero;
    }

    public Double getXenergy() {
        return xenergy;
    }

    public Double getYenergy() {
        return yenergy;
    }

    public Double getZenergy() {
        return zenergy;
    }

    public int getNcampioni() {
        return ncampioni;
    }

    public Date getTimeStart() {
        return timeStart;
    }

    public Date getTimeEnd() {
        return timeEnd;
    }

    public String getAction(){
        return azione;
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof MyMovement))
            return false;

        MyMovement obj = (MyMovement) o;
        if(obj.getIdentifier().equals(identifier) &&
                obj.getDurata() == durata &&
                obj.getConsecutiviX() == consecutiviX &&
                obj.getConsecutiviY() == consecutiviY &&
                obj.getConsecutiviZ() == consecutiviZ &&
                obj.getXzero() == xzero &&
                obj.getYzero() == yzero &&
                obj.getZzero() == zzero &&
                obj.getMaxX() == maxX &&
                obj.getMaxY() == maxY &&
                obj.getMaxZ() == maxZ &&
                obj.getMinX() == minX &&
                obj.getMinY() == minY &&
                obj.getMinZ() == minZ &&
                obj.getMeanX() == meanX &&
                obj.getMeanY() == meanY &&
                obj.getMeanZ() == meanZ &&
                obj.getDate().equals(timeStart) &&
                obj.getFinishDate().equals(timeEnd))
            return true;
        else {
            return false;
        }
    }

    public List<MyNearable> getList(){
        return list;
    }

    public void setAction(String nameAction){
        if(nameAction == null){
            azione = "";
            for(MyNearable n: list)
                n.addAction("");
        } else {
            azione = nameAction;
            for(MyNearable n: list)
                n.addAction(nameAction);
        }
    }

    public double[] getValues(){
        double[] values = new double[47];
        values[0] = durata;
        values[1] = ncampioni;
        values[2] = consecutiviX;
        values[3] = consecutiviY;
        values[4] = consecutiviZ;
        values[5] = maxX;
        values[6] = maxY;
        values[7] = maxZ;
        values[8] = minX;
        values[9] = minY;
        values[10] = minZ;
        values[11] = meanX;
        values[12] = meanY;
        values[13] = meanZ;
        values[14] = stdDevX;
        values[15] = stdDevY;
        values[16] = stdDevZ;
        values[17] = differenceX;
        values[18] = differenceY;
        values[19] = differenceZ;
        values[20] = varX;
        values[21] = varY;
        values[22] = varZ;
        values[23] = covXY;
        values[24] = covXZ;
        values[25] = covYZ;
        values[26] = pearsonXY;
        values[27] = pearsonXZ;
        values[28] = pearsonYZ;
        values[29] = xzero;
        values[30] = yzero;
        values[31] = zzero;
        values[32] = xenergy;
        values[33] = yenergy;
        values[34] = zenergy;
        values[35] = rootMeanSquareX;
        values[36] = rootMeanSquareY;
        values[37] = rootMeanSquareZ;
        values[38] = kurtosisX;
        values[39] = kurtosisY;
        values[40] = kurtosisZ;
        values[41] = quartileInferioreX;
        values[42] = quartileInferioreY;
        values[43] = quartileInferioreZ;
        values[44] = quartileSuperioreX;
        values[45] = quartileSuperioreY;
        values[46] = quartileSuperioreZ;

        return values;
    }

    protected MyMovement(Parcel in) {
        if (in.readByte() == 0x01) {
            list = new ArrayList<MyNearable>();
            in.readList(list, MyNearable.class.getClassLoader());
        } else {
            list = null;
        }
        etichettaTemporanea = in.readString();
        identifier = in.readString();
        durata = in.readByte() == 0x00 ? null : in.readLong();
        maxX = in.readByte() == 0x00 ? null : in.readDouble();
        maxY = in.readByte() == 0x00 ? null : in.readDouble();
        maxZ = in.readByte() == 0x00 ? null : in.readDouble();
        minX = in.readByte() == 0x00 ? null : in.readDouble();
        minY = in.readByte() == 0x00 ? null : in.readDouble();
        minZ = in.readByte() == 0x00 ? null : in.readDouble();
        meanX = in.readByte() == 0x00 ? null : in.readDouble();
        meanY = in.readByte() == 0x00 ? null : in.readDouble();
        meanZ = in.readByte() == 0x00 ? null : in.readDouble();
        varX = in.readByte() == 0x00 ? null : in.readDouble();
        varY = in.readByte() == 0x00 ? null : in.readDouble();
        varZ = in.readByte() == 0x00 ? null : in.readDouble();
        stdDevX = in.readByte() == 0x00 ? null : in.readDouble();
        stdDevY = in.readByte() == 0x00 ? null : in.readDouble();
        stdDevZ = in.readByte() == 0x00 ? null : in.readDouble();
        differenceX = in.readByte() == 0x00 ? null : in.readDouble();
        differenceY = in.readByte() == 0x00 ? null : in.readDouble();
        differenceZ = in.readByte() == 0x00 ? null : in.readDouble();
        covXY = in.readByte() == 0x00 ? null : in.readDouble();
        covXZ = in.readByte() == 0x00 ? null : in.readDouble();
        covYZ = in.readByte() == 0x00 ? null : in.readDouble();
        pearsonXY = in.readByte() == 0x00 ? null : in.readDouble();
        pearsonXZ = in.readByte() == 0x00 ? null : in.readDouble();
        pearsonYZ = in.readByte() == 0x00 ? null : in.readDouble();
        consecutiviX = in.readByte() == 0x00 ? null : in.readDouble();
        consecutiviY = in.readByte() == 0x00 ? null : in.readDouble();
        consecutiviZ = in.readByte() == 0x00 ? null : in.readDouble();
        xzero = in.readByte() == 0x00 ? null : in.readDouble();
        yzero = in.readByte() == 0x00 ? null : in.readDouble();
        zzero = in.readByte() == 0x00 ? null : in.readDouble();
        xenergy = in.readByte() == 0x00 ? null : in.readDouble();
        yenergy = in.readByte() == 0x00 ? null : in.readDouble();
        zenergy = in.readByte() == 0x00 ? null : in.readDouble();
        rootMeanSquareX = in.readByte() == 0x00 ? null : in.readDouble();
        rootMeanSquareY = in.readByte() == 0x00 ? null : in.readDouble();
        rootMeanSquareZ = in.readByte() == 0x00 ? null : in.readDouble();
        kurtosisX = in.readByte() == 0x00 ? null : in.readDouble();
        kurtosisY = in.readByte() == 0x00 ? null : in.readDouble();
        kurtosisZ = in.readByte() == 0x00 ? null : in.readDouble();
        quartileInferioreX = in.readByte() == 0x00 ? null : in.readDouble();
        quartileInferioreY = in.readByte() == 0x00 ? null : in.readDouble();
        quartileInferioreZ = in.readByte() == 0x00 ? null : in.readDouble();
        quartileSuperioreX = in.readByte() == 0x00 ? null : in.readDouble();
        quartileSuperioreY = in.readByte() == 0x00 ? null : in.readDouble();
        quartileSuperioreZ = in.readByte() == 0x00 ? null : in.readDouble();
        azione = in.readString();
        ncampioni = in.readInt();
        long tmpDate = in.readLong();
        timeStart = tmpDate != -1 ? new Date(tmpDate) : null;
        long tmpDate2 = in.readLong();
        timeEnd = tmpDate2 != -1 ? new Date(tmpDate2) : null;

    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Date getDate() {
        return timeStart;
    }

    public Date getFinishDate() { return timeEnd; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (list == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(list);
        }
        dest.writeString(etichettaTemporanea);
        dest.writeString(identifier);
        if (durata == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeLong(durata);
        }
        if (maxX == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(maxX);
        }
        if (maxY == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(maxY);
        }
        if (maxZ == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(maxZ);
        }
        if (minX == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(minX);
        }
        if (minY == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(minY);
        }
        if (minZ == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(minZ);
        }
        if (meanX == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(meanX);
        }
        if (meanY == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(meanY);
        }
        if (meanZ == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(meanZ);
        }
        if (varX == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(varX);
        }
        if (varY == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(varY);
        }
        if (varZ == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(varZ);
        }
        if (stdDevX == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(stdDevX);
        }
        if (stdDevY == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(stdDevY);
        }
        if (stdDevZ == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(stdDevZ);
        }
        if (differenceX == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(differenceX);
        }
        if (differenceY == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(differenceY);
        }
        if (differenceZ == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(differenceZ);
        }
        if (covXY == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(covXY);
        }
        if (covXZ == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(covXZ);
        }
        if (covYZ == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(covYZ);
        }
        if (pearsonXY == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(pearsonXY);
        }
        if (pearsonXZ == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(pearsonXZ);
        }
        if (pearsonYZ == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(pearsonYZ);
        }
        if (consecutiviX == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(consecutiviX);
        }
        if (consecutiviY == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(consecutiviY);
        }
        if (consecutiviZ == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(consecutiviZ);
        }
        if (xzero == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(xzero);
        }
        if (yzero == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(yzero);
        }
        if (zzero == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(zzero);
        }
        if (xenergy == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(xenergy);
        }
        if (yenergy == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(yenergy);
        }
        if (zenergy == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(zenergy);
        }

        if (rootMeanSquareX == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(rootMeanSquareX);
        }
        if (rootMeanSquareY == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(rootMeanSquareY);
        }
        if (rootMeanSquareZ == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(rootMeanSquareZ);
        }

        if (kurtosisX == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(kurtosisX);
        }
        if (kurtosisY == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(kurtosisY);
        }
        if (kurtosisZ == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(kurtosisZ);
        }

        if (quartileInferioreX == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(quartileInferioreX);
        }
        if (quartileInferioreY == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(quartileInferioreY);
        }
        if (quartileInferioreZ == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(quartileInferioreZ);
        }

        if (quartileSuperioreX == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(quartileSuperioreX);
        }
        if (quartileSuperioreY == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(quartileSuperioreY);
        }
        if (quartileSuperioreZ == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(quartileSuperioreZ);
        }
        dest.writeString(azione);
        dest.writeInt(ncampioni);
        dest.writeLong(timeStart != null ? timeStart.getTime() : -1L);
        dest.writeLong(timeEnd != null ? timeEnd.getTime() : -1L);

    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MyMovement> CREATOR = new Parcelable.Creator<MyMovement>() {
        @Override
        public MyMovement createFromParcel(Parcel in) {
            return new MyMovement(in);
        }

        @Override
        public MyMovement[] newArray(int size) {
            return new MyMovement[size];
        }
    };

    public String getIdentifier() {
        return identifier;
    }

    public Long getDurata() {
        return durata;
    }

    public void setStickersId(int stickersId) {
        for(MyNearable n : list)
            n.setId(stickersId);
    }
}