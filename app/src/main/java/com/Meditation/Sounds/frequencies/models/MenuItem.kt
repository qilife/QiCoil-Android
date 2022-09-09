package com.Meditation.Sounds.frequencies.models

/**
 * Created by Admin on 3/22/2017.
 */

class MenuItem {
    //    public MainActivity.MENU_ITEM getId() {
    //        return id;
    //    }

    var resId: Int = -1

    var  name: String? = null

    constructor() {

    }

    constructor(resId: Int, name: String) {
        //        this.id = itemId;
        this.resId = resId
        this.name = name
    }
}
