package com.example.plantastic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.plantastic.data.PlantasticDatabase;
import com.example.plantastic.data.entities.KastmisVajadusIntervall;
import com.example.plantastic.data.entities.Kasutaja;
import com.example.plantastic.data.entities.Taim;
import com.example.plantastic.data.entities.TaimLiik;
import com.example.plantastic.data.entities.TaimSort;
import com.example.plantastic.data.entities.Teade;
import com.example.plantastic.data.entities.HooldusTüüp;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        new Thread(() -> {
            PlantasticDatabase db = PlantasticDatabase.getInstance(this);

            // 1. Setup Data
            Kasutaja user = new Kasutaja();
            user.kasutajanimi = "Mari";
            user.teade_start = System.currentTimeMillis();
            user.teade_aeg = System.currentTimeMillis() + 3600_000;
            long userId = db.kasutajaDao().insert(user);

            TaimLiik liik = new TaimLiik();
            liik.nimetus = "Sukulent";
            liik.ladinakeelne_nimetus = "Succulentus";
            long liikId = db.taimLiikDao().insert(liik);

            KastmisVajadusIntervall intervall = new KastmisVajadusIntervall();
            intervall.paevad = 7;
            long intId = db.kastmisVajadusIntervallDao().insert(intervall);

            TaimSort sort = new TaimSort();
            sort.nimetus = "Ficus Benjamin";
            sort.ladinakeelne_nimetus = "Ficus benjamina";
            sort.liik_id = (int) liikId;
            sort.kastmisvajadus = (int) intId;
            long sortId = db.taimSortDao().insert(sort);

            Taim taim = new Taim();
            taim.nimi = "My Office Ficus";
            taim.kasutaja_id = (int) userId;
            taim.sort_id = (int) sortId;
            long taimId = db.taimDao().insert(taim);

            // 2. Add HooldusTüüp (Missing dependency)
            HooldusTüüp hooldus = new HooldusTüüp();
            hooldus.nimetus = "Kastmine";
            long hooldusId = db.hooldusTüüpDao().insert(hooldus);

            // 3. Add a Notification (Teade)
            Teade teade = new Teade();
            teade.taim_id = (int) taimId;
            teade.hooldusTüüp_id = (int) hooldusId; // Link it!
            teade.aeg = System.currentTimeMillis() + 86400000;
            teade.kommentaar = "Time to water the Ficus!";
            db.teadeDao().insert(teade);

            List<Taim> taimed = db.taimDao().getByUserId((int) userId);
        }).start();
    }

    public void goToMain(View view) {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
}
