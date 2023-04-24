package com.ferreusveritas.dynamictreesplus.systems.mushroomlogic;

import com.ferreusveritas.dynamictrees.systems.poissondisc.PoissonDisc;
import com.ferreusveritas.dynamictrees.util.SimpleBitmap;

public class MushroomCapDisc extends PoissonDisc {

    private static final int minRad = 1;
    private static final int maxRad = 8;

    private static final SimpleBitmap[] cbm = new SimpleBitmap[maxRad+1];//Bitmaps of whole circles
    private static final SimpleBitmap[] icbm = new SimpleBitmap[maxRad+1];//Bitmaps of the interiors of circles(Non-edge)

    static {
        int[] circledata = {0x0, 0x48, 0x488, 0x3690, 0x248D1, 0x16D919, 0xDB5B19, 0x7FF6B19};//Packed circle data.  3 bits per slice length. 1 element per circle.
        for (int r = minRad; r <= maxRad; r++) {//Circles with radius 1 - 8
            SimpleBitmap whole = circleBitmapGen(r, circledata[r - minRad]);//Unpack circle bitmaps
            SimpleBitmap inside = new SimpleBitmap(whole.getWidth(), whole.getHeight());//Make a bitmap the same size that will serve as the inner circle pixels(non-edge)

            //Generate interior circle bitmap
            for (int z = 0; z < inside.getHeight(); z++) {
                for (int x = 0; x < inside.getWidth(); x++) {
                    boolean in;
                    if (r == 1)
                        in = x == 1 && z == 1;
                    else {
                        SimpleBitmap previousCircle = cbm[r-1];
                        in = previousCircle.isPixelOn(x - 1, z - 1);
                    }
                    inside.setPixel(x, z, in ? 1 : 0);
                }
            }

            cbm[r] = whole;
            icbm[r] = inside;
        }
        //Treat radius 0 as if it is 1
        cbm[0] = cbm[minRad];
        icbm[0] = icbm[minRad];
    }

    public MushroomCapDisc(int x, int z, int radius) {
        set(x, z, radius);
    }

    public PoissonDisc setRadius(int radius) {
        this.radius = net.minecraft.util.Mth.clamp(radius, minRad, 8);
        return this;
    }

    protected SimpleBitmap getCircleBitmap() {
        return cbm[radius];
    }

    protected SimpleBitmap getCircleInteriorBitmap() {
        return icbm[radius];
    }

}
