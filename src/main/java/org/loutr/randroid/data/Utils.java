package org.loutr.randroid.data;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import org.loutr.randroid.model.Rando;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by romain on 1/11/14.
 */
public class Utils {
    @TargetApi(11)
    static public <T> void executeAsyncTask(AsyncTask<T, ?, ?> task,
                                            T... params) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        }
        else {
            task.execute(params);
        }
    }

    /**
     * Returns the <param>nbSundays</param> sundays that precede the sunday defined in <param>calendar</param>
     * @param calendar
     * @param nbSundays
     * @return
     */
	public static List<Rando> getPreviousRandos(Calendar calendar, int nbSundays){

        if(calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY){
            return null;
        }

		List<Rando> randos = new ArrayList<Rando>();

        Calendar currentCal = calendar;

        for(int i=0;i<nbSundays;i++){
            currentCal =(Calendar)currentCal.clone();
            currentCal.add(Calendar.DAY_OF_MONTH,-7);

            Rando rando = new Rando();
            rando.setDate(currentCal);

            randos.add(rando);
        }

		return randos;
	}

}
