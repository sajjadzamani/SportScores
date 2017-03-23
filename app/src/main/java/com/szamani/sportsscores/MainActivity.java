package com.szamani.sportsscores;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private Document htmlDoc;
    private String htmlUrl = "http://www.espn.com/nba/scoreboard/_/date/20170323";
    private TextView txt;

    private List<JsoupAsyncTask.Team> teams = Collections.synchronizedList(new ArrayList<JsoupAsyncTask.Team>());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout inner=  (LinearLayout) findViewById(R.id.innerLayout);
        getLayoutInflater().inflate(R.layout.game, inner);


        JsoupAsyncTask jsoupAsyncTask = new JsoupAsyncTask();
        jsoupAsyncTask.execute();

    }

    public void createNewGame(List<JsoupAsyncTask.Team> teams){


    }

    private class JsoupAsyncTask extends AsyncTask<Void,Void,Void> {

        class Team{
            private String name;
            private String score;

            public Team(String name, String score){
                this.name = name;
                this.score = score;
            }

            public Team(){
                this.name="";
                this.score="";
            }

            private String getScore(){
                return score;
            }

            private String getName(){
                return name;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Team team =new Team();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                htmlDoc = Jsoup.connect(htmlUrl)
                        .maxBodySize(0).get();
                Elements scripts = htmlDoc.getElementsByTag("script");
                Element scoreReport =null;
                for(int i=0;i < scripts.size();i++){
                    if(scripts.get(i).toString().contains("window.espn.scoreboardData")){
                        scoreReport = scripts.get(i);
                        break;
                    }
                }

                Pattern namePattern = Pattern.compile("\"location\":\"(.*?)\"");
                Pattern scorePattern = Pattern.compile("\"score\":\"(.*?)\"");
                Matcher nameMatcher = namePattern.matcher(scoreReport.toString());
                Matcher scoreMatcher = scorePattern.matcher(scoreReport.toString());

                while(nameMatcher.find() && scoreMatcher.find()){
                    Team newTeam = new Team(nameMatcher.group(1),scoreMatcher.group(1));
                    teams.add(newTeam);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            txt.setText(teams.get(teams.size() -2).getName());

        }

    }
}
