package com.example;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by evgeniy on 26.07.2016.
 */
public class Albums {
    public String title;
    public String message;
    //public String[] errors=new String[]{};
    public List<String> errors=new ArrayList<String>();
    public String total;
    public int total_pages;
    public int page;
    public String limit;

    List<Dataset> dataset=new ArrayList<Dataset>();
}
