package projekt;
class TRownanie {
    TWyrazenie lewaStrona;
    String znak;
    Double wartosc;
    TRownanie(String string, TWyrazenie w, Double ldz) {
       lewaStrona=w;
       znak=string;
       wartosc=ldz;
    }
    /**
     ** Metoda zwraca najwyższy stopień podanego wielomiany 
     * @return stopień wilomianu
     */
    public double getMaxSt(){
        double max=0;
        for(int i=0;i<lewaStrona.lista.size();i++){
            if(max<lewaStrona.lista.get(i).getPotega()){
                max=lewaStrona.lista.get(i).getPotega();
            }
        }
        return max;
    }
    public int getIndexDegree(double degree){
         for(int i=0;i<lewaStrona.lista.size();i++){
            if(degree==lewaStrona.lista.get(i).getPotega()){
                return i;
            }
        }
         return -1;
    }
    public void sort(){
        for(int i=0;i<lewaStrona.lista.size();i++){
           for(int j=i;j<lewaStrona.lista.size();j++){
               if(lewaStrona.lista.get(i).getPotega()<lewaStrona.lista.get(j).getPotega()){
                   TSkladnik tmp=lewaStrona.lista.get(j);
                   lewaStrona.lista.set(j,lewaStrona.lista.get(i));
                   lewaStrona.lista.set(i, tmp);
               }
            } 
        }
    }
}
