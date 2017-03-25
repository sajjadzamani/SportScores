package com.szamani.sportsscores;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final String htmlUrl = "http://www.espn.com/nba/scoreboard/_/date/";  //Handle to source
    private String htmlUrlDate;  //link with date
    private String date;
    private LinearLayout innerLayout; //Layout in ScrollView
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inflater = getLayoutInflater();
        innerLayout = (LinearLayout)findViewById(R.id.innerLayout);

        DatePicker datePicker = (DatePicker) findViewById(R.id.datePicker);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.YEAR, +1);
        datePicker.setMaxDate(calendar.getTimeInMillis());
        calendar.setTimeInMillis(System.currentTimeMillis());
        datePicker.init(calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int dayOfMonth) {
                date = Integer.toString(dayOfMonth + 100 * (month + 1) + 10000 * year);

            }
        });
        htmlUrlDate = htmlUrl+date;
        update();
        Button reload = (Button) findViewById(R.id.reloadBtn);
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    htmlUrlDate = htmlUrl+date;
                    update();

            }
        });

    }


    public void createNewGame(List<JsoupAsyncTask.Team> teams){
        if(innerLayout.getChildCount() > 0) {
            innerLayout.removeAllViews();
        }

        for(int i = 0; i<teams.size(); i= i+2){

            String nameHome = teams.get(i).getName();
            String scoreHome = teams.get(i).getScore();
            String nameAway = teams.get(i+1).getName();
            String scoreAway = teams.get(i+1).getScore();
            View newGame = inflater.inflate(R.layout.game_view, innerLayout, false);
            int resIdHome=MainActivity.this.getResources().getIdentifier(nameHome, "drawable", MainActivity.this.getPackageName());
            int resIdAway=MainActivity.this.getResources().getIdentifier(nameAway, "drawable", MainActivity.this.getPackageName());
            ImageView home = (ImageView) newGame.findViewById(R.id.home);
            ImageView away = (ImageView) newGame.findViewById(R.id.away);
            home.setImageResource(resIdHome);
            away.setImageResource(resIdAway);
            TextView score = (TextView) newGame.findViewById(R.id.score);
            score.setText(scoreHome+"-"+scoreAway);
            innerLayout.addView(newGame);

        }
    }

    private class JsoupAsyncTask extends AsyncTask<Void,Void,Void> {

        private Document htmlDoc;

        class Team {
            private String name;
            private String score;

            private Team(String name, String score) {
                this.name = name;
                this.score = score;
            }


            private String getScore() {
                return score;
            }

            private String getName() {
                return name;
            }
        }

        private List<JsoupAsyncTask.Team> teams = Collections.synchronizedList(new ArrayList<JsoupAsyncTask.Team>());

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                htmlDoc = Jsoup.connect(htmlUrlDate)
                        .maxBodySize(0).get();
                Elements scripts = htmlDoc.getElementsByTag("script"); //get all script tags
                Element scoreReport = null;

                for (int i = 0; i < scripts.size(); i++) {
                    if (scripts.get(i).toString().contains("window.espn.scoreboardData")) {
                        scoreReport = scripts.get(i); //script with score data
                        break;
                    }
                }

                Pattern namePattern = Pattern.compile("\"location\":\"(.*?)\""); //team identifier
                Pattern scorePattern = Pattern.compile("\"score\":\"(.*?)\"");   //team score
                Matcher nameMatcher = namePattern.matcher(scoreReport.toString());
                Matcher scoreMatcher = scorePattern.matcher(scoreReport.toString());

                while (nameMatcher.find() && scoreMatcher.find()) {
                    Team newTeam = new Team(nameMatcher.group(1).toLowerCase().replaceAll("\\s+",""),
                                            scoreMatcher.group(1));
                    teams.add(newTeam);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            createNewGame(teams);
        }
    }

    public void update(){
        JsoupAsyncTask jsoupAsyncTask = new JsoupAsyncTask();
        jsoupAsyncTask.execute();
    }
}
