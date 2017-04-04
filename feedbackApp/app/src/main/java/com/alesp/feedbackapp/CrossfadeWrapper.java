package com.alesp.feedbackapp;

/**
 * Created by alesp on 31/03/2017.
 */
        import com.mikepenz.crossfader.Crossfader;
        import com.mikepenz.google_material_typeface_library.GoogleMaterial;
        import com.mikepenz.materialdrawer.Drawer;
        import com.mikepenz.materialdrawer.interfaces.ICrossfader;
        import com.mikepenz.materialdrawer.model.ExpandableDrawerItem;
        import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;

public class CrossfadeWrapper implements ICrossfader {
    private Crossfader mCrossfader;
    private Drawer myDrawer;
    private PrimaryDrawerItem dummy;
    private int position = -1;
    private boolean isComplete = false;

    public CrossfadeWrapper(Crossfader crossfader) {
        this.mCrossfader = crossfader;
    }
    public CrossfadeWrapper(Crossfader crossfader,Drawer myDrawer, PrimaryDrawerItem dummy){
        this.mCrossfader=crossfader;
        this.dummy=dummy;
        this.myDrawer=myDrawer;
        isComplete = true;

    }

    @Override
    public void crossfade() {
        if(isComplete){
            position = position == -1 ?  (int) dummy.getIdentifier() : position;

            if(!mCrossfader.isCrossFaded()){
                myDrawer.removeItemByPosition(position);
            }
            else{
                myDrawer.addItemAtPosition(new PrimaryDrawerItem().withIcon(GoogleMaterial.Icon.gmd_settings),position);
            }

        }
        mCrossfader.crossFade();
    }

    //Metodo custom che faccio io perchè sono figo
    public void crossfade(Drawer myDrawer, PrimaryDrawerItem dummy){


        position = position == -1 ?  (int) dummy.getIdentifier() : position;

        if(!mCrossfader.isCrossFaded()){
            myDrawer.removeItemByPosition(position);
        }
        else{
            myDrawer.addItemAtPosition(new PrimaryDrawerItem().withIcon(GoogleMaterial.Icon.gmd_settings),position);
        }

        mCrossfader.crossFade();

    }

    @Override
    public boolean isCrossfaded() {
        return mCrossfader.isCrossFaded();
    }
}