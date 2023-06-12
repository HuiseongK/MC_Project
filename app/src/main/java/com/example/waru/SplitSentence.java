package com.example.waru;

import java.util.ArrayList;
import kss.Kss;

public class SplitSentence {
    Kss kss = new Kss();

    public ArrayList<String> Start_Split(String text){
        Kss kss = new Kss();
        ArrayList<String> result = kss.splitSentences(text, true);
        return result;
    }


}
