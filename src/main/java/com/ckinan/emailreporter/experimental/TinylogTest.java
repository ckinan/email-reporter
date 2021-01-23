package com.ckinan.emailreporter.experimental;

import org.tinylog.Logger;

public class TinylogTest {

    public static void main(String[] args) {
        for(int i=0; i<20000; i++) {
            Logger.info(i);
        }
    }

}
