package it.noovle.android.texaandroidvoiceinteraction;

/**
 * Created by fabiosgro on 15/02/15.
 */
public class Risultato {

    private String titolo;
    private String url;
    private String num;

    public Risultato() {

    }

    public Risultato(String num, String titolo, String url) {
        this.num = num;
        this.titolo = titolo;
        this.url = url;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


}
