package se.tdp025.Rangi.ncs_db;

/**
 * User: Torbjörn Kvist(torkv393)
 * Date: 2012-12-19
 */
public class Color {
    public int red;
    public int green;
    public int blue;

    public String ncs = "";
    public String hex = "";

    public Color(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public Color(String ncs, String hex,int red, int green, int blue) {
        this.ncs = ncs;
        this.hex = hex;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }
}