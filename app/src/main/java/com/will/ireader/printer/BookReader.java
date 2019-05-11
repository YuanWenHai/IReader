package com.will.ireader.printer;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;

/**
 * created  by will on 2019/5/11 16:07
 */

public class BookReader {


    /**
     * read book string with charset from file system, null will be returned if file not found.
     * @param book
     * @param charset
     * @return book string or null
     */
    public static String read(File book, Charset charset){
        if(!book.isFile() || !book.exists() ){
            return null;
        }
        StringBuilder builder = new StringBuilder();
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(book),charset));
            String temp;

            while ((temp = reader.readLine()) != null){
                builder.append(temp).append("\n");
            }
            return builder.toString();
        }catch (FileNotFoundException f){
            Log.e("read book error","file not found");
            return null;
        }catch (IOException i){
            Log.e("read book error","IO exception");
            i.printStackTrace();
            return null;
        }
    }
}
