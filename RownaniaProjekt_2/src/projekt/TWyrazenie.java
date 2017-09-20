package projekt;
import java.util.ArrayList;
import java.util.List;


public class TWyrazenie {
    public TSkladnik skl=null;
    List<String> znaki = new ArrayList<>();
    List<TSkladnik> lista = new ArrayList<>();
    public TWyrazenie(TSkladnik s){
        this.skl=s;
        lista.add(s);
        System.out.println(s.getLiczba());
    }
    public TWyrazenie(String str,TSkladnik s){
        this.skl=s;
        if(str.equals("-")){
           skl.setLiczba(-1*skl.getLiczba());
        }
        System.out.println(skl.getLiczba());
        lista.add(skl);
    }
    public TWyrazenie(String str,TWyrazenie w,TSkladnik s){
        if(str.equals("-")){
            s.setLiczba(-1*s.getLiczba());
            str="+";
        }
        skl=s;
        znaki=w.znaki;
        znaki.add(str);
        lista=w.lista;
        lista.add(s);
    }
    public double oblicz(double x){
        int licznik =0;
        double wynik=0;
        int licznik2=0;
        List<Double> listaZmiennych= new ArrayList<>();
        for(int i=0;i<znaki.size();i++){
            if(znaki.get(i).equals("*") || znaki.get(i).equals("/") ){
                licznik++;
            }
            listaZmiennych.add(this.lista.get(i).oblicz(x));
        }
        listaZmiennych.add(this.lista.get(this.lista.size()-1).oblicz(x));
        licznik2=0;
        while(licznik>0){
            if(licznik2>=znaki.size()){
                licznik2=0;
            }
            if(znaki.get(licznik2).equals("*")){
                Double tmp=listaZmiennych.get(licznik2)*listaZmiennych.get(licznik2+1);
                listaZmiennych.set(licznik2, tmp);
                listaZmiennych.remove(licznik2+1);
                licznik--;
                znaki.remove(licznik2);
                if(licznik2>=znaki.size()){
                licznik2=0;
                }
            }
            if(znaki.get(licznik2).equals("/")){
                Double tmp=listaZmiennych.get(licznik2)/listaZmiennych.get(licznik2+1);
                listaZmiennych.set(licznik2, tmp);
                listaZmiennych.remove(licznik2+1);
                licznik--;
                znaki.remove(licznik2);
                if(licznik2>=znaki.size()){
                licznik2=0;
                }
            }
            licznik2++;
        }
        for(int i=0;i<znaki.size();i++){
            if(znaki.get(i).equals("+")){
                Double tmp=listaZmiennych.get(i)+listaZmiennych.get(i+1);
                listaZmiennych.set(i, tmp);
                listaZmiennych.set(i+1,0.0);
            }
            if(znaki.get(i).equals("-")){
                Double tmp=listaZmiennych.get(i)-listaZmiennych.get(i+1);
                listaZmiennych.set(i, tmp);
                listaZmiennych.set(i+1,0.0);
            }
        }
        for(int i=0;i<listaZmiennych.size();i++){
            wynik+=listaZmiennych.get(i);
        }
        return wynik;
    }

}
