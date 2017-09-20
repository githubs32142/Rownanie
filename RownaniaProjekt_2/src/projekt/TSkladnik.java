package projekt;
public class TSkladnik 
{
    private Double liczba;
    private String zmienna;
    private Double potega;

    public TSkladnik(Double liczba, String zmienna, Double potega) {
        this.liczba = liczba;
        this.zmienna = zmienna;
        this.potega = potega;
    }

    public Double getLiczba() {
        return liczba;
    }

    public Double getPotega() {
        return potega;
    }

    public String getZmienna() {
        return zmienna;
    }

    public void setLiczba(Double liczba) {
        this.liczba = liczba;
    }

    public void setPotega(Double potega) {
        this.potega = potega;
    }

    public void setZmienna(String zmienna) {
        this.zmienna = zmienna;
    }

    public double oblicz(double x){
        return liczba*(Math.pow(x,potega));
    }
    @Override
    public String toString() {
        return liczba+" "+ zmienna+" "+potega;
    }
    
}