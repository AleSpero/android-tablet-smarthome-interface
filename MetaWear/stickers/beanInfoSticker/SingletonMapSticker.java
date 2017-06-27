package com.alesp.feedbackapp.MetaWear.stickers.beanInfoSticker;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by claudio on 19/05/16.
 */
public class SingletonMapSticker {

    private static SingletonMapSticker instance = null;
    private static Map<String, Sticker> mapSticker = null;
    private static BeanInfoSticker infoSticker;

        private SingletonMapSticker(Map<String,Sticker> mapSticker){
            this.mapSticker = mapSticker;
        }

        public static synchronized SingletonMapSticker getInstance(){

            infoSticker = SingletonInfoSticker.getInstance().getInfoSticker();

            if(instance == null) {
                Map<String,Sticker> mapSticker = new HashMap<>();
                for (Sticker sticker : infoSticker.getSticker()) {
                    mapSticker.put(sticker.getId(), sticker);
                }
                instance = new SingletonMapSticker(mapSticker);
            }

            return instance;
        }

    public Map<String,Sticker> getMapSticker(){
        return mapSticker;
    }
}
