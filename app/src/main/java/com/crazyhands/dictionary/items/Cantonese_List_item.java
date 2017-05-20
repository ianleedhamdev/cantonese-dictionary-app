package com.crazyhands.dictionary.items;



public class Cantonese_List_item {


    private int mwordid;
    private String menglish;
    private String mjyutping;
    private String mcantonese;
    private String msoundAddress;


    public Cantonese_List_item(int id, String eng, String jyut,String canton,String address){
        mwordid = id;
        menglish = eng;
        mjyutping = jyut;
        mcantonese = canton;
        msoundAddress = address;
    }

    public int getwordid() {
        return mwordid;
    }

    public String getenglish() {
        return menglish;
    }

    public String getjyut () {return mjyutping;}

    public String getcantonese() {return mcantonese;}

    public String getsoundaddress() {
        return msoundAddress;
    }

}
