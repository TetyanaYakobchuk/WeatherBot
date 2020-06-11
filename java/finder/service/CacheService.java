package finder.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

//класс для кеширования
public class CacheService {


    //переменная, в которую будет сохраняться кеш
   private static StringBuffer resultStr = null;

    private CacheService(){};


    public  static String getResultStr() {
        return resultStr.toString();
    }

    public  static  void setResultStr(StringBuffer string) {
        if (resultStr==null){
            resultStr=new StringBuffer(string);
        }else resultStr.append("\n").append(string);
    }



    public  static boolean cacheIsEmpty(){
        return resultStr == null;
    }


    //метод , который каждый час очищает кеш
    @Scheduled(fixedRate =3600000)
    public void clearCache(){
        resultStr=null;
    }

}
